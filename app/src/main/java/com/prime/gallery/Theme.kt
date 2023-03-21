package com.prime.gallery

import android.app.Activity
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.AnimationConstants
import androidx.compose.animation.core.tween
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.primex.core.*
import com.primex.material2.*

private const val TAG = "Theme"

/**
 * A simple/shortcut typealias of [MaterialTheme]
 */
typealias Material = MaterialTheme


/**
 * A variant of caption.
 */
private val caption2 =
    TextStyle(fontWeight = FontWeight.Normal, fontSize = 10.sp, letterSpacing = 0.4.sp)

/**
 * A variant of [caption] with a smaller font size and tighter letter spacing.
 * Use this style for captions that require less emphasis or in situations where space is constrained.
 *
 * @see caption
 */
val Typography.caption2 get() = com.prime.gallery.caption2

/**
 * The alpha value for the container colors.
 *
 * This constant value represents the alpha (transparency) of the container colors in the current
 * [MaterialTheme]. The value is a Float between 0.0 and 1.0, where 0.0 is completely transparent
 * and 1.0 is completely opaque. This value can be used to adjust the transparency of container
 * backgrounds and other elements in your app that use the container color.
 */
val MaterialTheme.CONTAINER_COLOR_ALPHA get() = 0.15f

/**
 * Checks whether the preference with key [Audiofy.FORCE_COLORIZE] has been set, indicating
 * whether the app should force colorization of views.
 *
 * This property uses the `preference` composable to retrieve the preference value.
 *
 * @return `true` if the preference has been set, `false` otherwise.
 */
val MaterialTheme.forceColorize
    @Composable inline get() = preference(key = Gallery.FORCE_COLORIZE)

/**
 * A variant of [MaterialTheme.shapes.small] with a corner radius of 8dp.
 */
private val small2 = RoundedCornerShape(8.dp)

/**
 * A variant of [MaterialTheme.shapes.small] with a radius of 8dp.
 */
val Shapes.small2 get() = com.prime.gallery.small2

/**
 * This Composable function provides a primary container color with reduced emphasis as compared to
 * the primary color.
 * It is used for styling elements that require a less prominent color.
 *
 * The color returned by this function is derived from the primary color of the current
 * MaterialTheme with an alpha value equal to [MaterialTheme.CONTAINER_COLOR_ALPHA].
 *
 * @return a [Color] object representing the primary container color.
 */
val Colors.primaryContainer
    @Composable inline get() = colors.primary.copy(MaterialTheme.CONTAINER_COLOR_ALPHA)

/**
 * Returns a color that is suitable for content (icons, text, etc.) that sits on top of the primary container color.
 * This color is simply the primary color of the current theme.
 *
 * @return [Color] object that represents the on-primary container color
 */
val Colors.onPrimaryContainer
    @Composable inline get() = colors.primary

/**
 * Secondary container is applied to elements needing less emphasis than secondary
 */
val Colors.secondaryContainer
    @Composable inline get() = colors.secondary.copy(MaterialTheme.CONTAINER_COLOR_ALPHA)

/**
 * On-secondary container is applied to content (icons, text, etc.) that sits on top of secondary
 * container
 */
val Colors.onSecondaryContainer @Composable inline get() = colors.secondary

/**
 * Error container is applied to elements associated with an error state
 */
val Colors.errorContainer
    @Composable inline get() = colors.error.copy(MaterialTheme.CONTAINER_COLOR_ALPHA)

/**
 * On-error container is applied to content (icons, text, etc.) that sits on top of error container
 */
val Colors.onErrorContainer @Composable inline get() = colors.error

/**
 * The overlay color used for backgrounds and shadows.
 * The color is black with alpha 0.04 on light themes and white with alpha 0.04 on dark themes.
 */
val Colors.overlay
    @Composable inline get() = (if (isLight) Color.Black else Color.White).copy(0.04f)

/**
 * The outline color used in the light/dark theme.
 *
 * The color is semi-transparent white/black, depending on the current theme, with an alpha of 0.12.
 */
inline val Colors.outline
    get() = (if (isLight) Color.Black else Color.White).copy(0.12f)

val Colors.onOverlay
    @Composable inline get() = (colors.onBackground).copy(alpha = ContentAlpha.medium)

val Colors.lightShadowColor
    @Composable inline get() = if (isLight) Color.White else Color.White.copy(0.025f)

val Colors.darkShadowColor
    @Composable inline get() = if (isLight) Color(0xFFAEAEC0).copy(0.7f) else Color.Black.copy(0.6f)

private val LightPrimaryColor = Color.SkyBlue
private val LightSecondaryColor = Color.Rose

private val DarkPrimaryColor = Color.Amber
private val DarkSecondaryColor = Color.TrafficYellow

private val defaultThemeShapes =
    Shapes(
        small = RoundedCornerShape(4.dp),
        medium = RoundedCornerShape(4.dp),
        large = RoundedCornerShape(0.dp)
    )

@Composable
fun Theme(isDark: Boolean, content: @Composable () -> Unit) {
    val background by animateColorAsState(
        targetValue = if (isDark) Color(0xFF0E0E0F) else Color(0xFFF5F5FD),
        animationSpec = tween(AnimationConstants.DefaultDurationMillis)
    )

    // TODO: update status_bar here.

    val surface by animateColorAsState(
        targetValue = if (isDark) Color.TrafficBlack else Color.White,
        animationSpec = tween(AnimationConstants.DefaultDurationMillis)
    )

    val primary = if (isDark) DarkPrimaryColor else LightPrimaryColor
    val secondary = if (isDark) DarkSecondaryColor else LightSecondaryColor


    val colors = Colors(
        primary = primary,
        secondary = secondary,
        background = background,
        surface = surface,
        primaryVariant = primary.blend(Color.Black, 0.2f),
        secondaryVariant = secondary.blend(Color.Black, 0.2f),
        onPrimary = Color.SignalWhite,
        onSurface = if (isDark) Color(0xFFE1D9D1) else Color.UmbraGrey,
        onBackground = if (isDark) Color(0xFFE1D9D1) else Color.UmbraGrey,
        error = Color.OrientRed,
        onSecondary = Color.SignalWhite,
        onError = Color.SignalWhite,
        isLight = !isDark
    )

    // FixMe: Add Hide/Show Logic plus move this somewhere else.
    val colorize by preference(key = Gallery.COLOR_STATUS_BAR)
    val isStatusBarHidden by preference(key = Gallery.HIDE_STATUS_BAR)
    val view = LocalView.current
    if (!view.isInEditMode) {
        val overlay = Material.colors.overlay
        SideEffect {
            val window = (view.context as Activity).window
            val color = (if (colorize) colors.primaryVariant else overlay).toArgb()
            window.statusBarColor = color
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars =
                !colorize && !isDark
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars =
                !colorize && !isDark
            window.navigationBarColor = color
            // hide show logic.
        }
    }

    MaterialTheme(
        colors = colors,
        shapes = defaultThemeShapes,
        content = content
    )
}