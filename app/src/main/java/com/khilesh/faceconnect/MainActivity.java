package com.khilesh.faceconnect;

import android.app.*;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import android.widget.*;
import android.text.InputType;
import java.util.*;

public class MainActivity extends Activity {
    LinearLayout root, feed;
    EditText composer;
    final ArrayList<Post> posts = new ArrayList<>();
    int blue = Color.rgb(24, 119, 242);
    int ink = Color.rgb(28, 30, 33);
    int muted = Color.rgb(101, 103, 107);

    static class Post {
        String author, body;
        int likes = 0;
        ArrayList<String> comments = new ArrayList<>();
        Post(String a, String b) { author=a; body=b; }
    }

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().setStatusBarColor(Color.WHITE);
        getWindow().setNavigationBarColor(Color.WHITE);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        posts.add(new Post("FaceConnect", "Welcome! Apni community se judne ke liye pehli post share karein."));
        posts.add(new Post("Aarav Sharma", "Namaste dosto! FaceConnect par aapka swagat hai. 👋"));
        showHome();
    }

    TextView text(String s, int size, int color, boolean bold) {
        TextView t = new TextView(this); t.setText(s); t.setTextSize(size); t.setTextColor(color);
        if (bold) t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        t.setPadding(0, 4, 0, 4); return t;
    }
    GradientDrawable bg(int color, int radius) {
        GradientDrawable d = new GradientDrawable(); d.setColor(color); d.setCornerRadius(radius); return d;
    }
    LinearLayout column() { LinearLayout l=new LinearLayout(this); l.setOrientation(LinearLayout.VERTICAL); return l; }
    LinearLayout row() { LinearLayout l=new LinearLayout(this); l.setOrientation(LinearLayout.HORIZONTAL); l.setGravity(Gravity.CENTER_VERTICAL); return l; }
    void showHome() {
        root=column(); root.setBackgroundColor(Color.rgb(240,242,245)); setContentView(root);
        LinearLayout header=row(); header.setPadding(16,12,16,12); header.setBackgroundColor(Color.WHITE);
        TextView logo=text("FaceConnect",25,blue,true); header.addView(logo,new LinearLayout.LayoutParams(0,-2,1));
        TextView search=text("⌕",30,ink,true); header.addView(search); root.addView(header);
        LinearLayout nav=row(); nav.setBackgroundColor(Color.WHITE); nav.setPadding(8,10,8,10);
        String[] tabs={"⌂  Home","♙  Friends","＋  Create","♧  Alerts","☻  Profile"};
        for(String tab:tabs){ TextView v=text(tab,13,blue,true); v.setGravity(Gravity.CENTER); nav.addView(v,new LinearLayout.LayoutParams(0,-2,1)); }
        root.addView(nav);
        ScrollView scroll=new ScrollView(this); feed=column(); scroll.addView(feed); root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        LinearLayout create=column(); create.setPadding(14,12,14,12); create.setBackgroundColor(Color.WHITE);
        composer=new EditText(this); composer.setHint("Aapke dimag mein kya chal raha hai?"); composer.setMinLines(2); composer.setGravity(Gravity.TOP); composer.setTextSize(15);
        create.addView(composer);
        Button postBtn=new Button(this); postBtn.setText("Post Share Karein"); postBtn.setTextColor(Color.WHITE); postBtn.setBackgroundTintList(android.content.res.ColorStateList.valueOf(blue));
        postBtn.setOnClickListener(v->{String s=composer.getText().toString().trim(); if(!s.isEmpty()){posts.add(0,new Post("You",s)); composer.setText(""); ((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(composer.getWindowToken(),0); renderPosts();}});
        create.addView(postBtn); root.addView(create);
        renderPosts();
    }
    void renderPosts() {
        feed.removeAllViews();
        for(Post p:posts) {
            LinearLayout card=column(); card.setPadding(16,12,16,10); card.setBackgroundColor(Color.WHITE);
            LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(-1,-2); cp.setMargins(0,7,0,7); feed.addView(card,cp);
            card.addView(text(p.author,16,ink,true));
            card.addView(text("Abhi · 🌐",12,muted,false));
            TextView body=text(p.body,16,ink,false); body.setPadding(0,12,0,14); card.addView(body);
            View line=new View(this); line.setBackgroundColor(Color.LTGRAY); card.addView(line,new LinearLayout.LayoutParams(-1,1));
            TextView counts=text("👍 "+p.likes+" likes     💬 "+p.comments.size()+" comments",12,muted,false); card.addView(counts);
            LinearLayout actions=row();
            Button like=new Button(this); like.setText("👍 Like"); like.setTextColor(blue); like.setOnClickListener(v->{p.likes++; renderPosts();});
            Button comment=new Button(this); comment.setText("💬 Comment"); comment.setTextColor(blue); comment.setOnClickListener(v->commentDialog(p));
            actions.addView(like,new LinearLayout.LayoutParams(0,-2,1)); actions.addView(comment,new LinearLayout.LayoutParams(0,-2,1)); card.addView(actions);
            for(String c:p.comments) card.addView(text("  • "+c,14,muted,false));
        }
    }
    void commentDialog(Post p) {
        EditText input=new EditText(this); input.setHint("Comment likhein");
        new AlertDialog.Builder(this).setTitle("Comment").setView(input).setNegativeButton("Cancel",null).setPositiveButton("Send",(d,w)->{
            String s=input.getText().toString().trim(); if(!s.isEmpty()){p.comments.add("You: "+s); renderPosts();}
        }).show();
    }
}
