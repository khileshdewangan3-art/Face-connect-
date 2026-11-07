# FaceConnect Android

Java Android app with a local-first chat design and a standalone Node.js message relay. Firebase is not used.

## Chat architecture
- Each phone stores its own conversation history in its private SQLite database.
- The Android app gets a stable random FaceConnect ID on first launch.
- Messages are sent through the standalone HTTPS relay.
- The relay keeps undelivered messages in RAM only, for up to 24 hours, and removes them when the recipient fetches them.
- The relay does not write message history to disk. A server restart/redeploy can lose undelivered messages.
- This prototype is NOT end-to-end encrypted and IDs are not yet protected by account authentication. Do not use for sensitive conversations.

## Deploy the relay (one-time setup)
1. Push this repository to GitHub.
2. In Render, create a new Web Service and connect this repository. The included `render.yaml` can configure it automatically.
3. Wait for deployment and copy the service HTTPS URL, for example `https://your-service.onrender.com`.
4. Open `app/src/main/java/com/khilesh/faceconnect/FirebaseRelay.java` and replace `https://YOUR-RENDER-SERVICE.onrender.com` with your real service URL.
5. Commit and push the change. GitHub Actions will build the APK.

## Build APK
Open Actions → Build FaceConnect APK → wait for success → download the `FaceConnect-debug-apk` artifact.

## Important limitations
This is a prototype relay, not a production messaging service. The current RAM queue can lose messages on server restart and has no durable delivery guarantee, push notifications, phone verification, abuse controls, or end-to-end encryption. Free hosting may sleep when idle. Chat history remains on the phone and can be lost if app data is cleared or the app is uninstalled.
