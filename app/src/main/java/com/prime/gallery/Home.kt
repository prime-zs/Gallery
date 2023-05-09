package com.prime.gallery

import android.util.Log
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
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.prime.gallery.core.compose.NavTab
import com.prime.gallery.core.compose.Navigation
import com.prime.gallery.core.compose.ToastHostState
import com.prime.gallery.core.compose.current
import com.prime.gallery.directory.store.Folders
import com.prime.gallery.directory.store.FoldersViewModel
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

// Default Enter/Exit Transitions.
@OptIn(ExperimentalAnimationApi::class)
private val EnterTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition) =
    {
        scaleIn(initialScale = 0.98f, animationSpec = tween(220, delayMillis = 90)) +
                fadeIn(animationSpec = tween(700))

    }
private val ExitTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition) =
    {
        fadeOut(tween(700))
    }

@OptIn(ExperimentalAnimationApi::class)
@Composable
@NonRestartableComposable
private fun NavGraph(
    controller: NavHostController,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        content = {
            // In order to navigate and remove the need to pass controller below UI components.
            // pass controller as composition local.
            CompositionLocalProvider(
                LocalNavController provides controller,
                content = {
                    // actual paragraph
                    AnimatedNavHost(
                        navController = controller,
                        startDestination = Folders.route,
                        enterTransition = EnterTransition,
                        exitTransition = ExitTransition,
                        builder = {
                            //Folders
                            composable(Folders.route) {
                                val viewModel = hiltViewModel<FoldersViewModel>()
                                Folders(viewModel = viewModel)
                            }

                            // Folder Explorer.
                            composable(Photos.route) {
                                val viewModel = hiltViewModel<PhotosViewModel>()
                                Photos(viewModel = viewModel)
                            }

                            // Settings
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

private const val TAG = "Home"

// The starting point of the app.
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun Home(state: ToastHostState) {
    val navController = rememberAnimatedNavController()
    // TODO: Add logic to hide the nav graph
    // TODO: Add
    val currRoute by  navController.currentBackStackEntryAsState()
    val hide = currRoute?.arguments?.getString(Photos.PARAM_TYPE) == Photos.GET_FROM_FOLDER

    Log.d(TAG, "Home: ${hide}")
    Navigation(
        content = { NavGraph(controller = navController) },
        toast = state,
        hide = hide
    ) {
        val current = navController.current
        val provider = LocalsProvider.current
        // Photos
        NavTab(
            title = "Photos",
            icon = Icons.Outlined.Image,
            checked = current == Photos.route
        ) {
            navController.navigate(Photos.direction(Photos.GET_EVERY)) {
                // Pop up to the start destination of the graph to
                // avoid building up a large stack of destinations
                // on the back stack as users select items
              /*  popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }*/
                // Avoid multiple copies of the same destination when
                // reselecting the same item
                launchSingleTop = true
                // Restore state when reselecting a previously selected item
                restoreState = true
            }
        }

        // Folders
        NavTab(
            title = "Folders",
            icon = Icons.Outlined.Folder,
            checked = current == Folders.route
        ) {
            navController.navigate(Folders.direction()) {
                // Pop up to the start destination of the graph to
                // avoid building up a large stack of destinations
                // on the back stack as users select items
              /*  popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }*/
                // Avoid multiple copies of the same destination when
                // re-selecting the same item
                launchSingleTop = true
                // Restore state when re-selecting a previously selected item
                restoreState = true
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

        //Settings screen
        NavTab(
            title = "Settings",
            icon = Icons.Outlined.Tune,
            checked = current == Settings.route
        ) {
            navController.navigate(Settings.route) {
                // Pop up to the start destination of the graph to
                // avoid building up a large stack of destinations
                // on the back stack as users select items
              /*  popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }*/
                // Avoid multiple copies of the same destination when
                // reselecting the same item
                launchSingleTop = true
                // Restore state when reselecting a previously selected item
                restoreState = true
            }
        }
    }
}