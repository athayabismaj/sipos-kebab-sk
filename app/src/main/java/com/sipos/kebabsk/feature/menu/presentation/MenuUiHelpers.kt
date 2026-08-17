package com.sipos.kebabsk.feature.menu.presentation

import androidx.compose.runtime.Immutable
import com.sipos.kebabsk.common.VariantDisplayUtils
import com.sipos.kebabsk.feature.menu.domain.model.MenuItem
import java.text.NumberFormat
import java.util.Locale

@Immutable
data class MenuVariantItem(
    val menuName: String,
    val categoryName: String?,
    val variantId: Long,
    val variantName: String,
    val price: Long,
    val isAvailable: Boolean,
    val insufficientStock: Boolean = false,
    val imageUrl: String? = null
)

fun buildMenuVariantTitle(menuName: String, variantName: String): String {
    val menu = menuName.trim()
    val variant = variantName.trim()

    if (menu.isBlank()) return variant
    if (variant.isBlank() || variant.equals(menu, ignoreCase = true)) return menu

    val formattedVariant = VariantDisplayUtils.formatVariantName(menu, variant)
    if (!formattedVariant.equals(variant, ignoreCase = true)) {
        return "$menu $formattedVariant".trim()
    }

    val menuWords = menu.split(Regex("\\s+"))
    val variantWords = variant.split(Regex("\\s+"))
    val sharedPrefixLength = menuWords
        .zip(variantWords)
        .takeWhile { (menuWord, variantWord) -> menuWord.equals(variantWord, ignoreCase = true) }
        .size
    val variantSuffix = variantWords.drop(sharedPrefixLength).joinToString(" ")

    return if (variantSuffix.isBlank()) menu else "$menu $variantSuffix"
}

fun buildMenuVariantItems(menus: List<MenuItem>): List<MenuVariantItem> {
    return menus.flatMap { menu ->
        menu.variants.map { variant ->
            MenuVariantItem(
                menuName = menu.name,
                categoryName = menu.categoryName,
                variantId = variant.id,
                variantName = variant.name,
                price = variant.price,
                isAvailable = variant.isAvailable,
                insufficientStock = variant.insufficientStock,
                imageUrl = variant.imageUrl
            )
        }
    }
}

fun buildMenuCategories(menuItems: List<MenuVariantItem>): List<String?> {
    val uniqueCategories = menuItems.mapNotNull { it.categoryName }.distinct().sorted()
    return listOf(null) + uniqueCategories
}

fun filterMenuItems(menuItems: List<MenuVariantItem>, selectedCategory: String?): List<MenuVariantItem> {
    return if (selectedCategory == null) menuItems else menuItems.filter { it.categoryName == selectedCategory }
}

fun buildQuickAmounts(totalAmount: Long): List<Long> {
    if (totalAmount == 0L) return emptyList()

    val exactAmount = totalAmount
    val list = mutableListOf<Long>()
    list.add(exactAmount)

    val basicRounding = listOf(
        (exactAmount / 5000 + 1) * 5000,
        (exactAmount / 10000 + 1) * 10000,
        (exactAmount / 20000 + 1) * 20000,
        (exactAmount / 50000 + 1) * 50000,
        (exactAmount / 100000 + 1) * 100000
    )

    list.addAll(basicRounding.filter { it > exactAmount }.distinct().take(3))

    if (list.size < 4) {
        val highest = list.lastOrNull() ?: exactAmount
        list.add(highest + 50000)
    }

    return list.distinct().take(4)
}
