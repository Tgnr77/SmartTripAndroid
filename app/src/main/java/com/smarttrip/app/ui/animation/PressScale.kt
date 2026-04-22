package com.smarttrip.app.ui.animation

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer

/**
 * Slightly shrinks the composable while pressed and springs back on release.
 * Pair with a Button/Card that accepts the same [interactionSource] so the
 * scale is driven by the real tap state.
 */
@Composable
fun Modifier.scaleOnPress(
    interactionSource: MutableInteractionSource,
    scaleDown: Float = 0.96f
): Modifier {
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) scaleDown else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "pressScale"
    )
    return this.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

/** Convenience: creates and remembers a [MutableInteractionSource] for press-scale usage. */
@Composable
fun rememberPressInteractionSource(): MutableInteractionSource =
    remember { MutableInteractionSource() }
