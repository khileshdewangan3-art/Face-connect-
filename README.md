# FaceConnect Android

FaceConnect is a Java Android community and messaging app.

## Local-first design
- Posts, likes and comments are saved on the device.
- Chat history is stored in the app's private SQLite database on each phone.
- Message delivery uses Firebase Realtime Database as a temporary relay.
- The recipient saves an incoming message locally and then the app deletes that relay item.
- This is not end-to-end encryption. Do not use it for sensitive messages until audited E2EE is implemented.

## Configure online chat
1. Create an Android app in Firebase with package name `com.khilesh.faceconnect`.
2. Download `google-services.json` and put it at `app/google-services.json`.
3. In Firebase Authentication, enable **Anonymous** sign-in.
4. Create a Realtime Database.
5. In Realtime Database Rules, publish the contents of `database.rules.json`.
6. Build the APK using GitHub Actions.

The Friends tab has a **Chat with FaceConnect ID** action. Each user opens Chat to see their own Firebase UID and shares it with the other person. The other person enters that UID to start a chat.

## Build APK
Open Actions → Build FaceConnect APK → wait for success → download the `FaceConnect-debug-apk` artifact.

## Important
Without Firebase configuration, the app can still build and its local feed works, but online message delivery remains disabled. Pending messages exist temporarily in the relay inbox until the recipient receives them; received messages are deleted from the relay after being copied to the recipient's local database. Local app data can be lost if the user clears app storage or uninstalls the app. This first chat version uses anonymous Firebase IDs and does not yet include phone-number login, contact discovery, media attachments, push notifications, or end-to-end encryption.
