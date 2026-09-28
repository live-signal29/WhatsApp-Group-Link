package com.example.utils

import android.util.Log
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
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    private val OG_TITLE_PATTERN = Pattern.compile("<meta[^>]+property=[\"']og:title[\"'][^>]+content=[\"']([^\"']+)[\"']", Pattern.CASE_INSENSITIVE)
    private val OG_TITLE_PATTERN_ALT = Pattern.compile("<meta[^>]+content=[\"']([^\"']+)[\"'][^>]+property=[\"']og:title[\"']", Pattern.CASE_INSENSITIVE)

    private val OG_IMAGE_PATTERN = Pattern.compile("<meta[^>]+property=[\"']og:image[\"'][^>]+content=[\"']([^\"']+)[\"']", Pattern.CASE_INSENSITIVE)
    private val OG_IMAGE_PATTERN_ALT = Pattern.compile("<meta[^>]+content=[\"']([^\"']+)[\"'][^>]+property=[\"']og:image[\"']", Pattern.CASE_INSENSITIVE)

    private val OG_DESC_PATTERN = Pattern.compile("<meta[^>]+property=[\"']og:description[\"'][^>]+content=[\"']([^\"']+)[\"']", Pattern.CASE_INSENSITIVE)
    private val OG_DESC_PATTERN_ALT = Pattern.compile("<meta[^>]+content=[\"']([^\"']+)[\"'][^>]+property=[\"']og:description[\"']", Pattern.CASE_INSENSITIVE)

    private val TITLE_TAG_PATTERN = Pattern.compile("<title>(.*?)</title>", Pattern.CASE_INSENSITIVE)

    /**
     * Fetches WhatsApp Group or Channel OpenGraph metadata from the URL.
     */
    suspend fun fetchMetadata(url: String): DetectedMetadata? = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(url.trim())
                .header(
                    "User-Agent",
                    "Mozilla/5.0 (Linux; Android 13; Pixel 7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"
                )
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .header("Accept-Language", "en-US,en;q=0.9")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.w(TAG, "HTTP error fetching URL: ${response.code}")
                    return@withContext extractFallbackFromUrl(url)
                }

                val html = response.body?.string() ?: return@withContext extractFallbackFromUrl(url)

                var title = extractPattern(html, OG_TITLE_PATTERN) ?: extractPattern(html, OG_TITLE_PATTERN_ALT)
                val image = extractPattern(html, OG_IMAGE_PATTERN) ?: extractPattern(html, OG_IMAGE_PATTERN_ALT)
                val desc = extractPattern(html, OG_DESC_PATTERN) ?: extractPattern(html, OG_DESC_PATTERN_ALT)

                if (title == null) {
                    title = extractPattern(html, TITLE_TAG_PATTERN)
                }

                // Clean title if it contains WhatsApp generic text
                val cleanTitle = cleanWhatsAppTitle(title, url)

                DetectedMetadata(
                    title = cleanTitle,
                    description = desc?.replace("&amp;", "&")?.replace("&#39;", "'"),
                    imageUrl = image
                )
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to scrape metadata from $url: ${e.message}")
            extractFallbackFromUrl(url)
        }
    }

    private fun extractPattern(html: String, pattern: Pattern): String? {
        val matcher = pattern.matcher(html)
        return if (matcher.find()) {
            val content = matcher.group(1)?.trim()
            if (content.isNullOrBlank()) null else content
        } else null
    }

    private fun cleanWhatsAppTitle(rawTitle: String?, url: String): String? {
        if (rawTitle.isNullOrBlank()) return extractFallbackFromUrl(url)?.title

        var cleaned = rawTitle
            .replace("WhatsApp Group Invite", "")
            .replace("WhatsApp Group", "")
            .replace("WhatsApp Channel", "")
            .replace("WhatsApp", "")
            .replace("on WhatsApp", "")
            .replace("|", "")
            .replace("–", "")
            .replace("-", "")
            .trim()

        if (cleaned.isBlank() || cleaned.length < 2) {
            return extractFallbackFromUrl(url)?.title
        }

        return cleaned
    }

    private fun extractFallbackFromUrl(url: String): DetectedMetadata? {
        val trimmed = url.trim()
        val code = when {
            trimmed.contains("chat.whatsapp.com/") -> trimmed.substringAfter("chat.whatsapp.com/").substringBefore("?").substringBefore("/")
            trimmed.contains("whatsapp.com/channel/") -> trimmed.substringAfter("whatsapp.com/channel/").substringBefore("?").substringBefore("/")
            else -> null
        } ?: return null

        // Generate a clean readable name placeholder from code if network failed
        val sampleName = if (code.length > 5) {
            val prefix = code.take(8).replaceFirstChar { it.uppercase() }
            "Group $prefix"
        } else "WhatsApp Group"

        return DetectedMetadata(
            title = sampleName,
            description = "Welcome to our WhatsApp community! Join us to connect.",
            imageUrl = null
        )
    }
}
