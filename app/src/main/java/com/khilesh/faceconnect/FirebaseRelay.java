package com.khilesh.faceconnect;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.UUID;
import android.content.Context;
import android.content.SharedPreferences;

public class FirebaseRelay {
    public interface Callback {
        void onReady(String uid);
        void onMessage(String id, String fromUid, String body, long sentAt);
        void onError(String message);
    }
    // Replace this with the deployed HTTPS URL from your own relay hosting.
    private static final String SERVER = "https://YOUR-RENDER-SERVICE.onrender.com";
    private final ScheduledExecutorService worker = Executors.newSingleThreadScheduledExecutor();
    private volatile boolean running = false;
    private String uid = "";
    private Callback callback;

    public void connect(Context context, Callback cb) {
        callback = cb;
        SharedPreferences prefs = context.getSharedPreferences("faceconnect_identity", Context.MODE_PRIVATE);
        uid = prefs.getString("uid", "");
        if (uid.isEmpty()) {
            uid = UUID.randomUUID().toString().replace("-", "");
            prefs.edit().putString("uid", uid).apply();
        }
        cb.onReady(uid);
        if (SERVER.contains("YOUR-RENDER-SERVICE")) {
            cb.onError("Relay server deploy karna baki hai. README ke steps follow karein.");
            return;
        }
        running = true;
        worker.scheduleWithFixedDelay(this::poll, 0, 4, TimeUnit.SECONDS);
    }

    public void send(String myUid, String recipientUid, String body, Callback cb) {
        worker.execute(() -> {
            try {
                JSONObject payload = new JSONObject();
                payload.put("from", myUid); payload.put("to", recipientUid); payload.put("body", body);
                request("POST", SERVER + "/messages", payload.toString());
            } catch (Exception e) { cb.onError("Message send failed: " + e.getMessage()); }
        });
    }

    private void poll() {
        if (!running) return;
        try {
            JSONObject response = new JSONObject(request("GET", SERVER + "/inbox/" + uid, null));
            JSONArray items = response.optJSONArray("messages");
            if (items == null) return;
            for (int i = 0; i < items.length(); i++) {
                JSONObject m = items.getJSONObject(i);
                callback.onMessage(m.optString("id"), m.optString("from"), m.optString("body"), m.optLong("sentAt"));
            }
        } catch (Exception e) { callback.onError("Relay unavailable: " + e.getMessage()); }
    }

    private String request(String method, String endpoint, String json) throws Exception {
        HttpURLConnection c = (HttpURLConnection) new URL(endpoint).openConnection();
        c.setRequestMethod(method); c.setConnectTimeout(10000); c.setReadTimeout(10000);
        if (json != null) {
            c.setDoOutput(true); c.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            try (OutputStream out = c.getOutputStream()) { out.write(json.getBytes(StandardCharsets.UTF_8)); }
        }
        int code = c.getResponseCode();
        java.io.InputStream stream = code >= 200 && code < 300 ? c.getInputStream() : c.getErrorStream();
        StringBuilder result = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line; while ((line = reader.readLine()) != null) result.append(line);
        } finally { c.disconnect(); }
        if (code < 200 || code >= 300) throw new Exception("HTTP " + code + ": " + result);
        return result.toString();
    }

    public void disconnect() { running = false; worker.shutdownNow(); }
}
