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
