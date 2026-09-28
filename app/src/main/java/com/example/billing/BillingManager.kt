package com.example.billing

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.consumePurchase
import com.android.billingclient.api.queryProductDetails
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class BillingState {
    data object Idle : BillingState()
    data object Loading : BillingState()
    data class Ready(val formattedPrice: String, val productDetails: ProductDetails?) : BillingState()
    data object Purchasing : BillingState()
    data class PendingPurchase(val message: String) : BillingState()
    data class PurchaseSuccess(val purchaseToken: String, val orderId: String, val listingId: String) : BillingState()
    data class Error(val message: String) : BillingState()
}

class BillingManager(private val context: Context) : PurchasesUpdatedListener {

    private val scope = CoroutineScope(Dispatchers.Main)
    private var billingClient: BillingClient? = null

    private val _billingState = MutableStateFlow<BillingState>(BillingState.Idle)
    val billingState: StateFlow<BillingState> = _billingState.asStateFlow()

    private var activeProductDetails: ProductDetails? = null
    private var currentTargetListingId: String = ""

    init {
        initializeBillingClient()
    }

    private fun initializeBillingClient() {
        val pendingPurchasesParams = PendingPurchasesParams.newBuilder()
            .enableOneTimeProducts()
            .build()

        billingClient = BillingClient.newBuilder(context)
            .setListener(this)
            .enablePendingPurchases(pendingPurchasesParams)
            .build()

        connectToGooglePlay()
    }

    fun connectToGooglePlay(onConnected: (() -> Unit)? = null) {
        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.d("BillingManager", "Billing setup finished successfully")
                    queryProducts()
                    onConnected?.invoke()
                } else {
                    Log.w("BillingManager", "Billing setup failed: ${billingResult.debugMessage} (${billingResult.responseCode})")
                    // Default fallback price if Play Console is not yet configured
                    _billingState.value = BillingState.Ready(
                        formattedPrice = "Rs 239",
                        productDetails = null
                    )
                }
            }

            override fun onBillingServiceDisconnected() {
                Log.w("BillingManager", "Billing service disconnected. Will retry on next request.")
            }
        })
    }

    fun queryProducts() {
        scope.launch {
            _billingState.value = BillingState.Loading
            val productList = listOf(
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(PRODUCT_ID_PROMOTION_3_DAYS)
                    .setProductType(BillingClient.ProductType.INAPP)
                    .build()
            )

            val params = QueryProductDetailsParams.newBuilder()
                .setProductList(productList)
                .build()

            val client = billingClient
            if (client == null || !client.isReady) {
                // If Play Billing is unavailable (e.g. offline, emulator without Play Store), provide default price display
                _billingState.value = BillingState.Ready(
                    formattedPrice = "Rs 239",
                    productDetails = null
                )
                return@launch
            }

            try {
                val productDetailsResult = client.queryProductDetails(params)
                val productDetailsList = productDetailsResult.productDetailsList

                if (productDetailsResult.billingResult.responseCode == BillingClient.BillingResponseCode.OK &&
                    !productDetailsList.isNullOrEmpty()
                ) {
                    val details = productDetailsList.firstOrNull { it.productId == PRODUCT_ID_PROMOTION_3_DAYS }
                    activeProductDetails = details
                    val formattedPrice = details?.oneTimePurchaseOfferDetails?.formattedPrice ?: "Rs 239"
                    _billingState.value = BillingState.Ready(
                        formattedPrice = formattedPrice,
                        productDetails = details
                    )
                } else {
                    Log.d("BillingManager", "Product not returned from Play Store: code ${productDetailsResult.billingResult.responseCode}. Using catalog default.")
                    _billingState.value = BillingState.Ready(
                        formattedPrice = "Rs 239",
                        productDetails = null
                    )
                }
            } catch (e: Exception) {
                Log.e("BillingManager", "Error querying product details", e)
                _billingState.value = BillingState.Ready(
                    formattedPrice = "Rs 239",
                    productDetails = null
                )
            }
        }
    }

    /**
     * Launches the official Google Play Billing checkout flow.
     */
    fun launchPurchaseFlow(activity: Activity, listingId: String, onFallbackPurchase: ((String) -> Unit)? = null) {
        currentTargetListingId = listingId
        val client = billingClient

        if (client == null || !client.isReady) {
            connectToGooglePlay {
                launchPurchaseFlow(activity, listingId, onFallbackPurchase)
            }
            return
        }

        val details = activeProductDetails
        if (details == null) {
            Log.w("BillingManager", "ProductDetails null; check Play Console configuration. Offering sandbox simulation.")
            // If in development/sandbox mode and Play Store has no product configured, allow simulated test purchase
            onFallbackPurchase?.invoke(listingId)
            return
        }

        _billingState.value = BillingState.Purchasing

        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(details)
                .build()
        )

        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .setObfuscatedAccountId(listingId) // Ties purchase to listing
            .build()

        val responseCode = client.launchBillingFlow(activity, flowParams).responseCode
        if (responseCode != BillingClient.BillingResponseCode.OK) {
            _billingState.value = BillingState.Error("Failed to open Google Play checkout: code $responseCode")
        }
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                if (purchases != null) {
                    for (purchase in purchases) {
                        handlePurchase(purchase)
                    }
                }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                _billingState.value = BillingState.Error("Purchase canceled.")
            }
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                // If consumable was not consumed from previous purchase, consume it now
                purchases?.firstOrNull()?.let { handlePurchase(it) }
                    ?: run { _billingState.value = BillingState.Error("Item already purchased. Verifying...") }
            }
            BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE,
            BillingClient.BillingResponseCode.NETWORK_ERROR -> {
                _billingState.value = BillingState.Error("Network error. Please check your internet connection.")
            }
            else -> {
                _billingState.value = BillingState.Error("Purchase error: ${billingResult.debugMessage}")
            }
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        when (purchase.purchaseState) {
            Purchase.PurchaseState.PURCHASED -> {
                scope.launch {
                    val client = billingClient ?: return@launch
                    // Consumable product: Consume the purchase token so user can promote again
                    val consumeParams = ConsumeParams.newBuilder()
                        .setPurchaseToken(purchase.purchaseToken)
                        .build()

                    val consumeResult = client.consumePurchase(consumeParams)
                    if (consumeResult.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        Log.d("BillingManager", "Purchase successfully consumed: ${purchase.purchaseToken.take(8)}...")
                    }

                    val listingId = purchase.accountIdentifiers?.obfuscatedAccountId ?: currentTargetListingId
                    _billingState.value = BillingState.PurchaseSuccess(
                        purchaseToken = purchase.purchaseToken,
                        orderId = purchase.orderId ?: ("order_${System.currentTimeMillis()}"),
                        listingId = listingId
                    )
                }
            }
            Purchase.PurchaseState.PENDING -> {
                _billingState.value = BillingState.PendingPurchase(
                    "Payment is pending. Your promotion will activate automatically after Google Play confirms the payment."
                )
            }
            Purchase.PurchaseState.UNSPECIFIED_STATE -> {
                _billingState.value = BillingState.Error("Purchase state unspecified. Please check your Google Play account.")
            }
        }
    }

    fun resetState() {
        val currentPrice = (billingState.value as? BillingState.Ready)?.formattedPrice ?: "Rs 239"
        _billingState.value = BillingState.Ready(currentPrice, activeProductDetails)
    }

    companion object {
        const val PRODUCT_ID_PROMOTION_3_DAYS = "promotion_3_days"
    }
}
