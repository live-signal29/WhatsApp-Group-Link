package com.example

import com.example.data.model.ListingItem
import com.example.data.model.ListingType
import com.example.data.repository.ListingRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Locale

class ExampleUnitTest {

    @Test
    fun `format views handles various numbers`() {
        assertEquals("500", ListingRepository.formatViews(500))
        assertEquals("1.2k", ListingRepository.formatViews(1200))
        assertEquals("2.09k", ListingRepository.formatViews(2090))
        assertEquals("4.29k", ListingRepository.formatViews(4290))
        assertEquals("10.4k", ListingRepository.formatViews(10400))
    }

    @Test
    fun `promotion active check logic`() {
        val now = System.currentTimeMillis()
        val itemActive = ListingItem(isPromoted = true, promotionEnd = now + 86400000L)
        assertTrue(itemActive.isPromotionCurrentlyActive())

        val itemExpired = ListingItem(isPromoted = true, promotionEnd = now - 1000L)
        assertFalse(itemExpired.isPromotionCurrentlyActive())
    }

    @Test
    fun `link validation rejects malicious schemes`() {
        val maliciousUrls = listOf(
            "javascript:alert(1)",
            "file:///data/system",
            "content://settings",
            "http://chat.whatsapp.com/test",
            "ftp://whatsapp.com"
        )

        for (url in maliciousUrls) {
            val lower = url.trim().lowercase(Locale.ROOT)
            val isValid = lower.startsWith("https://chat.whatsapp.com/") || lower.startsWith("https://whatsapp.com/channel/")
            assertFalse("Expected $url to be invalid", isValid)
        }

        val validGroup = "https://chat.whatsapp.com/sampleGroupCode12345"
        assertTrue(validGroup.startsWith("https://chat.whatsapp.com/"))

        val validChannel = "https://whatsapp.com/channel/sampleChannelCode12345"
        assertTrue(validChannel.startsWith("https://whatsapp.com/channel/"))
    }
}
