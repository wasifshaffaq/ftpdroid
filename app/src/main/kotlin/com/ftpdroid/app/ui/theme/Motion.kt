package com.ftpdroid.app.ui.theme

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically

object FtpDroidMotion {
    val StandardSpring = spring<Float>(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMedium)
    val DecelerateSpring = spring<Float>(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow)
    val BounceSpring = spring<Float>(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)
}

fun enterFromBottom(): EnterTransition = slideInVertically(
    initialOffsetY = { it / 3 },
    animationSpec = spring(dampingRatio = 0.8f, stiffness = 380f)
) + fadeIn(animationSpec = tween(220))

fun exitToBottom(): ExitTransition = slideOutVertically(
    targetOffsetY = { it / 3 }
) + fadeOut(animationSpec = tween(180))
