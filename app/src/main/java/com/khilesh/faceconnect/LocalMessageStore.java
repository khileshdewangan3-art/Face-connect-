package com.khilesh.faceconnect;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;

/** Private on-device chat history. Message bodies are never uploaded by this class. */
public class LocalMessageStore extends SQLiteOpenHelper {
    public static class Message {
        public final String id, peerUid, senderUid, body;
        public final long sentAt;
        public Message(String id, String peerUid, String senderUid, String body, long sentAt) {
            this.id=id; this.peerUid=peerUid; this.senderUid=senderUid; this.body=body; this.sentAt=sentAt;
        }
    }
    public LocalMessageStore(Context context) { super(context, "faceconnect_local.db", null, 1); }
    @Override public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE messages (id TEXT PRIMARY KEY, peer_uid TEXT NOT NULL, sender_uid TEXT NOT NULL, body TEXT NOT NULL, sent_at INTEGER NOT NULL)");
    }
    @Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) { }
    public synchronized void save(Message m) {
        ContentValues v=new ContentValues(); v.put("id",m.id); v.put("peer_uid",m.peerUid);
        v.put("sender_uid",m.senderUid); v.put("body",m.body); v.put("sent_at",m.sentAt);
        getWritableDatabase().insertWithOnConflict("messages",null,v,SQLiteDatabase.CONFLICT_REPLACE);
    }
    public synchronized ArrayList<Message> conversation(String peerUid) {
        ArrayList<Message> out=new ArrayList<>();
        try(Cursor c=getReadableDatabase().query("messages",null,"peer_uid=?",new String[]{peerUid},null,null,"sent_at ASC")) {
            while(c.moveToNext()) out.add(new Message(c.getString(c.getColumnIndexOrThrow("id")),
                c.getString(c.getColumnIndexOrThrow("peer_uid")),c.getString(c.getColumnIndexOrThrow("sender_uid")),
                c.getString(c.getColumnIndexOrThrow("body")),c.getLong(c.getColumnIndexOrThrow("sent_at"))));
        }
        return out;
    }
    public synchronized void clearConversation(String peerUid) {
        getWritableDatabase().delete("messages","peer_uid=?",new String[]{peerUid});
    }
}
