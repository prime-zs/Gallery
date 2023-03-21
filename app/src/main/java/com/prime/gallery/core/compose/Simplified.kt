@file:Suppress("NOTHING_TO_INLINE")

package com.prime.gallery.core.compose

import androidx.annotation.DrawableRes
import androidx.annotation.RawRes
import androidx.compose.animation.core.AnimationConstants
import androidx.compose.animation.graphics.ExperimentalAnimationGraphicsApi
import androidx.compose.animation.graphics.res.animatedVectorResource
import androidx.compose.animation.graphics.vector.AnimatedImageVector
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import com.primex.material2.Label

//This file holds the simple extension, utility methods of compose.
/**
 * Composes placeholder with lottie icon.
 */
@Composable
inline fun Placeholder(
    title: String,
    @RawRes iconResId: Int,
    modifier: Modifier = Modifier,
    vertical: Boolean = true,
    message: String? = null,
    noinline action: @Composable (() -> Unit)? = null
) {
    com.primex.material2.Placeholder(
        modifier = modifier,
        vertical = vertical,
        message = { if (message != null) Text(text = message) },
        title = { Label(text = title.ifEmpty { " " }, maxLines = 2) },

        icon = {
            val composition by rememberLottieComposition(
                spec = LottieCompositionSpec.RawRes(
                    iconResId
                )
            )
            LottieAnimation(
                composition = composition, iterations = Int.MAX_VALUE
            )
        },
        action = action,
    )
}


@ExperimentalAnimationGraphicsApi
@Composable
inline fun rememberAnimatedVectorResource(@DrawableRes id: Int, atEnd: Boolean) =
    androidx.compose.animation.graphics.res.rememberAnimatedVectorPainter(
        animatedImageVector = AnimatedImageVector.animatedVectorResource(id = id), atEnd = atEnd
    )

@Composable
inline fun Image(
    data: Any?,
    modifier: Modifier = Modifier,
    error: Painter? = null,
    contentScale: ContentScale = ContentScale.Crop,
    alignment: Alignment = Alignment.Center,
    fadeMills: Int = AnimationConstants.DefaultDurationMillis,
) {
    val context = LocalContext.current
    val request = remember(data) {
        ImageRequest.Builder(context).data(data).crossfade(fadeMills).build()
    }

    AsyncImage(
        model = request,
        contentDescription = null,
        error = error,
        modifier = modifier,
        contentScale = contentScale,
        alignment = alignment,
    )
}
