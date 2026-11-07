package com.khilesh.faceconnect;

import android.app.*;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.content.*;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import org.json.*;
import java.util.*;

public class MainActivity extends Activity {
    final int blue=Color.rgb(24,119,242), ink=Color.rgb(28,30,33), muted=Color.rgb(101,103,107), bg=Color.rgb(245,247,250);
    LinearLayout root, content, composerBox;
    EditText composer;
    ArrayList<Post> posts=new ArrayList<>();
    SharedPreferences prefs;
    String page="Home";
    static class Post {
        String author,body,id; int likes=0; boolean liked=false; ArrayList<String> comments=new ArrayList<>();
        Post(String a,String b){author=a;body=b;id=UUID.randomUUID().toString();}
    }
    @Override public void onCreate(Bundle b){super.onCreate(b);prefs=getSharedPreferences("faceconnect",MODE_PRIVATE);getWindow().setStatusBarColor(Color.WHITE);getWindow().setNavigationBarColor(Color.WHITE);getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR|View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);load();showPage("Home");}
    TextView txt(String s,int size,int color,boolean bold){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);if(bold)t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);t.setPadding(0,5,0,5);return t;}
    GradientDrawable shape(int color,int radius){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(radius);return d;}
    LinearLayout col(){LinearLayout l=new LinearLayout(this);l.setOrientation(1);return l;}
    LinearLayout row(){LinearLayout l=new LinearLayout(this);l.setOrientation(0);l.setGravity(Gravity.CENTER_VERTICAL);return l;}
    void showPage(String p){page=p;root=col();root.setBackgroundColor(bg);setContentView(root);
        LinearLayout header=row();header.setPadding(18,12,18,12);header.setBackgroundColor(Color.WHITE);
        TextView logo=txt("FaceConnect",25,blue,true);header.addView(logo,new LinearLayout.LayoutParams(0,-2,1));
        TextView search=txt("⌕",30,ink,true);search.setOnClickListener(v->searchDialog());header.addView(search);root.addView(header);
        LinearLayout nav=row();nav.setBackgroundColor(Color.WHITE);nav.setPadding(4,8,4,8);
        String[] names={"Home","Friends","Create","Alerts","Profile"};String[] icons={"⌂","♙","+","♧","☺"};
        for(int i=0;i<names.length;i++){final String n=names[i];LinearLayout item=col();item.setGravity(Gravity.CENTER);TextView ic=txt(icons[i],20,page.equals(n)?blue:muted,true);TextView label=txt(n,11,page.equals(n)?blue:muted,true);item.addView(ic);item.addView(label);item.setOnClickListener(v->{if(n.equals("Create")){showPage("Home");composer.requestFocus();((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).showSoftInput(composer,InputMethodManager.SHOW_IMPLICIT);}else showPage(n);});nav.addView(item,new LinearLayout.LayoutParams(0,-2,1));}
        root.addView(nav);ScrollView scroll=new ScrollView(this);content=col();scroll.addView(content);root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        if(page.equals("Home"))home();else if(page.equals("Friends"))friends();else if(page.equals("Alerts"))alerts();else if(page.equals("Profile"))profile();
    }
    void home(){LinearLayout welcome=col();welcome.setPadding(18,16,18,12);welcome.setBackgroundColor(Color.WHITE);welcome.addView(txt("Your community, your space ✨",20,ink,true));welcome.addView(txt("Share updates and stay connected with friends.",13,muted,false));content.addView(welcome);
        composerBox=col();composerBox.setPadding(14,12,14,12);composerBox.setBackgroundColor(Color.WHITE);LinearLayout.LayoutParams m=new LinearLayout.LayoutParams(-1,-2);m.setMargins(0,8,0,8);content.addView(composerBox,m);
        composer=new EditText(this);composer.setHint("Aapke dimag mein kya chal raha hai?");composer.setMinLines(2);composer.setGravity(Gravity.TOP);composer.setTextSize(15);composerBox.addView(composer);
        Button post=new Button(this);post.setText("POST SHARE KAREIN");post.setTextColor(Color.WHITE);post.setBackgroundTintList(android.content.res.ColorStateList.valueOf(blue));post.setOnClickListener(v->publish());composerBox.addView(post);
        renderPosts();}
    void publish(){String s=composer.getText().toString().trim();if(s.isEmpty()){composer.setError("Post likhein");return;}posts.add(0,new Post("You",s));save();composer.setText("");((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(composer.getWindowToken(),0);renderPosts();Toast.makeText(this,"Post shared!",Toast.LENGTH_SHORT).show();}
    void renderPosts(){if(content==null)return;for(int i=content.getChildCount()-1;i>=2;i--)content.removeViewAt(i);for(Post p:posts){LinearLayout card=col();card.setPadding(16,13,16,12);card.setBackground(shape(Color.WHITE,18));LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(-1,-2);cp.setMargins(10,5,10,7);content.addView(card,cp);
        LinearLayout author=row();TextView avatar=txt(initial(p.author),16,Color.WHITE,true);avatar.setGravity(Gravity.CENTER);avatar.setBackground(shape(blue,50));LinearLayout.LayoutParams ap=new LinearLayout.LayoutParams(40,40);ap.setMargins(0,0,10,0);author.addView(avatar,ap);LinearLayout meta=col();meta.addView(txt(p.author,15,ink,true));meta.addView(txt("Abhi · 🌐",11,muted,false));author.addView(meta);card.addView(author);
        TextView body=txt(p.body,16,ink,false);body.setPadding(0,14,0,14);card.addView(body);View line=new View(this);line.setBackgroundColor(Color.rgb(235,237,240));card.addView(line,new LinearLayout.LayoutParams(-1,1));
        card.addView(txt("👍 "+p.likes+" likes     💬 "+p.comments.size()+" comments",12,muted,false));LinearLayout actions=row();Button like=new Button(this);like.setText((p.liked?"♥ Liked":"♡ Like"));like.setTextColor(p.liked?blue:muted);like.setOnClickListener(v->{p.liked=!p.liked;p.likes+=p.liked?1:-1;save();renderPosts();});Button comment=new Button(this);comment.setText("💬 Comment");comment.setTextColor(blue);comment.setOnClickListener(v->commentDialog(p));actions.addView(like,new LinearLayout.LayoutParams(0,-2,1));actions.addView(comment,new LinearLayout.LayoutParams(0,-2,1));card.addView(actions);
        for(String c:p.comments)card.addView(txt("  • "+c,13,muted,false));}}
    String initial(String s){return s.length()==0?"F":s.substring(0,1).toUpperCase();}
    void commentDialog(Post p){EditText input=new EditText(this);input.setHint("Comment likhein");new AlertDialog.Builder(this).setTitle("Add a comment").setView(input).setNegativeButton("Cancel",null).setPositiveButton("Send",(d,w)->{String s=input.getText().toString().trim();if(!s.isEmpty()){p.comments.add("You: "+s);save();renderPosts();}}).show();}
    void friends(){section("Find your people","Connect with people in your community.");String[] people={"Aarav Sharma","Priya Verma","Rahul Dewangan","Anjali Sahu"};for(String person:people){LinearLayout r=row();r.setPadding(16,12,16,12);r.setBackground(shape(Color.WHITE,14));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(10,5,10,5);content.addView(r,lp);r.addView(txt("  "+initial(person)+"   "+person,15,ink,true),new LinearLayout.LayoutParams(0,-2,1));Button add=new Button(this);add.setText("Add");add.setOnClickListener(v->{add.setText("Requested");add.setEnabled(false);Toast.makeText(this,"Friend request sent (demo)",Toast.LENGTH_SHORT).show();});r.addView(add);}}
    void alerts(){section("Notifications","You're all caught up.");content.addView(txt("🔔  New likes, comments and friend requests will appear here.",14,muted,false));}
    void profile(){section("Your profile","Manage your FaceConnect identity.");LinearLayout c=col();c.setGravity(Gravity.CENTER);c.setPadding(20,25,20,25);c.setBackground(shape(Color.WHITE,18));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(12,8,12,8);content.addView(c,lp);TextView av=txt("Y",32,Color.WHITE,true);av.setGravity(Gravity.CENTER);av.setBackground(shape(blue,100));c.addView(av,new LinearLayout.LayoutParams(76,76));c.addView(txt("You",22,ink,true));c.addView(txt(posts.size()+" posts shared",14,muted,false));Button edit=new Button(this);edit.setText("Edit display name");edit.setOnClickListener(v->{EditText e=new EditText(this);e.setText(prefs.getString("name","You"));new AlertDialog.Builder(this).setTitle("Display name").setView(e).setNegativeButton("Cancel",null).setPositiveButton("Save",(d,w)->{String n=e.getText().toString().trim();if(!n.isEmpty()){prefs.edit().putString("name",n).apply();Toast.makeText(this,"Name saved",Toast.LENGTH_SHORT).show();}}).show();});c.addView(edit);}
    void section(String title,String sub){LinearLayout h=col();h.setPadding(18,20,18,15);h.setBackgroundColor(Color.WHITE);h.addView(txt(title,23,ink,true));h.addView(txt(sub,13,muted,false));content.addView(h);}
    void searchDialog(){EditText e=new EditText(this);e.setHint("Search posts");new AlertDialog.Builder(this).setTitle("Search FaceConnect").setView(e).setNegativeButton("Close",null).setPositiveButton("Search",(d,w)->{String q=e.getText().toString().trim().toLowerCase();showPage("Home");if(!q.isEmpty()){ArrayList<Post> found=new ArrayList<>();for(Post p:posts)if(p.body.toLowerCase().contains(q)||p.author.toLowerCase().contains(q))found.add(p);content.removeAllViews();content.addView(txt("Search results: "+found.size(),16,ink,true));for(Post p:found)content.addView(txt(p.author+"\n"+p.body,15,ink,false));}}).show();}
    void save(){JSONArray a=new JSONArray();try{for(Post p:posts){JSONObject o=new JSONObject();o.put("author",p.author);o.put("body",p.body);o.put("likes",p.likes);o.put("liked",p.liked);JSONArray cs=new JSONArray();for(String c:p.comments)cs.put(c);o.put("comments",cs);a.put(o);}prefs.edit().putString("posts",a.toString()).apply();}catch(Exception ignored){}}
    void load(){String raw=prefs.getString("posts",null);if(raw==null){posts.add(new Post("FaceConnect","Welcome! Apni community se judne ke liye pehli post share karein."));posts.add(new Post("Aarav Sharma","Namaste dosto! FaceConnect par aapka swagat hai. 👋"));save();return;}try{JSONArray a=new JSONArray(raw);for(int i=0;i<a.length();i++){JSONObject o=a.getJSONObject(i);Post p=new Post(o.optString("author","Member"),o.optString("body",""));p.likes=o.optInt("likes",0);p.liked=o.optBoolean("liked",false);JSONArray cs=o.optJSONArray("comments");if(cs!=null)for(int j=0;j<cs.length();j++)p.comments.add(cs.getString(j));posts.add(p);}}catch(Exception e){posts.clear();posts.add(new Post("FaceConnect","Welcome to FaceConnect!"));}}
}
