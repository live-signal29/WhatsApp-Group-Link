package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.ListingItem
import com.example.data.model.ListingType
import com.example.data.repository.ListingRepository
import com.example.data.repository.ValidationResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read app name from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("WhatsApp Group Links", appName)
    }

    @Test
    fun `format views handles various thresholds`() {
        assertEquals("999", ListingRepository.formatViews(999))
        assertEquals("1.2k", ListingRepository.formatViews(1200))
        assertEquals("2.09k", ListingRepository.formatViews(2090))
        assertEquals("4.29k", ListingRepository.formatViews(4290))
        assertEquals("10.4k", ListingRepository.formatViews(10400))
        assertEquals("100k", ListingRepository.formatViews(100000))
    }

    @Test
    fun `promotion active check respects end timestamp`() {
        val now = System.currentTimeMillis()
        val activeItem = ListingItem(
            isPromoted = true,
            promotionEnd = now + 100000L
        )
        assertTrue(activeItem.isPromotionCurrentlyActive())

        val expiredItem = ListingItem(
            isPromoted = true,
            promotionEnd = now - 10000L
        )
        assertFalse(expiredItem.isPromotionCurrentlyActive())

        val nonPromotedItem = ListingItem(
            isPromoted = false,
            promotionEnd = now + 100000L
        )
        assertFalse(nonPromotedItem.isPromotionCurrentlyActive())
    }
}
