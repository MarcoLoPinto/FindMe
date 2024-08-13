package com.liner.findme.ui.composable

import android.widget.RatingBar
import androidx.annotation.RawRes
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutElastic
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Star
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.math.MathUtils
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.liner.findme.R
import kotlinx.coroutines.launch
import kotlin.math.ceil
import kotlin.math.floor

@Composable
fun FindMeLottieAnimation(
    @RawRes animationRes: Int,
    modifier: Modifier = Modifier,
    speed: Float = 0.6f,
    iterations: Int = LottieConstants.IterateForever
) {
    val composition by rememberLottieComposition(spec = LottieCompositionSpec.RawRes(animationRes))
    LottieAnimation(
        modifier = modifier,
        composition = composition,
        iterations = iterations,
        speed = speed
    )
}

@Composable
fun RatingBar(
    percentage: Float = 0.5f,
    stars: Int = 5,
    starsSize: Dp = 30.dp
) {
    val colorR = 225
    val colorG = 225

    val fullStars = (stars * percentage).toInt()
    val halfStarPercentage = stars * percentage - fullStars
    val remainingEmptyStars = (stars - fullStars - halfStarPercentage).toInt()

    val starsColor = Color(colorR, colorG, 0)
    val starsHalfColor1 = Color(
        (0.4f * colorR).toInt(),
        (0.4f * colorG).toInt(),
        0
    )
    val starsHalfColor2 = Color(
        (0.6f * colorR).toInt(),
        (0.6f * colorG).toInt(),
        0
    )
    val starsEmpyColor = Color(
        128,
        128,
        128
    )

    Row(modifier = Modifier.height(starsSize)) {
        repeat(fullStars) {
            Icon(
                modifier = Modifier.size(starsSize),
                imageVector = Icons.Outlined.Star,
                contentDescription = null,
                tint = starsColor
            )
        }

        if (fullStars + remainingEmptyStars < stars) {
            Icon(
                modifier = Modifier.size(starsSize),
                imageVector = Icons.Outlined.Star,
                contentDescription = null,
                tint = if (halfStarPercentage < 0.4f) starsHalfColor1 else starsHalfColor2
            )
        }
        repeat(remainingEmptyStars) {
            Icon(
                modifier = Modifier.size(starsSize),
                imageVector = Icons.Outlined.Star,
                contentDescription = null,
                tint = starsEmpyColor
            )
        }


    }
}

@Composable
fun RatingBarVotedDistance(voted_distance_mean: Float, voted_num: Int = -1, starsSize: Dp = 35.dp) {
    val minKmEval = 5f
    val maxKmEval = 500f
    val numStars = 5
    var percentage = 1 - (MathUtils.clamp(
        voted_distance_mean,
        minKmEval,
        maxKmEval
    ) - minKmEval) / (maxKmEval - minKmEval)
    if (voted_num == 0) percentage = 1f / numStars
    RatingBar(percentage = percentage, stars = numStars, starsSize = starsSize)
}

@Composable
@Preview
fun RatingBarVotedDistanceExample() {
    val animationDuration = 20000
    val interpolation = remember { Animatable(500f) }

    LaunchedEffect(interpolation) {
        launch {
            interpolation.animateTo(targetValue = 0f, animationSpec = tween(animationDuration))
        }
    }

    RatingBarVotedDistance(voted_distance_mean = interpolation.value)
}