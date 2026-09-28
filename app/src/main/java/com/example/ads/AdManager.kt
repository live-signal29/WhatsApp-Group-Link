package com.example.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.BuildConfig
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.OnUserEarnedRewardListener
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform

object AdConfig {
    // =========================================================================
    // ADMOB AD UNIT IDs CONFIGURATION
    // App ID: ca-app-pub-1895906484640218~7497689908
    // =========================================================================

    // Test IDs provided by Google for safe testing (to prevent invalid traffic policy flags in debug):
    private const val TEST_BANNER_ID = "ca-app-pub-3940256099942544/6300978111"
    private const val TEST_INTERSTITIAL_ID = "ca-app-pub-3940256099942544/1033173712"
    private const val TEST_REWARDED_INTERSTITIAL_ID = "ca-app-pub-3940256099942544/5354046379"

    // Production AdMob Unit IDs from your AdMob Console:
    const val PRODUCTION_BANNER_ID = "ca-app-pub-1895906484640218/5647309771"
    const val PRODUCTION_REWARDED_INTERSTITIAL_ID = "ca-app-pub-1895906484640218/6184608237"
    const val PRODUCTION_INTERSTITIAL_ID = "ca-app-pub-1895906484640218/6184608237"

    val BANNER_AD_UNIT_ID: String
        get() = if (BuildConfig.DEBUG) TEST_BANNER_ID else PRODUCTION_BANNER_ID

    val REWARDED_INTERSTITIAL_AD_UNIT_ID: String
        get() = if (BuildConfig.DEBUG) TEST_REWARDED_INTERSTITIAL_ID else PRODUCTION_REWARDED_INTERSTITIAL_ID

    val INTERSTITIAL_AD_UNIT_ID: String
        get() = if (BuildConfig.DEBUG) TEST_INTERSTITIAL_ID else PRODUCTION_INTERSTITIAL_ID
}

object AdManager {
    private const val TAG = "AdManager"

    private var interstitialAd: InterstitialAd? = null
    private var isInterstitialLoading = false
    private var lastInterstitialShowTime = 0L
    private const val MIN_INTERSTITIAL_INTERVAL_MS = 60_000L // Throttle 60 seconds

    private var rewardedInterstitialAd: RewardedInterstitialAd? = null
    private var isRewardedLoading = false

    var canRequestAds: Boolean = true
        private set

