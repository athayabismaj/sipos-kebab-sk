package com.sipos.kebabsk.feature.splash.presentation

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val ColorPrimary = Color(0xFF904D00)
private val ColorPrimaryContainer = Color(0xFFFF8C00)
private val ColorSurface = Color(0xFFFEF8F3)
private val ColorSurfaceVariant = Color(0xFFEDE7E2)
private val ColorOnSurfaceVariant = Color(0xFF564334)

@Composable
fun KebabSkSplashScreen() {
    val infiniteTransition = rememberInfiniteTransition(label = "booting")
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "progress"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(ColorSurfaceVariant, ColorSurface, ColorPrimary.copy(alpha = 0.05f))
                )
            )
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(128.dp)
                    .shadow(elevation = 20.dp, shape = CircleShape, clip = false)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(ColorPrimary, ColorPrimaryContainer)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White.copy(alpha = 0.1f))
                )

                Icon(
                    imageVector = Icons.Default.ShoppingCart,
                    contentDescription = "Logo",
                    tint = Color.White,
                    modifier = Modifier.size(64.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Kebab SK",
                fontSize = 48.sp,
                fontWeight = FontWeight.Black,
                fontStyle = FontStyle.Italic,
                color = ColorPrimary,
                letterSpacing = (-1).sp
            )

            Text(
                text = "DIGITAL HEARTH POS",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = ColorOnSurfaceVariant,
                letterSpacing = 2.sp
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 64.dp)
                .width(280.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "BOOTING SYSTEM...",
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = ColorOnSurfaceVariant.copy(alpha = 0.7f),
                letterSpacing = 1.5.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(50))
                    .background(ColorSurfaceVariant)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .fillMaxHeight()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(ColorPrimaryContainer, ColorPrimary)
                            )
                        )
                )
            }
        }
    }
}
