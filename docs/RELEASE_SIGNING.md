# Android Release Signing & CI/CD Guide

This guide describes how to sign your release Android App Bundle (AAB) for Google Play Console distribution, and configure GitHub Actions for automatic builds.

---

## 1. Generate an Upload Keystore

Run the following command on your terminal:

```bash
keytool -genkey -v -keystore my-upload-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias upload
```

You will be asked to enter a password and your developer details. Save these credentials securely!

---

## 2. Prepare Base64 Keystore for GitHub Actions

Convert your `.jks` file into a single base64 string:

```bash
base64 -w 0 my-upload-key.jks > keystore_base64.txt
```

---

## 3. Configure GitHub Secrets

In your GitHub repository:
1. Go to **Settings > Secrets and variables > Actions > New repository secret**.
2. Add the following secrets:

| Secret Name | Value |
|---|---|
| `ANDROID_KEYSTORE_BASE64` | The entire base64 string from `keystore_base64.txt` |
| `KEYSTORE_PASSWORD` | The keystore password you set in step 1 |
| `KEY_ALIAS` | `upload` |
| `KEY_PASSWORD` | The key password you set in step 1 |

---

## 4. Automatic Build Process
Whenever you push to `main` or trigger a `workflow_dispatch`:
- GitHub Actions automatically runs `.github/workflows/android.yml`.
- It builds the debug APK (`group-links-debug-apk`).
- It decodes the keystore and builds the signed release bundle (`group-links-release-aab`).
- Download the generated `.aab` from the GitHub Actions Artifacts tab and upload it directly to Google Play Console.
