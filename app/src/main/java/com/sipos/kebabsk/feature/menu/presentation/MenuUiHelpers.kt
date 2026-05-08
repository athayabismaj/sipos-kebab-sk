package com.sipos.kebabsk.feature.menu.presentation

import androidx.compose.runtime.Immutable
import com.sipos.kebabsk.feature.menu.domain.model.MenuItem
import java.text.NumberFormat
import java.util.Locale

@Immutable
data class MenuVariantItem(
    val menuName: String,
    val categoryName: String?,
    val variantId: Long,
    val variantName: String,
    val price: Double,
    val isAvailable: Boolean,
    val insufficientStock: Boolean = false
)

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
                insufficientStock = variant.insufficientStock
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

fun buildQuickAmounts(totalAmount: Double): List<Int> {
    if (totalAmount == 0.0) return emptyList()

    val exactAmount = totalAmount.toInt()
    val list = mutableListOf<Int>()
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

// Formatter dengan desimal — untuk input/ringkasan
private val rupiahFormatter: NumberFormat by lazy {
    NumberFormat.getCurrencyInstance(Locale.forLanguageTag("id-ID"))
}

// Formatter TANPA desimal — untuk tampilan total besar agar tidak berantakan
private val rupiahFormatterNoDecimal: NumberFormat by lazy {
    NumberFormat.getCurrencyInstance(Locale.forLanguageTag("id-ID")).apply {
        maximumFractionDigits = 0
        minimumFractionDigits = 0
    }
}

fun toRupiah(amount: Double): String = rupiahFormatter.format(amount)

/** Rupiah tanpa koma/desimal. Untuk tampilan angka besar seperti Total Tagihan. */
fun toRupiahNoDecimal(amount: Double): String = rupiahFormatterNoDecimal.format(amount)
