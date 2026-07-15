package com.sipos.kebabsk

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LocalizationResourceTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun importantStringResourcesArePresentAndFormatted() {
        assertEquals("Halo, cahyo", context.getString(R.string.dashboard_cashier_greeting, "cahyo"))
        assertEquals("12 item", context.resources.getQuantityString(R.plurals.item_count, 12, 12))
        assertEquals("3 item pesanan", context.resources.getQuantityString(R.plurals.cart_order_count, 3, 3))
        assertEquals("Target harian: 34.5% tercapai", context.getString(R.string.dashboard_target_progress, 34.5))
    }

    @Test
    fun actionableContentDescriptionsAreNotBlank() {
        val descriptions = listOf(
            R.string.cd_delete_cart_item,
            R.string.cd_reduce_quantity,
            R.string.cd_add_quantity,
            R.string.cd_print_receipt,
            R.string.cd_previous_page,
            R.string.cd_next_page,
            R.string.cd_show_password,
            R.string.cd_hide_password
        )

        descriptions.forEach { resId ->
            assertFalse(context.getString(resId).isBlank())
        }
    }
}
