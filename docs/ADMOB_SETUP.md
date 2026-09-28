# Google AdMob & UMP Integration Guide

This guide describes how to configure Google Mobile Ads and User Messaging Platform (UMP) for Group Links.

---

## 1. Development & Testing (Default Behavior)
By default, the application is pre-configured with Google's official sample/test Ad Unit IDs:
- **Sample App ID**: `ca-app-pub-3940256099942544~3347511713`
- **Banner Ad Unit ID**: `ca-app-pub-3940256099942544/6300978111`
- **Interstitial Ad Unit ID**: `ca-app-pub-3940256099942544/1033173712`
- **Native Ad Unit ID**: `ca-app-pub-3940256099942544/2240015110`

These IDs ensure your AdMob account is never penalized during development, local testing, or QA.

---

## 2. Production Setup

### Step 1: Create an AdMob App
1. Go to [Google AdMob Console](https://apps.admob.com/).
2. Click **Apps > Add App > Android > Yes (or No if not published yet)**.
3. Name: **Group Links**.
4. Copy your **App ID** (format: `ca-app-pub-XXXXXXXXXXXXXXXX~XXXXXXXXXX`).

### Step 2: Create Ad Units
Create the following ad units:
1. **Banner Ad**: Named `Home_Banner`
2. **Interstitial Ad**: Named `Transition_Interstitial`

### Step 3: Update AndroidManifest.xml
In `app/src/main/AndroidManifest.xml`:
```xml
<meta-data
    android:name="com.google.android.gms.ads.APPLICATION_ID"
    android:value="ca-app-pub-YOUR_PUBLISHER_ID~YOUR_APP_ID" />
```

### Step 4: Update AdConfig.kt
In `app/src/main/java/com/example/ads/AdManager.kt`, update the production constants:
```kotlin
private const val PRODUCTION_BANNER_ID = "ca-app-pub-YOUR_ID/YOUR_BANNER_UNIT"
private const val PRODUCTION_INTERSTITIAL_ID = "ca-app-pub-YOUR_ID/YOUR_INTERSTITIAL_UNIT"
```

---

## 3. Policy & User Messaging Platform (UMP)
1. In the AdMob console, go to **Privacy & Messaging**.
2. Configure your **GDPR message** and **US state privacy laws message**.
3. Group Links automatically requests consent at launch using `AdManager.initialize(context)` before loading ads.
4. Ads are never placed over interactive checkout buttons or Join/Follow buttons.
