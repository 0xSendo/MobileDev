package com.example.baseconverter

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import kotlinx.coroutines.delay

@Composable
fun ButtonWithSlide(
    onClick: () -> Unit,
    colors: ButtonColors,
    shape: RoundedCornerShape,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var isClicked by remember { mutableStateOf(false) }
    val offsetX by animateFloatAsState(
        targetValue = if (isClicked) 300f else 0f,
        animationSpec = tween(
            durationMillis = 300,
            easing = FastOutSlowInEasing
        ),
        label = "slide_animation"
    )

    LaunchedEffect(isClicked) {
        if (isClicked) {
            delay(300)
            onClick()
            isClicked = false // Reset the state after animation
        }
    }

    Button(
        onClick = { isClicked = true },
        colors = colors,
        shape = shape,
        modifier = modifier.offset(x = offsetX.dp)
    ) {
        content()
    }
}

@Composable
fun IconButtonWithFade(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var isClicked by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(
        targetValue = if (isClicked) 0f else 1f,
        animationSpec = tween(
            durationMillis = 300,
            easing = FastOutSlowInEasing
        ),
        label = "fade_animation"
    )

    LaunchedEffect(isClicked) {
        if (isClicked) {
            delay(300)
            onClick()
            isClicked = false // Reset the state after animation
        }
    }

    IconButton(
        onClick = { isClicked = true },
        modifier = modifier.alpha(alpha)
    ) {
        content()
    }
}
