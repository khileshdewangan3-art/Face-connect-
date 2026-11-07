package com.khilesh.faceconnect;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import android.content.Intent;
import android.content.SharedPreferences;
import org.json.JSONObject;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AuthActivity extends Activity {
    private static final String SERVER = "https://YOUR-RENDER-SERVICE.onrender.com";
    private EditText email,password,name;
    private TextView message;
    private boolean signup=true;
    private final ExecutorService worker=Executors.newSingleThreadExecutor();
    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        SharedPreferences p=getSharedPreferences("faceconnect_identity",MODE_PRIVATE);
        if(p.getBoolean("logged_in",false)&&!p.getString("uid","").isEmpty()){openApp();return;}
        showForm();
    }
    private TextView text(String s,int size,int color){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);return t;}
    private void showForm(){
        LinearLayout root=new LinearLayout(this);root.setOrientation(1);root.setGravity(Gravity.CENTER);root.setPadding(28,24,28,24);root.setBackgroundColor(Color.rgb(245,247,250));
        TextView logo=text("FaceConnect",30,Color.rgb(24,119,242));logo.setTypeface(null,1);root.addView(logo);
        TextView sub=text("Connect with your people",15,Color.DKGRAY);sub.setPadding(0,6,0,20);root.addView(sub);
        name=new EditText(this);name.setSingleLine(true);name.setHint("Your name");root.addView(name);
        email=new EditText(this);email.setSingleLine(true);email.setHint("Email address");email.setInputType(33);root.addView(email);
        password=new EditText(this);password.setSingleLine(true);password.setHint("Password (minimum 8 characters)");password.setInputType(129);root.addView(password);
        Button action=new Button(this);action.setText("Create account");action.setTextColor(Color.WHITE);action.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.rgb(24,119,242)));root.addView(action);
        TextView toggle=text("Already have an account? Login",15,Color.rgb(24,119,242));toggle.setGravity(Gravity.CENTER);toggle.setPadding(0,18,0,18);root.addView(toggle);
        message=text("",13,Color.RED);root.addView(message);
        action.setOnClickListener(v->submit());
        toggle.setOnClickListener(v->{signup=!signup;name.setVisibility(signup?View.VISIBLE:View.GONE);action.setText(signup?"Create account":"Login");toggle.setText(signup?"Already have an account? Login":"New to FaceConnect? Sign up");message.setText("");});
        root.setFocusableInTouchMode(true);root.requestFocus();setContentView(root);
    }
    private void submit(){
        String n=name.getText().toString().trim(),e=email.getText().toString().trim().toLowerCase(),pw=password.getText().toString();
        if(!e.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")){email.setError("Valid email enter karein");return;}
        if(pw.length()<8){password.setError("Password minimum 8 characters");return;}
        if(signup&&n.length()<2){name.setError("Name required");return;}
        message.setText("Please wait…");
        worker.execute(()->{
            HttpURLConnection c=null;
            try{
                JSONObject body=new JSONObject();body.put("email",e);body.put("password",pw);if(signup)body.put("name",n);
                URL url=new URL(SERVER+(signup?"/auth/register":"/auth/login"));c=(HttpURLConnection)url.openConnection();c.setRequestMethod("POST");c.setConnectTimeout(12000);c.setReadTimeout(12000);c.setDoOutput(true);c.setRequestProperty("Content-Type","application/json; charset=utf-8");
                try(OutputStream out=c.getOutputStream()){out.write(body.toString().getBytes(StandardCharsets.UTF_8));}
                int code=c.getResponseCode();InputStream stream=code>=200&&code<300?c.getInputStream():c.getErrorStream();StringBuilder data=new StringBuilder();
                try(BufferedReader r=new BufferedReader(new InputStreamReader(stream,StandardCharsets.UTF_8))){String line;while((line=r.readLine())!=null)data.append(line);}
                JSONObject result=new JSONObject(data.toString());if(code<200||code>=300)throw new Exception(result.optString("error","Request failed"));
                getSharedPreferences("faceconnect_identity",MODE_PRIVATE).edit().putString("uid",result.getString("uid")).putString("token",result.getString("token")).putString("email",e).putString("name",result.optString("name",n)).putBoolean("logged_in",true).apply();
                runOnUiThread(this::openApp);
            }catch(Exception ex){runOnUiThread(()->message.setText(ex.getMessage()==null?"Server unavailable. Try again.":ex.getMessage()));}
            finally{if(c!=null)c.disconnect();}
        });
    }
    private void openApp(){startActivity(new Intent(this,MainActivity.class));finish();}
    @Override protected void onDestroy(){worker.shutdownNow();super.onDestroy();}
}
