package com.example

import android.app.Application
import android.util.Log
import com.example.ads.AdManager
import com.example.billing.BillingManager
import com.example.data.local.AppDatabase
import com.example.data.repository.AuthRepository
import com.example.data.repository.ListingRepository

class GroupLinksApplication : Application() {

    lateinit var database: AppDatabase
        private set

    lateinit var billingManager: BillingManager
        private set

    lateinit var listingRepository: ListingRepository
        private set

    lateinit var authRepository: AuthRepository
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        Log.d("GroupLinksApp", "Initializing Group Links Application")

        // Initialize Firebase safely with fallback configuration
        try {
            if (com.google.firebase.FirebaseApp.getApps(this).isEmpty()) {
                val appId = getString(R.string.google_app_id)
                val projectId = getString(R.string.project_id)
                val apiKey = getString(R.string.google_api_key)
                val gcmSenderId = getString(R.string.gcm_defaultSenderId)
                val options = com.google.firebase.FirebaseOptions.Builder()
                    .setApplicationId(appId)
                    .setProjectId(projectId)
                    .setApiKey(apiKey)
                    .setGcmSenderId(gcmSenderId)
                    .build()
                com.google.firebase.FirebaseApp.initializeApp(this, options)
            }
        } catch (e: Exception) {
            Log.w("GroupLinksApp", "Firebase init note: ${e.message}")
        }

        // Initialize local Room database
        database = AppDatabase.getDatabase(this)

        // Initialize Auth repository
        authRepository = AuthRepository(this)

        // Initialize Listing repository
        listingRepository = ListingRepository(this, database.historyDao(), authRepository)

        // Initialize Billing Manager
        billingManager = BillingManager(this)

        // Initialize AdMob and UMP
        AdManager.initialize(this)
    }

    companion object {
        lateinit var instance: GroupLinksApplication
            private set
    }
}
