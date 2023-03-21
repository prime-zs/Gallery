package com.prime.gallery.core.compose

import androidx.annotation.FloatRange
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.*
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

/**
 * This houses the logic to show [Toast]s, animates [sheet] and displays update progress.
 * @param progress progress for the linear progress bar. pass [Float.NaN] to hide and -1 to show
 * indeterminate and value between 0 and 1 to show progress
 */
@Composable
fun Scaffold2(
    navBar: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    toast: ToastHostState = remember(::ToastHostState),
    @FloatRange(0.0, 1.0) progress: Float = Float.NaN,
    content: @Composable () -> Unit
) {
    Layout(
        modifier = modifier.fillMaxSize(),
        content = {
            // stack each part over the player.
            content()
            ToastHost(state = toast)
            navBar()
            // don't draw progressBar.
            when {
                progress == -1f -> LinearProgressIndicator()
                !progress.isNaN() -> LinearProgressIndicator(progress = progress)
            }
        },
        measurePolicy = CompactMeasurePolicy
    )
}


private val CompactMeasurePolicy =
    MeasurePolicy { measurables, constraints ->
        val width = constraints.maxWidth
        val height = constraints.maxHeight

        // create duplicate constants to measure the contents as per their wishes.
        val duplicate = constraints.copy(minWidth = 0, minHeight = 0)

        // measure original content with original constrains
        val contentPlaceable = measurables[0].measure(constraints)
        val toastPlaceable = measurables[1].measure(duplicate)
        val bottomBarPlaceable = measurables[2].measure(duplicate)
        val progressPlaceable = measurables.getOrNull(3)?.measure(duplicate)


        //place the content
        layout(width, height) {
            contentPlaceable.placeRelative(0, 0)
            toastPlaceable.placeRelative(
                // centre
                width / 2 - toastPlaceable.width / 2,
                // place above bottombar.
                height - toastPlaceable.height - bottomBarPlaceable.height - 16.dp.toPx()
                    .roundToInt()
            )
            bottomBarPlaceable.placeRelative(
                width / 2 - bottomBarPlaceable.width / 2,
                height - bottomBarPlaceable.height
            )
            progressPlaceable?.placeRelative(
                width / 2 - progressPlaceable.width / 2, height - progressPlaceable.height
            )
        }
    }