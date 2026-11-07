package com.khilesh.faceconnect;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.HashMap;
import java.util.Map;

/**
 * Temporary relay only: incoming messages are copied to local SQLite then deleted
 * from the recipient's Firebase inbox. This is not end-to-end encryption.
 */
public class FirebaseRelay {
    public interface Callback {
        void onReady(String uid);
        void onMessage(String id, String fromUid, String body, long sentAt);
        void onError(String message);
    }
    private final FirebaseAuth auth;
    private final DatabaseReference root;
    private ChildEventListener inboxListener;

    public FirebaseRelay() {
        if (FirebaseApp.getApps().isEmpty()) throw new IllegalStateException("Firebase is not configured. Add app/google-services.json.");
        auth=FirebaseAuth.getInstance();
        root=FirebaseDatabase.getInstance().getReference();
    }
    public void connect(Callback callback) {
        if(auth.getCurrentUser()!=null) startInbox(callback,auth.getCurrentUser().getUid());
        else auth.signInAnonymously().addOnSuccessListener(result ->
            startInbox(callback,result.getUser().getUid())
        ).addOnFailureListener(e -> callback.onError(e.getMessage()==null?"Firebase sign-in failed":e.getMessage()));
    }
    private void startInbox(Callback callback,String uid) {
        callback.onReady(uid);
        inboxListener=root.child("inbox").child(uid).addChildEventListener(new ChildEventListener() {
            @Override public void onChildAdded(DataSnapshot snap,String previous) {
                String from=snap.child("from").getValue(String.class);
                String body=snap.child("body").getValue(String.class);
                Long sent=snap.child("sentAt").getValue(Long.class);
                if(from==null || body==null || sent==null) { snap.getRef().removeValue(); return; }
                callback.onMessage(snap.getKey(),from,body,sent);
                snap.getRef().removeValue(); // recipient has received it; long-term history stays on device
            }
            @Override public void onChildChanged(DataSnapshot s,String p) { }
            @Override public void onChildRemoved(DataSnapshot s) { }
            @Override public void onChildMoved(DataSnapshot s,String p) { }
            @Override public void onCancelled(DatabaseError e) { callback.onError(e.getMessage()); }
        });
    }
    public void send(String myUid,String recipientUid,String body,Callback callback) {
        if(recipientUid==null || recipientUid.trim().isEmpty()) { callback.onError("Recipient ID is required."); return; }
        String id=root.child("inbox").child(recipientUid).push().getKey();
        Map<String,Object> payload=new HashMap<>();
        payload.put("from",myUid); payload.put("to",recipientUid); payload.put("body",body);
        payload.put("sentAt",System.currentTimeMillis());
        root.child("inbox").child(recipientUid).child(id).setValue(payload)
            .addOnSuccessListener(v -> { })
            .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }
    public void disconnect() {
        if(inboxListener!=null && auth.getCurrentUser()!=null)
            root.child("inbox").child(auth.getCurrentUser().getUid()).removeEventListener(inboxListener);
    }
}
