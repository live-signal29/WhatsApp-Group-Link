package com.example.utils

import android.util.Log
import com.example.data.model.ListingType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

data class DetectedMetadata(
    val title: String?,
    val description: String?,
    val imageUrl: String?
)

object LinkMetadataFetcher {

    private const val TAG = "LinkMetadataFetcher"

    private val client = OkHttpClient.Builder()
        .connectTimeout(7, TimeUnit.SECONDS)
        .readTimeout(7, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    // OpenGraph & Meta Regex Patterns (Flexible quotation & spacing)
    private val OG_TITLE_P1 = Pattern.compile("<meta[^>]+property=[\"']og:title[\"'][^>]+content=[\"']([^\"']+)[\"']", Pattern.CASE_INSENSITIVE)
    private val OG_TITLE_P2 = Pattern.compile("<meta[^>]+content=[\"']([^\"']+)[\"'][^>]+property=[\"']og:title[\"']", Pattern.CASE_INSENSITIVE)
    private val TWITTER_TITLE = Pattern.compile("<meta[^>]+name=[\"']twitter:title[\"'][^>]+content=[\"']([^\"']+)[\"']", Pattern.CASE_INSENSITIVE)

    private val OG_IMAGE_P1 = Pattern.compile("<meta[^>]+property=[\"']og:image(?::secure_url)?[\"'][^>]+content=[\"']([^\"']+)[\"']", Pattern.CASE_INSENSITIVE)
    private val OG_IMAGE_P2 = Pattern.compile("<meta[^>]+content=[\"']([^\"']+)[\"'][^>]+property=[\"']og:image(?::secure_url)?[\"']", Pattern.CASE_INSENSITIVE)
    private val TWITTER_IMAGE = Pattern.compile("<meta[^>]+name=[\"']twitter:image[\"'][^>]+content=[\"']([^\"']+)[\"']", Pattern.CASE_INSENSITIVE)
    private val WHATSAPP_CDN_IMG = Pattern.compile("(https://pps\\.whatsapp\\.net/v/[^\"'\\s<>]+)", Pattern.CASE_INSENSITIVE)

    private val OG_DESC_P1 = Pattern.compile("<meta[^>]+property=[\"']og:description[\"'][^>]+content=[\"']([^\"']+)[\"']", Pattern.CASE_INSENSITIVE)
    private val OG_DESC_P2 = Pattern.compile("<meta[^>]+content=[\"']([^\"']+)[\"'][^>]+property=[\"']og:description[\"']", Pattern.CASE_INSENSITIVE)

    private val TITLE_TAG_PATTERN = Pattern.compile("<title[^>]*>(.*?)</title>", Pattern.CASE_INSENSITIVE)
    private val H3_GROUP_NAME = Pattern.compile("<h3[^>]*class=[\"'][^\"']*_9vd5[^\"']*[\"'][^>]*>(.*?)</h3>", Pattern.CASE_INSENSITIVE)

    private val GROUP_URL_REGEX = Pattern.compile("(https?://)?chat\\.whatsapp\\.com/([A-Za-z0-9_-]+)", Pattern.CASE_INSENSITIVE)
    private val CHANNEL_URL_REGEX = Pattern.compile("(https?://)?(www\\.)?whatsapp\\.com/channel/([A-Za-z0-9_-]+)", Pattern.CASE_INSENSITIVE)

    /**
     * Extracts a clean, normalized WhatsApp URL even if user pasted full share text.
     */
    fun extractCleanWhatsAppUrl(input: String): String? {
        val trimmed = input.trim()
        val groupMatcher = GROUP_URL_REGEX.matcher(trimmed)
        if (groupMatcher.find()) {
            val code = groupMatcher.group(2)
            return "https://chat.whatsapp.com/$code"
        }
        val channelMatcher = CHANNEL_URL_REGEX.matcher(trimmed)
        if (channelMatcher.find()) {
            val code = channelMatcher.group(3)
            return "https://whatsapp.com/channel/$code"
        }
        return null
    }

    /**
     * Normalizes a WhatsApp link for strict duplicate detection.
     * Strips protocol, query params, trailing slashes, www.
     */
    fun normalizeLinkForDuplicateCheck(url: String): String {
        val clean = extractCleanWhatsAppUrl(url) ?: url
        return clean.trim()
            .lowercase()
            .removePrefix("https://")
            .removePrefix("http://")
            .removePrefix("www.")
            .substringBefore("?")
            .substringBefore("#")
            .trimEnd('/')
    }

    /**
     * Fetches WhatsApp Group or Channel OpenGraph metadata from the URL.
     * Tries Facebook OpenGraph crawler User-Agent first (which WhatsApp serves pure static OG HTML to),
     * then falls back to WhatsApp client UA if needed.
     */
    suspend fun fetchMetadata(url: String): DetectedMetadata? = withContext(Dispatchers.IO) {
        val targetUrl = extractCleanWhatsAppUrl(url) ?: if (!url.startsWith("http://") && !url.startsWith("https://")) "https://${url.trim()}" else url.trim()

        val crawlerAgents = listOf(
            "facebookexternalhit/1.1 (+http://www.facebook.com/externalhit_uatext.php)",
            "WhatsApp/2.24.6.77 A",
            "Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Mobile Safari/537.36"
        )

        for (ua in crawlerAgents) {
            try {
                val request = Request.Builder()
                    .url(targetUrl)
                    .header("User-Agent", ua)
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
                    .header("Accept-Language", "en-US,en;q=0.9")
                    .build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        return@use
                    }

                    val html = response.body?.string() ?: return@use
                    if (html.isBlank()) return@use

                    var rawTitle = extractPattern(html, OG_TITLE_P1)
                        ?: extractPattern(html, OG_TITLE_P2)
                        ?: extractPattern(html, TWITTER_TITLE)
                        ?: extractPattern(html, H3_GROUP_NAME)
                        ?: extractPattern(html, TITLE_TAG_PATTERN)

                    var rawImage = extractPattern(html, OG_IMAGE_P1)
                        ?: extractPattern(html, OG_IMAGE_P2)
                        ?: extractPattern(html, TWITTER_IMAGE)
                        ?: extractPattern(html, WHATSAPP_CDN_IMG)

                    var rawDesc = extractPattern(html, OG_DESC_P1)
                        ?: extractPattern(html, OG_DESC_P2)

                    val cleanTitle = cleanWhatsAppTitle(rawTitle, targetUrl)
                    val cleanImage = cleanImageUrl(rawImage)
                    val cleanDesc = rawDesc?.let { unescapeHtml(it) }

                    // If we obtained a valid title, return immediately
                    if (!cleanTitle.isNullOrBlank()) {
                        Log.d(TAG, "Successfully detected: title='$cleanTitle', image='$cleanImage'")
                        return@withContext DetectedMetadata(
                            title = cleanTitle,
                            description = cleanDesc,
                            imageUrl = cleanImage
                        )
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Fetch attempt with UA failed: ${e.message}")
            }
        }

        // Fallback if network blocked or invite code private
        extractFallbackFromUrl(targetUrl)
    }

    private fun extractPattern(html: String, pattern: Pattern): String? {
        val matcher = pattern.matcher(html)
        return if (matcher.find()) {
            val content = matcher.group(1)?.trim()
            if (content.isNullOrBlank()) null else content
        } else null
    }

    private fun cleanImageUrl(raw: String?): String? {
        if (raw.isNullOrBlank()) return null
        val unescaped = unescapeHtml(raw)
        // Check if valid URL
        if (unescaped.startsWith("http://") || unescaped.startsWith("https://")) {
            return unescaped
        }
        return null
    }

    private fun unescapeHtml(text: String): String {
        return text
            .replace("&amp;", "&")
            .replace("&quot;", "\"")
            .replace("&#39;", "'")
            .replace("&apos;", "'")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .trim()
    }

    private fun cleanWhatsAppTitle(rawTitle: String?, url: String): String? {
        if (rawTitle.isNullOrBlank()) return extractFallbackFromUrl(url)?.title

        var unescaped = unescapeHtml(rawTitle)

        // Remove trailing "• WhatsApp Channel" or "| WhatsApp" or "on WhatsApp"
        unescaped = unescaped
            .replace(Regex("(?i)\\s*[•|\\-–]\\s*WhatsApp Channel.*"), "")
            .replace(Regex("(?i)\\s*[•|\\-–]\\s*WhatsApp Group.*"), "")
            .replace(Regex("(?i)\\s+on WhatsApp\\b.*"), "")
            .trim()

        // If title is literally generic WhatsApp invite phrase, extract name from URL code
        val genericPhrases = listOf(
            "WhatsApp Group Invite",
            "WhatsApp Group",
            "WhatsApp Channel",
            "WhatsApp",
            "Join group chat on WhatsApp",
            "Join Chat"
        )

        for (phrase in genericPhrases) {
            if (unescaped.equals(phrase, ignoreCase = true)) {
                return extractFallbackFromUrl(url)?.title
            }
        }

        return unescaped.ifBlank { extractFallbackFromUrl(url)?.title }
    }

    fun extractFallbackFromUrl(url: String): DetectedMetadata? {
        val trimmed = url.trim()
        val code = when {
            trimmed.contains("chat.whatsapp.com/") -> trimmed.substringAfter("chat.whatsapp.com/").substringBefore("?").substringBefore("/")
            trimmed.contains("whatsapp.com/channel/") -> trimmed.substringAfter("whatsapp.com/channel/").substringBefore("?").substringBefore("/")
            else -> null
        } ?: return null

        val sampleName = if (code.length > 4) {
            val prefix = code.take(7).replaceFirstChar { it.uppercase() }
            "Group $prefix"
        } else "WhatsApp Group"

        return DetectedMetadata(
            title = sampleName,
            description = "Welcome to our WhatsApp community! Join us to connect.",
            imageUrl = null
        )
    }

    fun getDefaultImageForCategory(category: String, type: ListingType): String {
        return when (category.lowercase()) {
            "news" -> "https://images.unsplash.com/photo-1504711434969-e33886168f5c?w=160&auto=format&fit=crop&q=80"
            "entertainment" -> "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=160&auto=format&fit=crop&q=80"
            "funny" -> "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=160&auto=format&fit=crop&q=80"
            "poetry" -> "https://images.unsplash.com/photo-1455390582262-044cdead277a?w=160&auto=format&fit=crop&q=80"
            "videos" -> "https://images.unsplash.com/photo-1492691527719-9d1e07e534b4?w=160&auto=format&fit=crop&q=80"
            "education" -> "https://images.unsplash.com/photo-1503676260728-1c00da094a0b?w=160&auto=format&fit=crop&q=80"
            "sports" -> "https://images.unsplash.com/photo-1508098682722-e99c43a406b2?w=160&auto=format&fit=crop&q=80"
            "science" -> "https://images.unsplash.com/photo-1518770660439-4636190af475?w=160&auto=format&fit=crop&q=80"
            "friendship" -> "https://images.unsplash.com/photo-1529156069898-49953e39b3ac?w=160&auto=format&fit=crop&q=80"
            "food" -> "https://images.unsplash.com/photo-1504674900247-0877df9cc836?w=160&auto=format&fit=crop&q=80"
            "crypto" -> "https://images.unsplash.com/photo-1622979135225-d2ba269bc1df?w=160&auto=format&fit=crop&q=80"
            "business" -> "https://images.unsplash.com/photo-1556761175-5973dc0f32e7?w=160&auto=format&fit=crop&q=80"
            else -> if (type == ListingType.GROUP) {
                "https://images.unsplash.com/photo-1522071820081-009f0129c71c?w=160&auto=format&fit=crop&q=80"
            } else {
                "https://images.unsplash.com/photo-1518770660439-4636190af475?w=160&auto=format&fit=crop&q=80"
            }
        }
    }
}
