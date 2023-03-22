package com.prime.gallery

import android.net.wifi.hotspot2.pps.HomeSp
import androidx.compose.runtime.Composable
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph
import androidx.navigation.NavHostController
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.prime.gallery.core.ContentElevation
import com.prime.gallery.core.ContentPadding
import com.prime.gallery.core.compose.Scaffold2
import com.prime.gallery.core.compose.current
import com.prime.gallery.directory.store.Photos
import com.prime.gallery.directory.store.PhotosViewModel
import com.prime.gallery.settings.Settings
import com.prime.gallery.settings.SettingsViewModel
import com.primex.core.*
import com.primex.material2.Label
import com.primex.material2.dialog.NavigationBarProperties


@OptIn(ExperimentalAnimationApi::class)
private val EnterTransition =
    scaleIn(initialScale = 0.98f, animationSpec = tween(220, delayMillis = 90)) +
            fadeIn(animationSpec = tween(700))

private val ExitTransition = fadeOut(tween(700))

@OptIn(ExperimentalMaterialApi::class)
@NonRestartableComposable
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
        color = if (checked) Material.colors.secondaryContainer else Color.Transparent,
        contentColor = if (checked) Material.colors.onSecondaryContainer else LocalContentColor.current,
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

/**
 * Used to provide access to the [NavHostController] through composition without needing to pass it down the tree.
 *
 * To use this composition local, you can call [LocalNavController.current] to get the [NavHostController].
 * If no [NavHostController] has been set, an error will be thrown.
 *
 * Example usage:
 *
 * ```
 * val navController = LocalNavController.current
 * navController.navigate("destination")
 * ```
 */
val LocalNavController =
    staticCompositionLocalOf<NavHostController> {
        error("no local nav host controller found")
    }

@Composable
private fun NavigationBar(
    controller: NavHostController,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .padding(bottom = 16.dp, start = ContentPadding.normal, end = ContentPadding.normal)
            .height(58.dp)
            .fillMaxWidth()
            .scale(0.85f),
        elevation = ContentElevation.xHigh,
        shape = CircleShape
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val current = controller.current
            val provider = LocalsProvider.current
            NavTab(title = "Photos", icon = Icons.Outlined.Image, checked = current == Photos.route) {
                controller.navigate(Photos.direction(Photos.GET_EVERY))
            }
            NavTab(title = "Folders", icon = Icons.Outlined.Folder, checked = false) {
                controller.navigate(Photos.direction(Photos.GET_EVERY)){
                    launchSingleTop = true
                }
            }

            NavTab(title = "Albums", icon = Icons.Outlined.PhotoAlbum, checked = false) {
                provider.show(Text(R.string.coming_soon), Text(R.string.coming_soon_msg), accent = Color.Red)
            }

            NavTab(title = "Settings", icon = Icons.Outlined.Tune, checked = current == Settings.route) {
                controller.navigate(Settings.route){
                    launchSingleTop = true
                }
            }
        }
    }
}


@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun NavGraph(controller: NavHostController, modifier: Modifier = Modifier) {
    AnimatedNavHost(
        navController = controller,
        startDestination = Photos.route,
        modifier = modifier,
        enterTransition = { EnterTransition },
        exitTransition = { ExitTransition },
        builder = {
            composable(Photos.route) {
                val viewModel = hiltViewModel<PhotosViewModel>()
                Photos(viewModel = viewModel)
            }

            composable(Settings.route) {
                val viewModel = hiltViewModel<SettingsViewModel>()
                Settings(viewModel = viewModel)
            }
        }
    )
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun Home() {
    val controller =  rememberAnimatedNavController()
    val provider = LocalsProvider.current
    Scaffold2(
        navBar = { NavigationBar(controller) },
        toast = provider.toastHostState,
        progress = provider.inAppUpdateProgress.value
    ) {
        CompositionLocalProvider(LocalNavController provides controller) {
            Box(Modifier.fillMaxSize()) {
                NavGraph(controller = controller)
            }
        }
    }
}

