package com.sipos.kebabsk.ui.components

import androidx.compose.foundation.background
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

/**
 * Static skeleton placeholder.
 *
 * Keeping the existing modifier name avoids changing skeleton layouts while
 * preventing every placeholder from starting its own infinite animation.
 */
fun Modifier.shimmerEffect(): Modifier = background(Color(0xFFE7E5E4))
