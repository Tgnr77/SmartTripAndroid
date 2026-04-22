package com.smarttrip.app.ui.animation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay

/**
 * Wraps list items so they fade + slide in one after another when the list first appears.
 * The stagger caps at [maxStaggerIndex] so very long lists don't wait forever.
 */
@Composable
fun StaggeredFadeIn(
    index: Int,
    stepMillis: Int = 40,
    maxStaggerIndex: Int = 12,
    content: @Composable () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay((index.coerceAtMost(maxStaggerIndex) * stepMillis).toLong())
        visible = true
    }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(400, easing = FastOutSlowInEasing)) +
                slideInVertically(tween(400, easing = FastOutSlowInEasing)) { it / 6 }
    ) {
        content()
    }
}
