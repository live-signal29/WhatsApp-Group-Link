# Google Play Billing Testing Guide

This document describes how to configure and test in-app promotion purchases with Google Play Console.

---

## 1. Product Configuration

- **Product ID**: `promotion_3_days`
- **Product Type**: In-app product (Consumable)
- **Product Title**: `3-Day Group Promotion`
- **Product Description**: `Promote your WhatsApp group or channel for 3 days.`
- **Price**: Configure your regional pricing in Google Play Console (e.g., Rs 239, $1.99, etc.).

---

## 2. Setting Up in Google Play Console

1. Open your app in [Google Play Console](https://play.google.com/console).
2. In the left menu, scroll down to **Monetize > In-app products**.
3. Click **Create product**.
4. Set **Product ID** to: `promotion_3_days`.
5. Enter title, description, and configure default price.
6. Set Status to **Active**.

---

## 3. Configuring License Testers (Free Testing)

To test purchases without incurring real credit card charges:
1. In Play Console, navigate to **Settings > License testing**.
2. Add your Google test account emails (e.g. `tester@gmail.com`).
3. Set **License test response** to:
   - `RESPOND_NORMALLY` (prompts test card with "Always approves" or "Always declines")
   - Or test pending purchases with "Slow test card (approves after a few minutes)" to test `PENDING` states.

---

## 4. Internal Testing Track
1. Upload your signed release bundle (`.aab`) to the **Internal testing** track.
2. Add your testers' Google emails to the email list.
3. Share the opt-in link with the tester.
4. Once accepted, download the app from Google Play on the physical test device.
5. Tap **Promote Now**: The official Google Play Billing bottom sheet appears with `Test Card, always approves`.
6. Tap buy: the app verifies, consumes the purchase, and immediately marks the group as **Promoted**!
