package com.khilesh.faceconnect;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.*;
import java.util.ArrayList;

public class ChatActivity extends Activity implements FirebaseRelay.Callback {
    LinearLayout messages;
    EditText input;
    TextView identity,status;
    ScrollView scroll;
    FirebaseRelay relay;
    LocalMessageStore store;
    String myUid="",peerUid="";
    int blue=Color.rgb(24,119,242);
    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        peerUid=getIntent().getStringExtra("peerUid");
        if(peerUid==null)peerUid="";
        store=new LocalMessageStore(this);
        LinearLayout root=new LinearLayout(this);root.setOrientation(1);root.setBackgroundColor(Color.rgb(245,247,250));
        LinearLayout header=new LinearLayout(this);header.setPadding(16,12,16,12);header.setGravity(Gravity.CENTER_VERTICAL);header.setBackgroundColor(Color.WHITE);
        TextView title=new TextView(this);title.setText("FaceConnect Chat");title.setTextSize(20);title.setTextColor(blue);title.setTypeface(null,1);header.addView(title);
        root.addView(header);
        identity=new TextView(this);identity.setPadding(14,8,14,8);identity.setTextColor(Color.DKGRAY);root.addView(identity);
        status=new TextView(this);status.setPadding(14,4,14,8);status.setTextColor(Color.GRAY);status.setText("Connecting…");root.addView(status);
        scroll=new ScrollView(this);messages=new LinearLayout(this);messages.setOrientation(1);scroll.addView(messages);root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        LinearLayout composer=new LinearLayout(this);composer.setPadding(8,8,8,8);composer.setBackgroundColor(Color.WHITE);
        input=new EditText(this);input.setSingleLine(true);input.setHint("Message");composer.addView(input,new LinearLayout.LayoutParams(0,-2,1));
        Button send=new Button(this);send.setText("Send");send.setTextColor(Color.WHITE);send.setBackgroundTintList(android.content.res.ColorStateList.valueOf(blue));send.setOnClickListener(v->sendMessage());composer.addView(send);root.addView(composer);
        setContentView(root);renderLocal();
        try{relay=new FirebaseRelay();relay.connect(this);}catch(Exception e){status.setText("Firebase setup pending. Local chat history is available on this phone.");}
    }
    @Override public void onReady(String uid){myUid=uid;identity.setText("Your FaceConnect ID: "+uid+"\nChat with ID: "+peerUid);status.setText("Connected to message relay");}
    @Override public void onMessage(String id,String from,String body,long time){store.save(new LocalMessageStore.Message(id,from,from,body,time));if(from.equals(peerUid))renderLocal();}
    @Override public void onError(String message){status.setText("Connection: "+message);}
    void sendMessage(){
        String body=input.getText().toString().trim();if(body.isEmpty())return;
        if(myUid.isEmpty()){status.setText("Connect Firebase first. Message was not sent.");return;}
        long now=System.currentTimeMillis();String localId="local-"+now;
        store.save(new LocalMessageStore.Message(localId,peerUid,myUid,body,now));input.setText("");renderLocal();
        relay.send(myUid,peerUid,body,this);
    }
    void renderLocal(){
        if(messages==null||peerUid.isEmpty())return;messages.removeAllViews();
        ArrayList<LocalMessageStore.Message> list=store.conversation(peerUid);
        for(LocalMessageStore.Message m:list){TextView bubble=new TextView(this);bubble.setText(m.body);bubble.setTextSize(16);bubble.setTextColor(Color.rgb(28,30,33));bubble.setPadding(14,10,14,10);bubble.setBackgroundColor(m.senderUid.equals(myUid)?Color.rgb(220,239,255):Color.WHITE);
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-2,-2);lp.setMargins(10,5,10,5);lp.gravity=m.senderUid.equals(myUid)?Gravity.RIGHT:Gravity.LEFT;messages.addView(bubble,lp);}
        scroll.post(()->scroll.fullScroll(ScrollView.FOCUS_DOWN));
    }
    @Override protected void onDestroy(){if(relay!=null)relay.disconnect();super.onDestroy();}
}