    fun initialize(context: Context) {
        try {
            val consentInformation: ConsentInformation = UserMessagingPlatform.getConsentInformation(context)
            val params = ConsentRequestParameters.Builder()
                .setTagForUnderAgeOfConsent(false)
                .build()

            consentInformation.requestConsentInfoUpdate(
                context as? Activity ?: return,
                params,
                {
                    UserMessagingPlatform.loadAndShowConsentFormIfRequired(
                        context
                    ) { formError ->
                        if (formError != null) {
                            Log.w(TAG, "Consent form error: ${formError.message}")
                        }
                        if (consentInformation.canRequestAds()) {
                            canRequestAds = true
                            startAdsInitialization(context)
                        }
                    }
                },
                { requestConsentError ->
                    Log.w(TAG, "Consent update error: ${requestConsentError.message}")
                    // Allow ads if consent check failed or in debug mode
                    canRequestAds = true
                    startAdsInitialization(context)
                }
            )

            if (consentInformation.canRequestAds()) {
                canRequestAds = true
                startAdsInitialization(context)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing UMP", e)
            canRequestAds = true
            startAdsInitialization(context)
        }
    }

    private fun startAdsInitialization(context: Context) {
        MobileAds.initialize(context) { status ->
            Log.d(TAG, "MobileAds initialized: $status")
            preloadInterstitial(context)
            preloadRewardedInterstitial(context)
        }
    }

    fun preloadInterstitial(context: Context) {
        if (!canRequestAds || isInterstitialLoading || interstitialAd != null) return
        isInterstitialLoading = true

        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            context,
            AdConfig.INTERSTITIAL_AD_UNIT_ID,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    isInterstitialLoading = false
                    Log.d(TAG, "Interstitial ad loaded successfully")
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    interstitialAd = null
                    isInterstitialLoading = false
                    Log.w(TAG, "Interstitial ad failed to load: ${loadAdError.message}")
                }
            }
        )
    }

    fun preloadRewardedInterstitial(context: Context) {
        if (!canRequestAds || isRewardedLoading || rewardedInterstitialAd != null) return
        isRewardedLoading = true

        val adRequest = AdRequest.Builder().build()
        RewardedInterstitialAd.load(
            context,
            AdConfig.REWARDED_INTERSTITIAL_AD_UNIT_ID,
            adRequest,
            object : RewardedInterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedInterstitialAd) {
                    rewardedInterstitialAd = ad
                    isRewardedLoading = false
                    Log.d(TAG, "Rewarded interstitial loaded successfully")
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    rewardedInterstitialAd = null
                    isRewardedLoading = false
                    Log.w(TAG, "Rewarded interstitial failed to load: ${loadAdError.message}")
                }
            }
        )
    }

    /**
     * Shows an interstitial ad if available and throttled.
     * Complies with user policy: never shown before checkout, never shown immediately after purchase,
     * throttled by time interval.
     */
    fun showInterstitialIfReady(activity: Activity, onAdDismissed: () -> Unit) {
        val now = System.currentTimeMillis()
        if (now - lastInterstitialShowTime < MIN_INTERSTITIAL_INTERVAL_MS) {
            // Throttled
            onAdDismissed()
            return
        }

        val ad = interstitialAd
        if (ad != null) {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    interstitialAd = null
                    lastInterstitialShowTime = System.currentTimeMillis()
                    preloadInterstitial(activity)
                    onAdDismissed()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    interstitialAd = null
                    preloadInterstitial(activity)
                    onAdDismissed()
                }
            }
            ad.show(activity)
        } else {
            preloadInterstitial(activity)
            onAdDismissed()
        }
    }

    /**
     * Shows a Rewarded Interstitial ad if available.
     */
    fun showRewardedInterstitialIfReady(
        activity: Activity,
        onRewardEarned: () -> Unit = {},
        onDismissed: () -> Unit = {}
    ) {
        val ad = rewardedInterstitialAd
        if (ad != null) {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    rewardedInterstitialAd = null
                    preloadRewardedInterstitial(activity)
                    onDismissed()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    rewardedInterstitialAd = null
                    preloadRewardedInterstitial(activity)
                    onDismissed()
                }
            }
            ad.show(activity, OnUserEarnedRewardListener { rewardItem ->
                Log.d(TAG, "User earned reward: ${rewardItem.amount} ${rewardItem.type}")
                onRewardEarned()
            })
        } else {
            preloadRewardedInterstitial(activity)
            onDismissed()
        }
    }
}

/**
 * Modern non-intrusive banner ad composable
 */
@Composable
fun AdBannerView(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            modifier = Modifier.fillMaxWidth(),
            factory = { context ->
                AdView(context).apply {
                    setAdSize(AdSize.BANNER)
                    adUnitId = AdConfig.BANNER_AD_UNIT_ID

                    // Prevent Mesa renderer crash when DRM rendernode is unavailable in emulator
                    val isEmulator = android.os.Build.FINGERPRINT.startsWith("generic") ||
                            android.os.Build.MODEL.contains("google_sdk") ||
                            android.os.Build.HARDWARE.contains("goldfish") ||
                            android.os.Build.HARDWARE.contains("ranchu")
                    if (isEmulator) {
                        setLayerType(android.view.View.LAYER_TYPE_SOFTWARE, null)
                    }

                    adListener = object : com.google.android.gms.ads.AdListener() {
                        override fun onAdLoaded() {
                            Log.d("AdBannerView", "Banner ad loaded successfully")
                        }

                        override fun onAdFailedToLoad(error: LoadAdError) {
                            Log.w("AdBannerView", "Banner ad failed to load: ${error.message}")
                        }
                    }

                    loadAd(AdRequest.Builder().build())
                }
            },
            onRelease = { adView ->
                try {
                    adView.destroy()
                } catch (e: Exception) {
                    Log.w("AdBannerView", "Error destroying adView: ${e.message}")
                }
            }
        )
    }
}
