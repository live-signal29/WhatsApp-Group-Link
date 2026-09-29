package com.example.data.repository

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.example.data.local.HistoryDao
import com.example.data.local.HistoryEntity
import com.example.data.model.AppSettings
import com.example.data.model.CategoryItem
import com.example.data.model.ListingItem
import com.example.data.model.ListingStatus
import com.example.data.model.ListingType
import com.example.data.model.PromotionRecord
import com.example.data.model.PromotionStatus
import com.example.data.model.PurchaseRecord
import com.example.data.model.ReportRecord
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.UUID

class ListingRepository(
    private val context: Context,
    private val historyDao: HistoryDao,
    private val authRepository: AuthRepository
) {
    private val scope = CoroutineScope(Dispatchers.IO)
    private var firestore: FirebaseFirestore? = null

    // In-memory reactive state
    private val _listings = MutableStateFlow<List<ListingItem>>(emptyList())
    val listings: StateFlow<List<ListingItem>> = _listings.asStateFlow()

    private val _categories = MutableStateFlow<List<CategoryItem>>(emptyList())
    val categories: StateFlow<List<CategoryItem>> = _categories.asStateFlow()

    private val _promotions = MutableStateFlow<List<PromotionRecord>>(emptyList())
    val promotions: StateFlow<List<PromotionRecord>> = _promotions.asStateFlow()

    private val _reports = MutableStateFlow<List<ReportRecord>>(emptyList())
    val reports: StateFlow<List<ReportRecord>> = _reports.asStateFlow()

    private val _purchases = MutableStateFlow<List<PurchaseRecord>>(emptyList())
    val purchases: StateFlow<List<PurchaseRecord>> = _purchases.asStateFlow()

    private val _appSettings = MutableStateFlow(AppSettings())
    val appSettings: StateFlow<AppSettings> = _appSettings.asStateFlow()

    // Track listings viewed in this session to prevent spam incrementing
    private val sessionViewedListings = mutableSetOf<String>()

    private val baseCatalogListings: List<ListingItem> by lazy {
        ListingCatalogSeeder.generateFullCatalog()
    }

    init {
        // ALWAYS load base catalog with 30+ items per category immediately
        loadInitialData()

        try {
            if (FirebaseApp.getApps(context).isNotEmpty()) {
                firestore = FirebaseFirestore.getInstance()
                listenToFirestore()
            }
        } catch (e: Exception) {
            Log.w("ListingRepo", "Firestore init note: ${e.message}. Using offline catalog.")
        }
    }

    private fun loadInitialData() {
        val initialCategories = listOf(
            CategoryItem("cat_1", "News", "newspaper", 32),
            CategoryItem("cat_2", "Entertainment", "movie", 32),
            CategoryItem("cat_3", "Funny", "sentiment_very_satisfied", 32),
            CategoryItem("cat_4", "Poetry", "edit_note", 32),
            CategoryItem("cat_5", "Videos", "play_circle", 32),
            CategoryItem("cat_6", "Education", "school", 32),
            CategoryItem("cat_7", "Sports", "sports_soccer", 32),
            CategoryItem("cat_8", "Science", "science", 32),
            CategoryItem("cat_9", "Friendship", "diversity_3", 32),
            CategoryItem("cat_10", "Food", "restaurant", 32),
            CategoryItem("cat_11", "Crypto", "currency_bitcoin", 32),
            CategoryItem("cat_12", "Business", "business_center", 32)
        )
        _categories.value = initialCategories

        val now = System.currentTimeMillis()
        val threeDaysMs = 3 * 24 * 60 * 60 * 1000L

        val initialListings = listOf(
            // ==========================================
            // 1. NEWS (Groups & Channels)
            // ==========================================
            ListingItem(
                id = "list_news_1",
                ownerId = "owner_news_1",
                name = "Global Breaking News 24/7 📰",
                description = "Real-time international headlines, politics, weather, and breaking updates from verified sources.",
                imageUrl = "https://images.unsplash.com/photo-1504711434969-e33886168f5c?w=160&auto=format&fit=crop&q=80",
                whatsappLink = "https://chat.whatsapp.com/inviteNewsGlobal247",
                category = "News",
                type = ListingType.GROUP,
                status = ListingStatus.APPROVED,
                views = 12450,
                isPromoted = true,
                promotionStart = now - 3600000L,
                promotionEnd = now + threeDaysMs,
                createdAt = now - 7200000L
            ),
            ListingItem(
                id = "list_news_2",
                ownerId = "owner_news_2",
                name = "Daily Hindi & Urdu Headlines 🗞️",
                description = "Subah ki taaza khabrein, desh-videsh ki mukhy samachar aur viral public updates.",
                imageUrl = "https://images.unsplash.com/photo-1585829365295-ab7cd400c167?w=160&auto=format&fit=crop&q=80",
                whatsappLink = "https://chat.whatsapp.com/inviteDailyHindiSamachar",
                category = "News",
                type = ListingType.GROUP,
                status = ListingStatus.APPROVED,
                views = 8920,
                isPromoted = false,
                createdAt = now - 14400000L
            ),
            ListingItem(
                id = "list_news_chan_1",
                ownerId = "owner_news_3",
                name = "World Geopolitics & Defense News 🌐",
                description = "Official channel for deep analysis of military, economy, and foreign affairs.",
                imageUrl = "https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=160&auto=format&fit=crop&q=80",
                whatsappLink = "https://whatsapp.com/channel/0029VaWorldGeopolitics",
                category = "News",
                type = ListingType.CHANNEL,
                status = ListingStatus.APPROVED,
                views = 15300,
                isPromoted = true,
                promotionStart = now - 1200000L,
                promotionEnd = now + threeDaysMs,
                createdAt = now - 36000000L
            ),
            ListingItem(
                id = "list_news_chan_2",
                ownerId = "owner_news_4",
                name = "Live Weather Radar & City Alerts ⚡",
                description = "Storm tracking, monsoon rainfall forecasts, temperature alerts, and climate bulletins.",
                imageUrl = "https://images.unsplash.com/photo-1592210454359-9043f067919b?w=160&auto=format&fit=crop&q=80",
                whatsappLink = "https://whatsapp.com/channel/0029VaWeatherRadarAlerts",
                category = "News",
                type = ListingType.CHANNEL,
                status = ListingStatus.APPROVED,
                views = 6400,
                isPromoted = false,
                createdAt = now - 48000000L
            ),

            // ==========================================
            // 2. ENTERTAINMENT
            // ==========================================
            ListingItem(
                id = "list_ent_1",
                ownerId = "owner_ent_1",
                name = "Bollywood & Hollywood Mania 🌟",
                description = "First-day box office collections, upcoming teaser trailers, and celebrity interviews.",
                imageUrl = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=160&auto=format&fit=crop&q=80",
                whatsappLink = "https://chat.whatsapp.com/inviteCinemaManiaHub",
                category = "Entertainment",
                type = ListingType.GROUP,
                status = ListingStatus.APPROVED,
                views = 11200,
                isPromoted = true,
                promotionStart = now - 2000000L,
                promotionEnd = now + threeDaysMs,
                createdAt = now - 5000000L
            ),
            ListingItem(
                id = "list_ent_2",
                ownerId = "owner_ent_2",
                name = "OTT Web Series & Netflix Binge 🍿",
                description = "Best binge-worthy series recommendations, IMDb top ratings, and hidden indie film gems.",
                imageUrl = "https://images.unsplash.com/photo-1574375927938-d5a98e8ffe85?w=160&auto=format&fit=crop&q=80",
                whatsappLink = "https://chat.whatsapp.com/inviteOttSeriesDiscussion",
                category = "Entertainment",
                type = ListingType.GROUP,
                status = ListingStatus.APPROVED,
                views = 9400,
                isPromoted = false,
                createdAt = now - 22000000L
            ),
            ListingItem(
                id = "list_ent_chan_1",
                ownerId = "owner_ent_3",
                name = "Movie Trailers & Cinema Hub 🎬",
                description = "Hollywood and Bollywood HD trailers, release dates, and theatrical posters.",
                imageUrl = "https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?w=160&auto=format&fit=crop&q=80",
                whatsappLink = "https://whatsapp.com/channel/0029VaCinemaTrailersHub",
                category = "Entertainment",
                type = ListingType.CHANNEL,
                status = ListingStatus.APPROVED,
                views = 28900,
                isPromoted = true,
                promotionStart = now - 1800000L,
                promotionEnd = now + threeDaysMs,
                createdAt = now - 40000000L
            ),
            ListingItem(
                id = "list_ent_chan_2",
                ownerId = "owner_ent_4",
                name = "Celebrity Gossips & Red Carpet 📸",
                description = "Daily glamorous photoshoot updates, airport looks, and spicy industry buzz.",
                imageUrl = "https://images.unsplash.com/photo-1492684223066-81342ee5ff30?w=160&auto=format&fit=crop&q=80",
                whatsappLink = "https://whatsapp.com/channel/0029VaCelebrityGossipLive",
                category = "Entertainment",
                type = ListingType.CHANNEL,
                status = ListingStatus.APPROVED,
                views = 14200,
                isPromoted = false,
                createdAt = now - 52000000L
            ),

            // ==========================================
            // 3. FUNNY & COMEDY
            // ==========================================
            ListingItem(
                id = "list_funny_1",
                ownerId = "owner_funny_1",
                name = "Funny Memes & Viral Reels 🎭",
                description = "Laugh out loud every hour! Top hilarious comedy memes, relatable reels, and clips.",
                imageUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=160&auto=format&fit=crop&q=80",
                whatsappLink = "https://chat.whatsapp.com/inviteViralComedyMemes",
                category = "Funny",
                type = ListingType.GROUP,
                status = ListingStatus.APPROVED,
                views = 19400,
                isPromoted = true,
                promotionStart = now - 1500000L,
                promotionEnd = now + threeDaysMs,
                createdAt = now - 8000000L
            ),
            ListingItem(
                id = "list_funny_2",
                ownerId = "owner_funny_2",
                name = "Non-Stop Jokes & Chutkule 😂",
                description = "Majedaar Hindi jokes, savage comeback lines, aur family-friendly comedy jokes.",
                imageUrl = "https://images.unsplash.com/photo-1527224857830-43a7acc85260?w=160&auto=format&fit=crop&q=80",
                whatsappLink = "https://chat.whatsapp.com/inviteDesiChutkuleHub",
                category = "Funny",
                type = ListingType.GROUP,
                status = ListingStatus.APPROVED,
                views = 7800,
                isPromoted = false,
                createdAt = now - 19000000L
            ),
            ListingItem(
                id = "list_funny_chan_1",
                ownerId = "owner_funny_3",
                name = "Desi Bakchodi & Memers Hub 🤪",
                description = "Daily dose of relatable engineering, college, and trending desi memes.",
                imageUrl = "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=160&auto=format&fit=crop&q=80",
                whatsappLink = "https://whatsapp.com/channel/0029VaDesiMemersChannel",
                category = "Funny",
                type = ListingType.CHANNEL,
                status = ListingStatus.APPROVED,
                views = 34500,
                isPromoted = true,
                promotionStart = now - 900000L,
                promotionEnd = now + threeDaysMs,
                createdAt = now - 28000000L
            ),
            ListingItem(
                id = "list_funny_chan_2",
                ownerId = "owner_funny_4",
                name = "Funny Animals & Cute Pets 🐾",
                description = "Hilarious dogs, cats doing funny tricks, and wholesome pet videos.",
                imageUrl = "https://images.unsplash.com/photo-1543466835-00a7907e9de1?w=160&auto=format&fit=crop&q=80",
                whatsappLink = "https://whatsapp.com/channel/0029VaFunnyPetsWorld",
                category = "Funny",
                type = ListingType.CHANNEL,
                status = ListingStatus.APPROVED,
                views = 12100,
                isPromoted = false,
                createdAt = now - 44000000L
            ),

            // ==========================================
            // 4. POETRY & SHAYARI
            // ==========================================
            ListingItem(
                id = "list_poetry_1",
                ownerId = "owner_poet_1",
                name = "Urdu Shayari & Ghazal Lovers 📜",
                description = "Mirza Ghalib, Faiz, Jaun Elia, aur modern heart-touching sher-o-shayari collection.",
                imageUrl = "https://images.unsplash.com/photo-1455390582262-044cdead277a?w=160&auto=format&fit=crop&q=80",
                whatsappLink = "https://chat.whatsapp.com/inviteUrduShayariLovers",
                category = "Poetry",
                type = ListingType.GROUP,
                status = ListingStatus.APPROVED,
                views = 8400,
                isPromoted = true,
                promotionStart = now - 3000000L,
                promotionEnd = now + threeDaysMs,
                createdAt = now - 12000000L
            ),
            ListingItem(
                id = "list_poetry_2",
                ownerId = "owner_poet_2",
                name = "Hindi Kavita & Sahitya Sangam ✍️",
                description = "Kavi sammelan, prem kavita, prerak panktiyan aur sahitya premion ka mulyavan manch.",
                imageUrl = "https://images.unsplash.com/photo-1471107340929-a87cd0f5b5f3?w=160&auto=format&fit=crop&q=80",
                whatsappLink = "https://chat.whatsapp.com/inviteHindiKavitaSangam",
                category = "Poetry",
                type = ListingType.GROUP,
                status = ListingStatus.APPROVED,
                views = 5600,
                isPromoted = false,
                createdAt = now - 25000000L
            ),
            ListingItem(
                id = "list_poetry_chan_1",
                ownerId = "owner_poet_3",
                name = "Deep Romantic & Sad Quotes 💔",
                description = "Aesthetic written quotes for WhatsApp status, late night thoughts, and feelings.",
                imageUrl = "https://images.unsplash.com/photo-1518495973542-4542c06a5843?w=160&auto=format&fit=crop&q=80",
                whatsappLink = "https://whatsapp.com/channel/0029VaRomanticQuotesOfficial",
                category = "Poetry",
                type = ListingType.CHANNEL,
                status = ListingStatus.APPROVED,
                views = 18900,
                isPromoted = true,
                promotionStart = now - 400000L,
                promotionEnd = now + threeDaysMs,
                createdAt = now - 31000000L
            ),
            ListingItem(
                id = "list_poetry_chan_2",
                ownerId = "owner_poet_4",
                name = "Sufi Wisdom & Rumi Lines 🕊️",
                description = "Spiritual sufi quotes, Rumi, Shams Tabrizi, and peaceful life reflections.",
                imageUrl = "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=160&auto=format&fit=crop&q=80",
                whatsappLink = "https://whatsapp.com/channel/0029VaSufiWisdomRumi",
                category = "Poetry",
                type = ListingType.CHANNEL,
                status = ListingStatus.APPROVED,
                views = 11400,
                isPromoted = false,
                createdAt = now - 60000000L
            ),

            // ==========================================
            // 5. VIDEOS & REELS
            // ==========================================
            ListingItem(
                id = "list_vid_1",
                ownerId = "owner_vid_1",
                name = "Trending 4K WhatsApp Status 📱",
                description = "Full screen HD status videos, sad lyrical clips, love songs, and trending BGM.",
                imageUrl = "https://images.unsplash.com/photo-1492691527719-9d1e07e534b4?w=160&auto=format&fit=crop&q=80",
                whatsappLink = "https://chat.whatsapp.com/inviteTrendingStatus4K",
                category = "Videos",
                type = ListingType.GROUP,
                status = ListingStatus.APPROVED,
                views = 16700,
                isPromoted = true,
                promotionStart = now - 2200000L,
                promotionEnd = now + threeDaysMs,
                createdAt = now - 6000000L
            ),
            ListingItem(
                id = "list_vid_2",
                ownerId = "owner_vid_2",
                name = "Nature & Drone Cinematics 4K 🌲",
                description = "Relaxing aerial videos, mountain waterfalls, sunsets, and ambient nature sounds.",
                imageUrl = "https://images.unsplash.com/photo-1470071459604-3b5ec3a7fe05?w=160&auto=format&fit=crop&q=80",
                whatsappLink = "https://chat.whatsapp.com/inviteNatureDroneClips",
                category = "Videos",
                type = ListingType.GROUP,
                status = ListingStatus.APPROVED,
                views = 7200,
                isPromoted = false,
                createdAt = now - 18000000L
            ),
            ListingItem(
                id = "list_vid_chan_1",
                ownerId = "owner_vid_3",
                name = "Viral Shorts & TikTok Edits 🎥",
                description = "Daily viral compilations, crazy stunts, magic tricks, and life hacks video feed.",
                imageUrl = "https://images.unsplash.com/photo-1536240478700-b869070f9279?w=160&auto=format&fit=crop&q=80",
                whatsappLink = "https://whatsapp.com/channel/0029VaViralShortsDaily",
                category = "Videos",
                type = ListingType.CHANNEL,
                status = ListingStatus.APPROVED,
                views = 31200,
                isPromoted = true,
                promotionStart = now - 800000L,
                promotionEnd = now + threeDaysMs,
                createdAt = now - 27000000L
            ),
            ListingItem(
                id = "list_vid_chan_2",
                ownerId = "owner_vid_4",
                name = "CapCut & Video Editing Hub ✂️",
                description = "Free CapCut templates, Premiere Pro presets, sound effects, and transitions.",
                imageUrl = "https://images.unsplash.com/photo-1574717024653-61fd2cf4d44d?w=160&auto=format&fit=crop&q=80",
                whatsappLink = "https://whatsapp.com/channel/0029VaCapCutTemplatesHub",
                category = "Videos",
                type = ListingType.CHANNEL,
                status = ListingStatus.APPROVED,
                views = 14300,
                isPromoted = false,
                createdAt = now - 50000000L
            ),

            // ==========================================
            // 6. EDUCATION
            // ==========================================
            ListingItem(
                id = "list_edu_1",
                ownerId = "owner_edu_1",
                name = "Daily Current Affairs & General Knowledge 📚",
                description = "UPSC, SSC, Railway, aur banking exams ke liye best hand-written notes aur quizzes.",
                imageUrl = "https://images.unsplash.com/photo-1503676260728-1c00da094a0b?w=160&auto=format&fit=crop&q=80",
                whatsappLink = "https://chat.whatsapp.com/inviteDailyGkCurrentAffairs",
                category = "Education",
                type = ListingType.GROUP,
                status = ListingStatus.APPROVED,
                views = 15600,
                isPromoted = true,
                promotionStart = now - 2400000L,
                promotionEnd = now + threeDaysMs,
                createdAt = now - 7500000L
            ),
            ListingItem(
                id = "list_edu_2",
                ownerId = "owner_edu_2",
                name = "Fluent English Speaking Club 🗣️",
                description = "Daily vocabulary words, grammar rules, idioms, and voice room speaking practice.",
                imageUrl = "https://images.unsplash.com/photo-1544717305-2782549b5136?w=160&auto=format&fit=crop&q=80",
                whatsappLink = "https://chat.whatsapp.com/inviteEnglishSpeakingClub",
                category = "Education",
                type = ListingType.GROUP,
                status = ListingStatus.APPROVED,
                views = 10800,
                isPromoted = false,
                createdAt = now - 16000000L
            ),
            ListingItem(
                id = "list_edu_chan_1",
                ownerId = "owner_edu_3",
                name = "Python & Web Developers Code 💻",
                description = "Learn Python, JavaScript, React, AI prompts, and free certified course links.",
                imageUrl = "https://images.unsplash.com/photo-1526374965328-7f61d4dc18c5?w=160&auto=format&fit=crop&q=80",
                whatsappLink = "https://whatsapp.com/channel/0029VaPythonDevsOfficial",
                category = "Education",
                type = ListingType.CHANNEL,
                status = ListingStatus.APPROVED,
                views = 27400,
                isPromoted = true,
                promotionStart = now - 1100000L,
                promotionEnd = now + threeDaysMs,
                createdAt = now - 35000000L
            ),
            ListingItem(
                id = "list_edu_chan_2",
                ownerId = "owner_edu_4",
                name = "Govt Job Alerts & Notifications 📢",
                description = "Instant notifications for Central, State government recruitment exams and admit cards.",
                imageUrl = "https://images.unsplash.com/photo-1454165804606-c3d57bc86b40?w=160&auto=format&fit=crop&q=80",
                whatsappLink = "https://whatsapp.com/channel/0029VaGovtJobAlertsInstant",
                category = "Education",
                type = ListingType.CHANNEL,
                status = ListingStatus.APPROVED,
                views = 21500,
                isPromoted = false,
                createdAt = now - 55000000L
            ),

            // ==========================================
            // 7. SPORTS
            // ==========================================
            ListingItem(
                id = "list_sports_1",
                ownerId = "owner_sport_1",
                name = "Cricket Mania & Live IPL Updates 🏏",
                description = "Ball-by-ball scores, playing XI predictions, toss news, and match highlights.",
                imageUrl = "https://images.unsplash.com/photo-1540747913346-19e32dc3e97e?w=160&auto=format&fit=crop&q=80",
                whatsappLink = "https://chat.whatsapp.com/inviteCricketManiaLive",
                category = "Sports",
                type = ListingType.GROUP,
                status = ListingStatus.APPROVED,
                views = 18700,
                isPromoted = true,
                promotionStart = now - 1900000L,
                promotionEnd = now + threeDaysMs,
                createdAt = now - 6200000L
            ),
            ListingItem(
                id = "list_sports_2",
                ownerId = "owner_sport_2",
                name = "Gym Motivation & Fitness Diet 💪",
                description = "Bodybuilding workout routines, high-protein vegetarian/non-veg diet plans.",
                imageUrl = "https://images.unsplash.com/photo-1517838277536-f5f99be501cd?w=160&auto=format&fit=crop&q=80",
                whatsappLink = "https://chat.whatsapp.com/inviteGymFitnessMotivation",
                category = "Sports",
                type = ListingType.GROUP,
                status = ListingStatus.APPROVED,
                views = 9300,
                isPromoted = false,
                createdAt = now - 21000000L
            ),
            ListingItem(
                id = "list_sports_chan_1",
                ownerId = "owner_sport_3",
                name = "Football & Premier League Live ⚽",
                description = "Champions League, transfer market breaking news, Fabrizio Romano alerts, and tables.",
                imageUrl = "https://images.unsplash.com/photo-1508098682722-e99c43a406b2?w=160&auto=format&fit=crop&q=80",
                whatsappLink = "https://whatsapp.com/channel/0029VaFootballPremierLive",
                category = "Sports",
                type = ListingType.CHANNEL,
                status = ListingStatus.APPROVED,
                views = 32800,
                isPromoted = true,
                promotionStart = now - 600000L,
                promotionEnd = now + threeDaysMs,
                createdAt = now - 38000000L
            ),
            ListingItem(
                id = "list_sports_chan_2",
                ownerId = "owner_sport_4",
                name = "UFC & Boxing Fight Night 🥊",
                description = "UFC pay-per-view fight schedules, knockout highlights, weigh-in videos, and press conferences.",
                imageUrl = "https://images.unsplash.com/photo-1517649763962-0c623266ddc0?w=160&auto=format&fit=crop&q=80",
                whatsappLink = "https://whatsapp.com/channel/0029VaUfcCombatSportsLive",
                category = "Sports",
                type = ListingType.CHANNEL,
                status = ListingStatus.APPROVED,
                views = 12900,
                isPromoted = false,
                createdAt = now - 58000000L
            ),

            // ==========================================
            // 8. SCIENCE & TECH
            // ==========================================
            ListingItem(
                id = "list_sci_1",
                ownerId = "owner_sci_1",
                name = "NASA, Space & Astronomy Club 🌌",
                description = "James Webb telescope images, black hole discoveries, SpaceX rocket launches, and celestial events.",
                imageUrl = "https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=160&auto=format&fit=crop&q=80",
                whatsappLink = "https://chat.whatsapp.com/inviteSpaceAstronomyClub",
                category = "Science",
                type = ListingType.GROUP,
                status = ListingStatus.APPROVED,
                views = 11900,
                isPromoted = true,
                promotionStart = now - 2700000L,
                promotionEnd = now + threeDaysMs,
                createdAt = now - 8500000L
            ),
            ListingItem(
                id = "list_sci_2",
                ownerId = "owner_sci_2",
                name = "Mobile Android Tricks & Hidden Hacks 📱",
                description = "Battery saving tips, developer options hacks, secret dial codes, and customized APKs.",
                imageUrl = "https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=160&auto=format&fit=crop&q=80",
                whatsappLink = "https://chat.whatsapp.com/inviteAndroidTricksHacks",
                category = "Science",
                type = ListingType.GROUP,
                status = ListingStatus.APPROVED,
                views = 8600,
                isPromoted = false,
                createdAt = now - 23000000L
            ),
            ListingItem(
                id = "list_sci_chan_1",
                ownerId = "owner_sci_3",
                name = "Tech Gadgets & AI Daily ⚡",
                description = "Official channel for smartphone leaks, ChatGPT/Gemini prompts, GPUs, and tech discounts.",
                imageUrl = "https://images.unsplash.com/photo-1518770660439-4636190af475?w=160&auto=format&fit=crop&q=80",
                whatsappLink = "https://whatsapp.com/channel/0029VaTechGadgetsAiDaily",
                category = "Science",
                type = ListingType.CHANNEL,
                status = ListingStatus.APPROVED,
                views = 41200,
                isPromoted = true,
                promotionStart = now - 1400000L,
                promotionEnd = now + threeDaysMs,
                createdAt = now - 42000000L
            ),
            ListingItem(
                id = "list_sci_chan_2",
                ownerId = "owner_sci_4",
                name = "Future Science & Robotics 🤖",
                description = "Humanoid robots, quantum computing, renewable energy breakthroughs, and biotech.",
                imageUrl = "https://images.unsplash.com/photo-1485827404703-89b55fcc595e?w=160&auto=format&fit=crop&q=80",
                whatsappLink = "https://whatsapp.com/channel/0029VaRoboticsFutureTech",
                category = "Science",
                type = ListingType.CHANNEL,
                status = ListingStatus.APPROVED,
                views = 15700,
                isPromoted = false,
                createdAt = now - 62000000L
            ),

            // ==========================================
            // 9. FRIENDSHIP & COMMUNITY
            // ==========================================
            ListingItem(
                id = "list_fri_1",
                ownerId = "owner_fri_1",
                name = "Chill Friends Worldwide Adda ☕",
                description = "Friendly talk, voice chats, weekend gaming, sharing music playlists, and making genuine friends.",
                imageUrl = "https://images.unsplash.com/photo-1529156069898-49953e39b3ac?w=160&auto=format&fit=crop&q=80",
                whatsappLink = "https://chat.whatsapp.com/inviteChillFriendsWorldwide",
                category = "Friendship",
                type = ListingType.GROUP,
                status = ListingStatus.APPROVED,
                views = 23400,
                isPromoted = true,
                promotionStart = now - 3200000L,
                promotionEnd = now + threeDaysMs,
                createdAt = now - 9000000L
            ),
            ListingItem(
                id = "list_fri_2",
                ownerId = "owner_fri_2",
                name = "Late Night Talks & Musings 🌙",
                description = "A safe cozy space for late night thinkers, poetry recitals, calm talk, and peaceful music.",
                imageUrl = "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=160&auto=format&fit=crop&q=80",
                whatsappLink = "https://chat.whatsapp.com/inviteLateNightTalksClub",
                category = "Friendship",
                type = ListingType.GROUP,
                status = ListingStatus.APPROVED,
                views = 12600,
                isPromoted = false,
                createdAt = now - 18000000L
            ),
            ListingItem(
                id = "list_fri_chan_1",
                ownerId = "owner_fri_3",
                name = "Daily Positive Vibes & Affirmations ✨",
                description = "Morning motivation, mental peace tips, self-love affirmations, and calming quotes.",
                imageUrl = "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=160&auto=format&fit=crop&q=80",
                whatsappLink = "https://whatsapp.com/channel/0029VaPositiveVibesDaily",
                category = "Friendship",
                type = ListingType.CHANNEL,
                status = ListingStatus.APPROVED,
                views = 29500,
                isPromoted = true,
                promotionStart = now - 1700000L,
                promotionEnd = now + threeDaysMs,
                createdAt = now - 33000000L
            ),
            ListingItem(
                id = "list_fri_chan_2",
                ownerId = "owner_fri_4",
                name = "Gamers & Discord Community 🎮",
                description = "BGMI, Free Fire, Valorant, GTA 5 squads, tournament dates, and gaming clips.",
                imageUrl = "https://images.unsplash.com/photo-1538481199705-c710c4e965fc?w=160&auto=format&fit=crop&q=80",
                whatsappLink = "https://whatsapp.com/channel/0029VaGamersCommunitySquad",
                category = "Friendship",
                type = ListingType.CHANNEL,
                status = ListingStatus.APPROVED,
                views = 18200,
                isPromoted = false,
                createdAt = now - 54000000L
            ),

            // ==========================================
            // 10. FOOD & COOKING
            // ==========================================
            ListingItem(
                id = "list_food_1",
                ownerId = "owner_food_1",
                name = "Street Food Lovers & Zaika 🥘",
                description = "Famous street food joints, roadside hidden dhabas, reviews, and spicy biryani recipes.",
                imageUrl = "https://images.unsplash.com/photo-1504674900247-0877df9cc836?w=160&auto=format&fit=crop&q=80",
                whatsappLink = "https://chat.whatsapp.com/inviteStreetFoodZaikaClub",
                category = "Food",
                type = ListingType.GROUP,
                status = ListingStatus.APPROVED,
                views = 11400,
                isPromoted = true,
                promotionStart = now - 2100000L,
                promotionEnd = now + threeDaysMs,
                createdAt = now - 8100000L
            ),
            ListingItem(
                id = "list_food_2",
                ownerId = "owner_food_2",
                name = "Baking, Cakes & Desserts 🍰",
                description = "Oven-free cake recipes, chocolates, cookies, pastries, and masterchef decorating tips.",
                imageUrl = "https://images.unsplash.com/photo-1578985545062-69928b1d9587?w=160&auto=format&fit=crop&q=80",
                whatsappLink = "https://chat.whatsapp.com/inviteBakingCakesMastery",
                category = "Food",
                type = ListingType.GROUP,
                status = ListingStatus.APPROVED,
                views = 7600,
                isPromoted = false,
                createdAt = now - 20000000L
            ),
            ListingItem(
                id = "list_food_chan_1",
                ownerId = "owner_food_3",
                name = "Quick 10-Minute Recipe Hacks 🍳",
                description = "Easy breakfast ideas, instant snacks, bachelor cooking tricks, and lunchbox recipes.",
                imageUrl = "https://images.unsplash.com/photo-1498837167922-ddd27525d352?w=160&auto=format&fit=crop&q=80",
                whatsappLink = "https://whatsapp.com/channel/0029VaQuickRecipeHacks",
                category = "Food",
                type = ListingType.CHANNEL,
                status = ListingStatus.APPROVED,
                views = 24100,
                isPromoted = true,
                promotionStart = now - 1300000L,
                promotionEnd = now + threeDaysMs,
                createdAt = now - 36000000L
            ),
            ListingItem(
                id = "list_food_chan_2",
                ownerId = "owner_food_4",
                name = "Desi Shahi Pakwan & Biryani 🍲",
                description = "Royal Mughlai recipes, authentic spices, Hyderabadi dum biryani, and korma guides.",
                imageUrl = "https://images.unsplash.com/photo-1563379091339-03b21ab4a4f8?w=160&auto=format&fit=crop&q=80",
                whatsappLink = "https://whatsapp.com/channel/0029VaDesiShahiPakwan",
                category = "Food",
                type = ListingType.CHANNEL,
                status = ListingStatus.APPROVED,
                views = 13800,
                isPromoted = false,
                createdAt = now - 59000000L
            ),

            // ==========================================
            // 11. CRYPTO & FOREX
            // ==========================================
            ListingItem(
                id = "list_crypto_1",
                ownerId = "owner_cryp_1",
                name = "Crypto Signal & Trading 🚀",
                description = "Daily profitable crypto calls, Bitcoin analysis, spot gem signals, and risk management.",
                imageUrl = "https://images.unsplash.com/photo-1622979135225-d2ba269bc1df?w=160&auto=format&fit=crop&q=80",
                whatsappLink = "https://chat.whatsapp.com/inviteCryptoVipTradingSignals",
                category = "Crypto",
                type = ListingType.GROUP,
                status = ListingStatus.APPROVED,
                views = 19800,
                isPromoted = true,
                promotionStart = now - 3500000L,
                promotionEnd = now + threeDaysMs,
                createdAt = now - 7200000L
            ),
            ListingItem(
                id = "list_crypto_2",
                ownerId = "owner_cryp_2",
                name = "Forex & Stock Market Bull 📈",
                description = "EUR/USD, Gold XAUUSD charts, technical indicators, and price action trade setups.",
                imageUrl = "https://images.unsplash.com/photo-1611974789855-9c2a0a7236a3?w=160&auto=format&fit=crop&q=80",
                whatsappLink = "https://chat.whatsapp.com/inviteForexStockMarketBull",
                category = "Crypto",
                type = ListingType.GROUP,
                status = ListingStatus.APPROVED,
                views = 10400,
                isPromoted = false,
                createdAt = now - 17000000L
            ),
            ListingItem(
                id = "list_crypto_chan_1",
                ownerId = "owner_cryp_3",
                name = "Bitcoin & Altcoin 100x Gems 💎",
                description = "Early token launches, Binance listing alerts, airdrop guides, and on-chain whale tracking.",
                imageUrl = "https://images.unsplash.com/photo-1621416894569-0f39ed31d247?w=160&auto=format&fit=crop&q=80",
                whatsappLink = "https://whatsapp.com/channel/0029VaBitcoinGemCallsOfficial",
                category = "Crypto",
                type = ListingType.CHANNEL,
                status = ListingStatus.APPROVED,
                views = 38900,
                isPromoted = true,
                promotionStart = now - 750000L,
                promotionEnd = now + threeDaysMs,
                createdAt = now - 41000000L
            ),
            ListingItem(
                id = "list_crypto_chan_2",
                ownerId = "owner_cryp_4",
                name = "Airdrop Hunters & Free Testnets 🪂",
                description = "Claim zero-investment potential airdrops, testnet task walk-throughs, and faucet links.",
                imageUrl = "https://images.unsplash.com/photo-1639762681485-074b7f938ba0?w=160&auto=format&fit=crop&q=80",
                whatsappLink = "https://whatsapp.com/channel/0029VaAirdropHuntersCrypto",
                category = "Crypto",
                type = ListingType.CHANNEL,
                status = ListingStatus.APPROVED,
                views = 17600,
                isPromoted = false,
                createdAt = now - 63000000L
            ),

            // ==========================================
            // 12. BUSINESS & STARTUPS
            // ==========================================
            ListingItem(
                id = "list_biz_1",
                ownerId = "owner_biz_1",
                name = "E-Commerce & Startup Founders 💼",
                description = "Networking club for entrepreneurs, dropshippers, Amazon sellers, and small business owners.",
                imageUrl = "https://images.unsplash.com/photo-1556761175-5973dc0f32e7?w=160&auto=format&fit=crop&q=80",
                whatsappLink = "https://chat.whatsapp.com/inviteStartupFoundersClub",
                category = "Business",
                type = ListingType.GROUP,
                status = ListingStatus.APPROVED,
                views = 14300,
                isPromoted = true,
                promotionStart = now - 2800000L,
                promotionEnd = now + threeDaysMs,
                createdAt = now - 8800000L
            ),
            ListingItem(
                id = "list_biz_2",
                ownerId = "owner_biz_2",
                name = "Import Export & B2B Wholesale 🌐",
                description = "Direct factory manufacturers, bulk wholesale supplies, customs clearance guidance.",
                imageUrl = "https://images.unsplash.com/photo-1578575437130-527eed3abbec?w=160&auto=format&fit=crop&q=80",
                whatsappLink = "https://chat.whatsapp.com/inviteImportExportWholesale",
                category = "Business",
                type = ListingType.GROUP,
                status = ListingStatus.APPROVED,
                views = 8900,
                isPromoted = false,
                createdAt = now - 24000000L
            ),
            ListingItem(
                id = "list_biz_chan_1",
                ownerId = "owner_biz_3",
                name = "Digital Marketing & Freelance Clients 💰",
                description = "High-ticket client acquisition, SEO hacks, Meta Ads blueprint, and remote freelance jobs.",
                imageUrl = "https://images.unsplash.com/photo-1460925895917-afdab827c52f?w=160&auto=format&fit=crop&q=80",
                whatsappLink = "https://whatsapp.com/channel/0029VaDigitalMarketingAgency",
                category = "Business",
                type = ListingType.CHANNEL,
                status = ListingStatus.APPROVED,
                views = 31500,
                isPromoted = true,
                promotionStart = now - 950000L,
                promotionEnd = now + threeDaysMs,
                createdAt = now - 37000000L
            ),
            ListingItem(
                id = "list_biz_chan_2",
                ownerId = "owner_biz_4",
                name = "Real Estate & Commercial Deals 🏢",
                description = "Verified residential plots, commercial investment properties, rental yields, and legal docs.",
                imageUrl = "https://images.unsplash.com/photo-1560518883-ce09059eeffa?w=160&auto=format&fit=crop&q=80",
                whatsappLink = "https://whatsapp.com/channel/0029VaRealEstateDealsClub",
                category = "Business",
                type = ListingType.CHANNEL,
                status = ListingStatus.APPROVED,
                views = 15200,
                isPromoted = false,
                createdAt = now - 65000000L
            )
        )
        _listings.value = baseCatalogListings
    }

    private fun listenToFirestore() {
        val db = firestore ?: return
        db.collection("listings")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w("ListingRepo", "Firestore error: ${error.message}")
                    return@addSnapshotListener
                }
                if (snapshot != null && !snapshot.isEmpty) {
                    val remoteItems = snapshot.documents.mapNotNull { doc ->
                        doc.data?.let { ListingItem.fromMap(doc.id, it) }
                    }
                    if (remoteItems.isNotEmpty()) {
                        val remoteIds = remoteItems.map { it.id }.toSet()
                        val remoteLinks = remoteItems.map {
                            com.example.utils.LinkMetadataFetcher.normalizeLinkForDuplicateCheck(it.whatsappLink)
                        }.toSet()

                        // Preserve base catalog with 30+ items per category, merging remote additions on top!
                        val preservedBase = baseCatalogListings.filter {
                            it.id !in remoteIds &&
                            com.example.utils.LinkMetadataFetcher.normalizeLinkForDuplicateCheck(it.whatsappLink) !in remoteLinks
                        }
                        _listings.value = remoteItems + preservedBase
                    }
                }
            }

        db.collection("categories").addSnapshotListener { snapshot, error ->
            if (snapshot != null && !snapshot.isEmpty) {
                val items = snapshot.documents.mapNotNull { doc ->
                    doc.data?.let { CategoryItem.fromMap(doc.id, it) }
                }
                if (items.isNotEmpty()) {
                    _categories.value = items
                }
            }
        }
    }

    // ==========================================
    // FILTERED & SORTED LISTINGS
    // ==========================================

    fun getFilteredListings(type: ListingType, category: String? = null, searchQuery: String = ""): List<ListingItem> {
        val now = System.currentTimeMillis()
        val allApproved = _listings.value.filter { it.status == ListingStatus.APPROVED }

        return allApproved
            .filter { it.type == type }
            .filter { category == null || it.category.equals(category, ignoreCase = true) }
            .filter {
                searchQuery.isBlank() ||
                        it.name.contains(searchQuery, ignoreCase = true) ||
                        it.description.contains(searchQuery, ignoreCase = true) ||
                        it.category.contains(searchQuery, ignoreCase = true)
            }
            .sortedWith(
                compareByDescending<ListingItem> { it.isPromoted && (it.promotionEnd == 0L || now <= it.promotionEnd) }
                    .thenByDescending { it.createdAt }
            )
    }

    // ==========================================
    // WHATSAPP LINK VALIDATION
    // ==========================================

    fun validateWhatsAppLink(url: String, type: ListingType): ValidationResult {
        val trimmed = url.trim()
        if (trimmed.isEmpty()) {
            return ValidationResult.Error("Please enter a link.")
        }

        // Security check for malicious schemes
        val lower = trimmed.lowercase(Locale.ROOT)
        if (lower.startsWith("javascript:") || lower.startsWith("file:") || lower.startsWith("content:") || lower.startsWith("data:")) {
            return ValidationResult.Error("Invalid or unsafe URL protocol.")
        }

        if (!lower.startsWith("https://")) {
            return ValidationResult.Error("Link must start with https://")
        }

        return when (type) {
            ListingType.GROUP -> {
                if (lower.startsWith("https://chat.whatsapp.com/") && trimmed.length > 25) {
                    ValidationResult.Success(trimmed)
                } else {
                    ValidationResult.Error("Must be a valid WhatsApp Group link (e.g. https://chat.whatsapp.com/...)")
                }
            }
            ListingType.CHANNEL -> {
                if ((lower.startsWith("https://whatsapp.com/channel/") || lower.startsWith("https://www.whatsapp.com/channel/")) && trimmed.length > 29) {
                    ValidationResult.Success(trimmed)
                } else {
                    ValidationResult.Error("Must be a valid WhatsApp Channel link (e.g. https://whatsapp.com/channel/...)")
                }
            }
        }
    }

    // Check duplicate link using normalized WhatsApp URL
    fun isDuplicateLink(link: String): Boolean {
        val normalized = com.example.utils.LinkMetadataFetcher.normalizeLinkForDuplicateCheck(link)
        if (normalized.length < 5) return false
        return _listings.value.any {
            it.status != ListingStatus.DELETED &&
                    com.example.utils.LinkMetadataFetcher.normalizeLinkForDuplicateCheck(it.whatsappLink) == normalized
        }
    }

    // Check blocked keywords
    fun containsBlockedKeywords(text: String): Boolean {
        val blocked = _appSettings.value.blockedKeywords
        val lower = text.lowercase(Locale.ROOT)
        return blocked.any { lower.contains(it.lowercase(Locale.ROOT)) }
    }

    // ==========================================
    // JOIN / FOLLOW & HISTORY & VIEWS
    // ==========================================

    fun openWhatsAppLink(item: ListingItem) {
        val link = item.whatsappLink
        if (link.isBlank()) return

        // 1. Increment view count safely if not viewed in this session
        if (!sessionViewedListings.contains(item.id)) {
            sessionViewedListings.add(item.id)
            incrementViews(item.id)
        }

        // 2. Record to local History (Room DB)
        scope.launch {
            try {
                historyDao.insertOrUpdate(
                    HistoryEntity(
                        listingId = item.id,
                        name = item.name,
                        category = item.category,
                        type = item.type.name,
                        imageUrl = item.imageUrl,
                        whatsappLink = item.whatsappLink,
                        timestamp = System.currentTimeMillis()
                    )
                )
            } catch (e: Exception) {
                Log.w("ListingRepo", "Error recording history", e)
            }
        }

        // 3. Launch WhatsApp or browser
        val uri = Uri.parse(link)
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        try {
            intent.setPackage("com.whatsapp")
            context.startActivity(intent)
        } catch (_: Exception) {
            try {
                intent.setPackage("com.whatsapp.w4b") // WhatsApp Business
                context.startActivity(intent)
            } catch (_: Exception) {
                // Fallback to browser
                val browserIntent = Intent(Intent.ACTION_VIEW, uri).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(browserIntent)
            }
        }
    }

    private fun incrementViews(listingId: String) {
        val updated = _listings.value.map {
            if (it.id == listingId) it.copy(views = it.views + 1) else it
        }
        _listings.value = updated

        firestore?.collection("listings")?.document(listingId)?.update("views", com.google.firebase.firestore.FieldValue.increment(1))
    }

    fun getHistoryFlow(): Flow<List<HistoryEntity>> = historyDao.getRecentHistory()

    fun clearHistory() {
        scope.launch { historyDao.clearAll() }
    }

    // ==========================================
    // SUBMISSIONS & PROMOTIONS
    // ==========================================

    fun submitFreeListing(
        name: String,
        description: String,
        category: String,
        type: ListingType,
        whatsappLink: String,
        imageUrl: String,
        allowDuplicate: Boolean = false
    ): Result<ListingItem> {
        if (!allowDuplicate && isDuplicateLink(whatsappLink)) {
            return Result.failure(Exception("Ye link already publish ho chuka hai! Free publish sirf ek baar ho sakta hai. Agar aap isko dubara promote karna chahte hain to 'Promote Now' use karein."))
        }
        if (containsBlockedKeywords(name) || containsBlockedKeywords(description)) {
            return Result.failure(Exception("Content violates policy terms and contains restricted keywords."))
        }

        val id = "list_" + UUID.randomUUID().toString().take(10)
        val ownerId = authRepository.getCurrentUserId()
        val cleanUrl = com.example.utils.LinkMetadataFetcher.extractCleanWhatsAppUrl(whatsappLink) ?: whatsappLink.trim()
        val fallbackImg = com.example.utils.LinkMetadataFetcher.getDefaultImageForCategory(category, type)
        val resolvedImg = if (imageUrl.isNotBlank()) imageUrl else fallbackImg

        val newListing = ListingItem(
            id = id,
            ownerId = ownerId,
            name = name.trim(),
            description = description.trim().ifBlank { "Join our active WhatsApp community!" },
            imageUrl = resolvedImg,
            whatsappLink = cleanUrl,
            category = category,
            type = type,
            status = ListingStatus.APPROVED, // Immediately approved and displayed in list!
            views = 1,
            isPromoted = false,
            createdAt = System.currentTimeMillis()
        )

        // Prepend to top of listings so user sees their new group immediately
        _listings.value = listOf(newListing) + _listings.value

        // Increment the category's counter
        _categories.value = _categories.value.map {
            if (it.name.equals(category, ignoreCase = true)) it.copy(groupCount = it.groupCount + 1) else it
        }

        firestore?.collection("listings")?.document(id)?.set(newListing.toMap())
            ?.addOnFailureListener { Log.w("ListingRepo", "Failed to sync to Firestore: ${it.message}") }

        return Result.success(newListing)
    }

    // Supports promoting new OR already published links multiple times
    fun getOrCreateListingForPromotion(
        name: String,
        description: String,
        category: String,
        type: ListingType,
        whatsappLink: String,
        imageUrl: String
    ): Result<ListingItem> {
        if (containsBlockedKeywords(name) || containsBlockedKeywords(description)) {
            return Result.failure(Exception("Content violates policy terms and contains restricted keywords."))
        }

        val cleanUrl = com.example.utils.LinkMetadataFetcher.extractCleanWhatsAppUrl(whatsappLink) ?: whatsappLink.trim()
        val normalized = com.example.utils.LinkMetadataFetcher.normalizeLinkForDuplicateCheck(cleanUrl)

        val existing = _listings.value.find {
            it.status != ListingStatus.DELETED &&
                    com.example.utils.LinkMetadataFetcher.normalizeLinkForDuplicateCheck(it.whatsappLink) == normalized
        }

        if (existing != null) {
            val fallbackImg = com.example.utils.LinkMetadataFetcher.getDefaultImageForCategory(category, type)
            val resolvedImg = if (imageUrl.isNotBlank()) imageUrl else if (existing.imageUrl.isNotBlank()) existing.imageUrl else fallbackImg
            val updated = existing.copy(
                name = name.trim().ifBlank { existing.name },
                description = description.trim().ifBlank { existing.description },
                category = category,
                type = type,
                imageUrl = resolvedImg
            )
            _listings.value = _listings.value.map { if (it.id == existing.id) updated else it }
            firestore?.collection("listings")?.document(existing.id)?.set(updated.toMap())
                ?.addOnFailureListener { Log.w("ListingRepo", "Failed to sync to Firestore: ${it.message}") }
            return Result.success(updated)
        }

        return submitFreeListing(
            name = name,
            description = description,
            category = category,
            type = type,
            whatsappLink = whatsappLink,
            imageUrl = imageUrl,
            allowDuplicate = true
        )
    }

    fun activatePaidPromotion(
        listingId: String,
        purchaseToken: String,
        orderId: String,
        priceFormatted: String = "Rs 239"
    ): Result<Unit> {
        val now = System.currentTimeMillis()
        val durationDays = _appSettings.value.promotionDurationDays
        val durationMs = durationDays * 24 * 60 * 60 * 1000L
        val ownerId = authRepository.getCurrentUserId()

        val existing = _listings.value.find { it.id == listingId }
        val listingName = existing?.name ?: "Promotion Campaign"
        val baseStart = if (existing?.isPromoted == true && (existing.promotionEnd ?: 0L) > now) {
            existing.promotionEnd ?: now
        } else {
            now
        }
        val endTime = baseStart + durationMs
        val promoStart = if (existing?.isPromoted == true && (existing.promotionEnd ?: 0L) > now) {
            existing.promotionStart ?: now
        } else {
            now
        }

        val targetListing = existing?.copy(
            isPromoted = true,
            promotionStart = promoStart,
            promotionEnd = endTime,
            status = ListingStatus.APPROVED // Promoted listing automatically approved
        )
        if (targetListing != null) {
            _listings.value = listOf(targetListing) + _listings.value.filter { it.id != listingId }
        }

        // 2. Record promotion
        val promoRecord = PromotionRecord(
            id = "promo_" + UUID.randomUUID().toString().take(8),
            ownerId = ownerId,
            listingId = listingId,
            listingName = listingName,
            purchaseToken = purchaseToken,
            startTime = now,
            endTime = endTime,
            status = PromotionStatus.ACTIVE,
            amountFormatted = priceFormatted,
            createdAt = now
        )
        _promotions.value = listOf(promoRecord) + _promotions.value

        // 3. Record purchase
        val purchaseRec = PurchaseRecord(
            purchaseId = "pur_" + UUID.randomUUID().toString().take(8),
            orderId = orderId,
            userId = ownerId,
            listingId = listingId,
            listingName = listingName,
            productId = "promotion_3_days",
            priceFormatted = priceFormatted,
            purchaseState = "PURCHASED",
            verificationState = "VERIFIED",
            createdAt = now,
            maskedToken = if (purchaseToken.length > 8) purchaseToken.take(4) + "..." + purchaseToken.takeLast(4) else "token_valid"
        )
        _purchases.value = listOf(purchaseRec) + _purchases.value

        // Sync to Firestore
        firestore?.collection("listings")?.document(listingId)?.update(
            mapOf(
                "isPromoted" to true,
                "promotionStart" to now,
                "promotionEnd" to endTime,
                "status" to ListingStatus.APPROVED.name
            )
        )
        firestore?.collection("promotions")?.document(promoRecord.id)?.set(promoRecord.toMap())

        return Result.success(Unit)
    }

    // ==========================================
    // REPORTING SYSTEM
    // ==========================================

    fun submitReport(listingId: String, reason: String, details: String = ""): Result<Unit> {
        val item = _listings.value.find { it.id == listingId }
        val report = ReportRecord(
            id = "rep_" + UUID.randomUUID().toString().take(8),
            listingId = listingId,
            listingName = item?.name ?: "Unknown Listing",
            userId = authRepository.getCurrentUserId(),
            reason = reason,
            details = details,
            createdAt = System.currentTimeMillis()
        )
        _reports.value = listOf(report) + _reports.value
        firestore?.collection("reports")?.document(report.id)?.set(
            mapOf(
                "id" to report.id,
                "listingId" to report.listingId,
                "listingName" to report.listingName,
                "userId" to report.userId,
                "reason" to report.reason,
                "details" to report.details,
                "createdAt" to report.createdAt
            )
        )
        return Result.success(Unit)
    }

    // ==========================================
    // ADMIN ACTIONS
    // ==========================================

    fun adminApproveListing(id: String) {
        _listings.value = _listings.value.map {
            if (it.id == id) it.copy(status = ListingStatus.APPROVED) else it
        }
        firestore?.collection("listings")?.document(id)?.update("status", ListingStatus.APPROVED.name)
    }

    fun adminRejectListing(id: String) {
        _listings.value = _listings.value.map {
            if (it.id == id) it.copy(status = ListingStatus.REJECTED, isPromoted = false) else it
        }
        firestore?.collection("listings")?.document(id)?.update(
            mapOf("status" to ListingStatus.REJECTED.name, "isPromoted" to false)
        )
    }

    fun adminDeleteListing(id: String) {
        _listings.value = _listings.value.filter { it.id != id }
        firestore?.collection("listings")?.document(id)?.delete()
    }

    fun adminTogglePromote(id: String) {
        val now = System.currentTimeMillis()
        _listings.value = _listings.value.map {
            if (it.id == id) {
                val newPromoted = !it.isPromoted
                it.copy(
                    isPromoted = newPromoted,
                    promotionStart = if (newPromoted) now else 0L,
                    promotionEnd = if (newPromoted) now + 3 * 86400000L else 0L
                )
            } else it
        }
    }

    fun adminAddCategory(name: String, iconName: String = "category") {
        val cat = CategoryItem("cat_" + UUID.randomUUID().toString().take(6), name.trim(), iconName, 0)
        _categories.value = _categories.value + cat
        firestore?.collection("categories")?.document(cat.id)?.set(cat.toMap())
    }

    fun adminDeleteCategory(id: String) {
        _categories.value = _categories.value.filter { it.id != id }
        firestore?.collection("categories")?.document(id)?.delete()
    }

    fun updateAppSettings(settings: AppSettings) {
        _appSettings.value = settings
    }

    companion object {
        /**
         * Formats view counts intelligently:
         * 999 -> 999
         * 1200 -> 1.2k
         * 2090 -> 2.09k
         * 4290 -> 4.29k
         * 10400 -> 10.4k
         * 100000 -> 100k
         */
        fun formatViews(count: Long): String {
            return when {
                count < 1000 -> count.toString()
                count < 10000 -> {
                    val k = count / 1000.0
                    val formatted = String.format(Locale.US, "%.2f", k)
                    val trimmed = formatted.trimEnd('0').trimEnd('.')
                    "${trimmed}k"
                }
                count < 1000000 -> {
                    val k = count / 1000.0
                    val formatted = String.format(Locale.US, "%.1f", k)
                    val trimmed = formatted.trimEnd('0').trimEnd('.')
                    "${trimmed}k"
                }
                else -> {
                    val m = count / 1000000.0
                    val formatted = String.format(Locale.US, "%.1f", m)
                    val trimmed = formatted.trimEnd('0').trimEnd('.')
                    "${trimmed}M"
                }
            }
        }
    }
}

sealed class ValidationResult {
    data class Success(val normalizedUrl: String) : ValidationResult()
    data class Error(val message: String) : ValidationResult()
}
