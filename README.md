# FaceConnect
Android social app with email/password signup and login, local SQLite chat history and a Firebase-free Node.js API.

## Backend setup
1. Create a free PostgreSQL database at https://neon.tech/ and copy its connection string.
2. Deploy this repository on https://render.com using the included render.yaml.
3. In Render Environment, set DATABASE_URL to the Neon connection string and JWT_SECRET to a long random value.
4. After deploy, open https://YOUR-SERVICE.onrender.com/health and check that it returns {"ok":true}.
5. Replace the SERVER constant in AuthActivity.java and FirebaseRelay.java with your Render HTTPS URL. Commit the change; GitHub Actions builds the APK.

## App
- Sign up with name, email and password (minimum 8 characters).
- Login with the same email/password.
- Passwords are hashed with bcrypt on the backend; session JWT stored on the phone.
- Local chat history is in SQLite; relay holds pending messages in RAM up to 24 hours.
- Prototype limitation: chat endpoints are not yet authenticated with JWT and messages are not end-to-end encrypted. Do not use for sensitive messages.
