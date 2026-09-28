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
    val imageUrl: String?,
    val detectedType: ListingType? = null,
    val suggestedCategory: String? = null
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

    // Regex for HTML Character References (Hex: &#x1f680; and Decimal: &#128640;)
    private val HEX_ENTITY_PATTERN = Pattern.compile("&#x([0-9a-fA-F]+);?", Pattern.CASE_INSENSITIVE)
    private val DEC_ENTITY_PATTERN = Pattern.compile("&#([0-9]+);?")

    /**
     * Determines whether a WhatsApp link is a Group or Channel.
     */
    fun detectLinkType(url: String): ListingType? {
        val trimmed = url.trim().lowercase()
        return when {
            trimmed.contains("whatsapp.com/channel/") -> ListingType.CHANNEL
            trimmed.contains("chat.whatsapp.com/") -> ListingType.GROUP
            else -> null
        }
    }

    /**
     * Extracts a clean, normalized WhatsApp URL even if user pasted full share text.
     */
    fun extractCleanWhatsAppUrl(input: String): String? {
        val trimmed = input.trim()
        val channelMatcher = CHANNEL_URL_REGEX.matcher(trimmed)
        if (channelMatcher.find()) {
            val code = channelMatcher.group(3)
            return "https://whatsapp.com/channel/$code"
        }
        val groupMatcher = GROUP_URL_REGEX.matcher(trimmed)
        if (groupMatcher.find()) {
            val code = groupMatcher.group(2)
            return "https://chat.whatsapp.com/$code"
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
     */
    suspend fun fetchMetadata(url: String): DetectedMetadata? = withContext(Dispatchers.IO) {
        val targetUrl = extractCleanWhatsAppUrl(url) ?: if (!url.startsWith("http://") && !url.startsWith("https://")) "https://${url.trim()}" else url.trim()
        val detectedType = detectLinkType(targetUrl)

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

                    val rawTitle = extractPattern(html, OG_TITLE_P1)
                        ?: extractPattern(html, OG_TITLE_P2)
                        ?: extractPattern(html, TWITTER_TITLE)
                        ?: extractPattern(html, H3_GROUP_NAME)
                        ?: extractPattern(html, TITLE_TAG_PATTERN)

                    val rawImage = extractPattern(html, OG_IMAGE_P1)
                        ?: extractPattern(html, OG_IMAGE_P2)
                        ?: extractPattern(html, TWITTER_IMAGE)
                        ?: extractPattern(html, WHATSAPP_CDN_IMG)

                    val rawDesc = extractPattern(html, OG_DESC_P1)
                        ?: extractPattern(html, OG_DESC_P2)

                    val cleanTitle = cleanWhatsAppTitle(rawTitle, targetUrl)
                    val cleanImage = cleanImageUrl(rawImage)
                    val cleanDesc = rawDesc?.let { unescapeHtml(it) }

                    if (!cleanTitle.isNullOrBlank()) {
                        val suggestedCat = suggestCategory(cleanTitle, cleanDesc)
                        Log.d(TAG, "Successfully detected: title='$cleanTitle', image='$cleanImage', type=$detectedType, cat=$suggestedCat")
                        return@withContext DetectedMetadata(
                            title = cleanTitle,
                            description = cleanDesc,
                            imageUrl = cleanImage,
                            detectedType = detectedType,
                            suggestedCategory = suggestedCat
                        )
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Fetch attempt with UA failed: ${e.message}")
            }
        }

        // Fallback if network blocked or invite code private
        val fallback = extractFallbackFromUrl(targetUrl)
        fallback?.copy(
            detectedType = detectedType,
            suggestedCategory = fallback.title?.let { suggestCategory(it, fallback.description) }
        )
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
        if (unescaped.startsWith("http://") || unescaped.startsWith("https://")) {
            return unescaped
        }
        return null
    }

    /**
     * Completely decodes named, decimal, and hex HTML entities (including emojis & unicode),
     * and strips zero-width artifacts (&#x200b; etc).
     */
    fun unescapeHtml(text: String): String {
        var result = text
            .replace("&amp;", "&")
            .replace("&quot;", "\"")
            .replace("&#39;", "'")
            .replace("&apos;", "'")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&nbsp;", " ")
            .replace("&bull;", "•")
            .replace("&ndash;", "–")
            .replace("&mdash;", "—")
            .replace("&hellip;", "…")

        // 1. Decode Hex entities like &#x1f680; (🚀) or &#x200b; (zero-width space)
        val hexMatcher = HEX_ENTITY_PATTERN.matcher(result)
        val hexSb = StringBuffer()
        while (hexMatcher.find()) {
            val hex = hexMatcher.group(1) ?: ""
            val replacement = try {
                val codePoint = hex.toInt(16)
                if (codePoint == 0x200B || codePoint == 0x200C || codePoint == 0x200D || codePoint == 0xFEFF || codePoint == 0x00A0) {
                    "" // Strip zero-width space
                } else if (Character.isValidCodePoint(codePoint)) {
                    String(Character.toChars(codePoint))
                } else {
                    ""
                }
            } catch (_: Exception) {
                hexMatcher.group(0) ?: ""
            }
            hexMatcher.appendReplacement(hexSb, java.util.regex.Matcher.quoteReplacement(replacement))
        }
        hexMatcher.appendTail(hexSb)
        result = hexSb.toString()

        // 2. Decode Decimal entities like &#128640; (🚀)
        val decMatcher = DEC_ENTITY_PATTERN.matcher(result)
        val decSb = StringBuffer()
        while (decMatcher.find()) {
            val dec = decMatcher.group(1) ?: ""
            val replacement = try {
                val codePoint = dec.toInt(10)
                if (codePoint == 0x200B || codePoint == 0x200C || codePoint == 0x200D || codePoint == 0xFEFF || codePoint == 0x00A0) {
                    ""
                } else if (Character.isValidCodePoint(codePoint)) {
                    String(Character.toChars(codePoint))
                } else {
                    ""
                }
            } catch (_: Exception) {
                decMatcher.group(0) ?: ""
            }
            decMatcher.appendReplacement(decSb, java.util.regex.Matcher.quoteReplacement(replacement))
        }
        decMatcher.appendTail(decSb)
        result = decSb.toString()

        // 3. Strip any residual raw zero-width characters and tidy spaces
        return result
            .replace("\u200B", "")
            .replace("\u200C", "")
            .replace("\u200D", "")
            .replace("\uFEFF", "")
            .trim()
    }

    /**
     * Cleans titles by decoding entities, stripping WhatsApp suffixes, and normalizing.
     */
    fun cleanWhatsAppTitle(rawTitle: String?, url: String): String? {
        if (rawTitle.isNullOrBlank()) return extractFallbackFromUrl(url)?.title

        var unescaped = unescapeHtml(rawTitle)

        // Remove trailing "• WhatsApp Channel", "| WhatsApp", "on WhatsApp", etc.
        unescaped = unescaped
            .replace(Regex("(?i)\\s*[•|\\-–]\\s*WhatsApp Channel.*"), "")
            .replace(Regex("(?i)\\s*[•|\\-–]\\s*WhatsApp Group.*"), "")
            .replace(Regex("(?i)\\s+on WhatsApp\\b.*"), "")
            .replace(Regex("(?i)\\s*\\|\\s*WhatsApp\\b.*"), "")
            .replace(Regex("(?i)^WhatsApp\\s*:\\s*"), "")
            .trim()

        // Trim residual quotes or spaces
        unescaped = unescaped.trim(' ', '"', '\'', '“', '”')

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
        val isChannel = trimmed.contains("whatsapp.com/channel/")
        val code = when {
            trimmed.contains("chat.whatsapp.com/") -> trimmed.substringAfter("chat.whatsapp.com/").substringBefore("?").substringBefore("/")
            isChannel -> trimmed.substringAfter("whatsapp.com/channel/").substringBefore("?").substringBefore("/")
            else -> null
        } ?: return null

        val sampleName = if (code.length > 4) {
            val prefix = code.take(7).replaceFirstChar { it.uppercase() }
            if (isChannel) "Channel $prefix" else "Group $prefix"
        } else {
            if (isChannel) "WhatsApp Channel" else "WhatsApp Group"
        }

        return DetectedMetadata(
            title = sampleName,
            description = if (isChannel) "Follow our official WhatsApp Channel for daily updates!" else "Welcome to our WhatsApp community! Join us to connect.",
            imageUrl = null,
            detectedType = if (isChannel) ListingType.CHANNEL else ListingType.GROUP
        )
    }

    /**
     * Smartly suggests an app category based on title and description keywords.
     */
    fun suggestCategory(title: String, desc: String? = null): String? {
        val text = "$title ${desc ?: ""}".lowercase()
        return when {
            text.contains("signal") || text.contains("crypto") || text.contains("bitcoin") || text.contains("forex") || text.contains("trading") || text.contains("binance") || text.contains("airdrop") || text.contains("btc") -> "Crypto"
            text.contains("startup") || text.contains("business") || text.contains("marketing") || text.contains("wholesale") || text.contains("e-commerce") || text.contains("dropship") || text.contains("earning") || text.contains("client") || text.contains("agency") -> "Business"
            text.contains("news") || text.contains("samachar") || text.contains("khabar") || text.contains("headline") || text.contains("alert") || text.contains("weather") || text.contains("breaking") -> "News"
            text.contains("meme") || text.contains("funny") || text.contains("joke") || text.contains("chutkule") || text.contains("bakchodi") || text.contains("laugh") || text.contains("comedy") -> "Funny"
            text.contains("poetry") || text.contains("shayari") || text.contains("ghazal") || text.contains("kavita") || text.contains("quote") || text.contains("sad") || text.contains("romantic") -> "Poetry"
            text.contains("status") || text.contains("video") || text.contains("reel") || text.contains("short") || text.contains("capcut") || text.contains("clip") -> "Videos"
            text.contains("study") || text.contains("exam") || text.contains("gk") || text.contains("current affairs") || text.contains("upsc") || text.contains("ssc") || text.contains("job") || text.contains("python") || text.contains("code") || text.contains("english") || text.contains("education") -> "Education"
            text.contains("cricket") || text.contains("ipl") || text.contains("football") || text.contains("gym") || text.contains("fitness") || text.contains("sport") || text.contains("workout") -> "Sports"
            text.contains("tech") || text.contains("science") || text.contains("gadget") || text.contains("ai") || text.contains("android") || text.contains("space") || text.contains("nasa") || text.contains("robot") -> "Science"
            text.contains("friend") || text.contains("dost") || text.contains("chat") || text.contains("adda") || text.contains("vibe") || text.contains("gaming") || text.contains("bgmi") -> "Friendship"
            text.contains("food") || text.contains("recipe") || text.contains("cook") || text.contains("biryani") || text.contains("cake") || text.contains("bake") || text.contains("dish") || text.contains("zaika") -> "Food"
            text.contains("movie") || text.contains("cinema") || text.contains("bollywood") || text.contains("hollywood") || text.contains("trailer") || text.contains("web series") || text.contains("netflix") || text.contains("song") -> "Entertainment"
            else -> null
        }
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
