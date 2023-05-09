package com.prime.gallery.core.compose

import androidx.annotation.FloatRange
import androidx.compose.animation.*
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.*
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import androidx.navigation.NavGraph
import com.prime.gallery.Material
import com.prime.gallery.core.ContentElevation
import com.prime.gallery.core.ContentPadding
import com.prime.gallery.onPrimaryContainer
import com.prime.gallery.primaryContainer
import com.primex.core.*
import com.primex.material2.Label
import kotlin.math.roundToInt

private val NAV_BAR_COMPACT_HEIGHT = 58.dp

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun NavTab(
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    checked: Boolean = false,
    onRequestCheck: () -> Unit
) {
    Surface(
        selected = checked,
        onClick = onRequestCheck,
        modifier = modifier,
        shape = CircleShape,
        color = if (checked) Material.colors.primaryContainer else Color.Transparent,
        contentColor = if (checked) Material.colors.onPrimaryContainer else LocalContentColor.current,
        content = {
            Row(
                modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = ContentPadding.medium)
                    .animateContentSize(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = icon, contentDescription = null)
                if (checked)
                    Label(
                        text = title,
                        modifier = Modifier.padding(start = ContentPadding.medium),
                        style = Material.typography.caption,
                        fontWeight = FontWeight.SemiBold
                    )
            }
        }
    )
}

@Composable
@NonRestartableComposable
private fun NavigationBar(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    Surface(
        modifier = modifier
            .padding(ContentPadding.normal)
            .height(NAV_BAR_COMPACT_HEIGHT)
            .fillMaxWidth()
            .scale(0.85f),
        elevation = ContentElevation.xHigh,
        shape = CircleShape,
        color = Material.colors.background,
        border = if (Material.colors.isLight) null else ButtonDefaults.outlinedBorder
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            content = content
        )
    }
}

/**
 * A [CompositionLocal] used to set the content padding for the screen under the current [NavGraph].
 *
 * This local provides a [PaddingValues] object that represents the amount of padding to be applied
 * to the content of the screen. The default value is 0.dp, which means no padding will be applied.
 *
 * This local can be accessed using the [LocalWindowPadding] compositionLocalOf function.
 *
 * Example usage:
 *
 * ```
 * @Composable
 * fun MyScreenContent() {
 *     val windowPadding = LocalWindowPadding.current
 *
 *     Box(
 *         modifier = Modifier.padding(windowPadding)
 *     ) {
 *         // Screen content goes here
 *     }
 * }
 * ```
 */
val LocalWindowPadding =
    compositionLocalOf {
        PaddingValues(0.dp)
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


/**
 * This composable function is responsible for displaying the main content of the screen along with additional features
 * such as [Toast] messages, animated [NavigationBar], and app update progress indicator.
 *
 * @param content the main content of the screen to be displayed
 * @param modifier optional [Modifier] to be applied to the composable
 * @param toast optional [ToastHostState] object to handle [Toast] messages to be displayed
 * @param progress optional progress value to show a linear progress bar. Pass [Float.NaN] to hide the progress bar,
 *        -1 to show an indeterminate progress bar, or a value between 0 and 1 to show a determinate progress bar.
 * @param tabs optional [Composable] function to display a navigation bar or toolbar
 * @param hide optional value to force hide the bottom navigation bar.
 */
@Composable
// this currently represents the compact width nav bar
fun Navigation(
    content: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    hide: Boolean = false,
    toast: ToastHostState = remember(::ToastHostState),
    @FloatRange(0.0, 1.0) progress: Float = Float.NaN,
    tabs: @Composable () -> Unit,
) {
    val navBarPadding = WindowInsets.navigationBars.asPaddingValues()
    val orgBottomBarHeight = NAV_BAR_COMPACT_HEIGHT +
            navBarPadding.calculateBottomPadding()+
            2 * ContentPadding.normal
    val orgBottomBarHeightPx = with(LocalDensity.current){orgBottomBarHeight.toPx()}

    val currHeight = remember {
        mutableStateOf(0f)
    }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                val delta = available.y
                val newOffset = currHeight.value + delta
                currHeight.value =
                    newOffset.coerceIn(0f, orgBottomBarHeightPx)
                return Offset.Zero
            }
        }
    }

    Layout(
        modifier = modifier.nestedScroll(nestedScrollConnection).fillMaxSize(),
        measurePolicy = CompactMeasurePolicy,
        content = {
            CompositionLocalProvider(LocalWindowPadding provides PaddingValues(bottom = orgBottomBarHeight)) {
                content()
            }
            // then place the toast host
            ToastHost(state = toast)
            // The navigationBar
            // handle the logic for
            NavigationBar(modifier = Modifier
                .padding(navBarPadding)
                .offset{
                    IntOffset(0, if (hide) orgBottomBarHeightPx.roundToInt() else currHeight.value.toInt())
                }

            ) {
                tabs()
            }
            // don't draw progressBar.
            when {
                // special value indicating that the progress is about to start.
                progress == -1f -> LinearProgressIndicator()
                // draw the progress bar at the bottom of the screen when is not a NAN.
                !progress.isNaN() -> LinearProgressIndicator(progress = progress)
            }
        }
    )
}