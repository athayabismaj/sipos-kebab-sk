package com.sipos.kebabsk.common.presentation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sipos.kebabsk.AppTab
import com.sipos.kebabsk.R
import com.sipos.kebabsk.ui.theme.SiposKebabSkTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppBottomNavigationTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun bottomNavigationShowsLabelsAndCallsSelectionCallback() {
        var selectedTab = AppTab.CASHIER
        var clickedTab: AppTab? = null

        composeRule.setContent {
            SiposKebabSkTheme {
                AppBottomNavigation(
                    selectedDestination = selectedTab,
                    onDestinationSelected = {
                        selectedTab = it
                        clickedTab = it
                    }
                )
            }
        }

        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val cashier = context.getString(R.string.nav_cashier)
        val transactions = context.getString(R.string.nav_transactions)
        val profile = context.getString(R.string.nav_profile)

        composeRule.onNodeWithText(cashier).assertIsDisplayed()
        composeRule.onNodeWithText(transactions).assertIsDisplayed()
        composeRule.onNodeWithText(profile).assertIsDisplayed()

        composeRule.onNodeWithText(transactions).performClick()

        composeRule.runOnIdle {
            assertEquals(AppTab.TRANSACTIONS, clickedTab)
        }
    }
}
