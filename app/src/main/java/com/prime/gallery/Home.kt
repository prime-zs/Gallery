package com.prime.gallery

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.prime.gallery.core.compose.NavTab
import com.prime.gallery.core.compose.Navigation
import com.prime.gallery.core.compose.ToastHostState
import com.prime.gallery.core.compose.current
import com.prime.gallery.directory.store.Photos
import com.prime.gallery.directory.store.PhotosViewModel
import com.prime.gallery.settings.Settings
import com.prime.gallery.settings.SettingsViewModel
import com.primex.core.*

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

@OptIn(ExperimentalAnimationApi::class)
private val EnterTransition =
    scaleIn(initialScale = 0.98f, animationSpec = tween(220, delayMillis = 90)) +
            fadeIn(animationSpec = tween(700))

private val ExitTransition = fadeOut(tween(700))

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun NavGraph(
    controller: NavHostController,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        content = {
            CompositionLocalProvider(
                LocalNavController provides controller,
                content = {
                    // actual content
                    AnimatedNavHost(
                        navController = controller,
                        startDestination = Photos.route,
                        enterTransition = { EnterTransition },
                        exitTransition = { ExitTransition },
                        builder = {

                            // unit converter composable
                            composable(Photos.route) {
                                val viewModel = hiltViewModel<PhotosViewModel>()
                                Photos(viewModel = viewModel)
                            }

                            // settings
                            composable(Settings.route) {
                                val viewModel = hiltViewModel<SettingsViewModel>()
                                Settings(viewModel)
                            }
                        }
                    )
                }
            )
        }
    )
}


// The starting point of the app.
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun Home(state: ToastHostState) {
    val controller = rememberAnimatedNavController()
    Navigation(
        content = { NavGraph(controller = controller) },
        toast = state
    ) {
        val current = controller.current
        val provider = LocalsProvider.current

        // Photos
        NavTab(
            title = "Photos",
            icon = Icons.Outlined.Image,
            checked = current == Photos.route
        ) {
            controller.navigate(Photos.direction(Photos.GET_EVERY))
        }

        // Folders
        NavTab(
            title = "Folders",
            icon = Icons.Outlined.Folder,
            checked = false
        ) {
            controller.navigate(Photos.direction(Photos.GET_EVERY)) {
                launchSingleTop = true
            }
        }

        // Albums
        NavTab(
            title = "Albums",
            icon = Icons.Outlined.PhotoAlbum,
            checked = false
        ) {
            provider.show(
                Text(R.string.coming_soon),
                Text(R.string.coming_soon_msg),
                accent = Color.Red
            )
        }

        NavTab(
            title = "Settings",
            icon = Icons.Outlined.Tune,
            checked = current == Settings.route
        ) {
            controller.navigate(Settings.route) {
                launchSingleTop = true
            }
        }
    }
}