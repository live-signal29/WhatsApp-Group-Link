# Firebase Setup Guide for WhatsApp Group Links

This guide details how to connect the WhatsApp Group Links Android app to your own Firebase project for real-time Firestore persistence, authentication, and secure rule enforcement.

---

## 1. Create a Firebase Project
1. Go to the [Firebase Console](https://console.firebase.google.com/).
2. Click **Add project**, enter **WhatsApp Group Links**, and follow the on-screen steps.
3. Once created, click on the **Android icon** to register an Android app.

## 2. Register Android App
1. **Package name**: Enter `com.aistudio.whatsappgrouplinks.pkrzla` (from `app/build.gradle.kts`).
2. **App nickname**: `WhatsApp Group Links`.
3. **Debug signing certificate SHA-1**: Optional during setup, but required if using Google Sign-In.
4. Download the generated `google-services.json` file.
5. Place `google-services.json` inside the `app/` directory of this project:
   ```
   app/google-services.json
   ```

## 3. Enable Firebase Services

### A. Cloud Firestore
1. In the Firebase console sidebar, navigate to **Build > Firestore Database**.
2. Click **Create database** and select your closest server location.
3. Start in **Production mode**.
4. In the **Rules** tab, paste the contents of `firestore.rules` provided in this repository.

### B. Firebase Authentication
1. Navigate to **Build > Authentication**.
2. Click **Get Started**.
3. Under the **Sign-in method** tab, enable:
   - **Email/Password**
   - **Anonymous** (for guest explorers)
4. (Optional) Create your first administrator account with email: `admin@grouplinks.app`.

---

## 4. Collections Structure
The app organizes data into these collections:
- `listings`: All submitted WhatsApp groups and channels.
- `categories`: Configurable categories (News, Crypto, Entertainment, etc.).
- `promotions`: Records of active and expired paid promotion campaigns.
- `purchases`: Receipts verified via Google Play Billing.
- `reports`: Community flag reports awaiting admin review.
- `app_settings`: Global configuration (pricing, duration, reach text, ad toggles).

---

## 5. Offline & Sandbox Fallback
If Firebase credentials are not yet added or the user is offline, Group Links operates seamlessly using an offline in-memory catalog, Room local database for history, and local admin authorization so the app is always functional.
