package com.prime.gallery

import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.PhotoAlbum
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.prime.gallery.core.NightMode
import com.prime.gallery.core.compose.NavigationBarItem2
import com.prime.gallery.core.compose.Placeholder
import com.prime.gallery.core.compose.current
import com.prime.gallery.directory.local.Folders
import com.prime.gallery.directory.local.FoldersViewModel
import com.prime.gallery.directory.local.Images
import com.prime.gallery.directory.local.ImagesViewModel
import com.prime.gallery.directory.local.Viewer
import com.prime.gallery.directory.local.ViewerViewModel
import com.prime.gallery.settings.Settings
import com.prime.gallery.settings.SettingsViewModel

private const val TAG = "Home"

/**
 * A short-hand alias of [MaterialTheme]
 */
typealias Material = MaterialTheme

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
val LocalNavController = staticCompositionLocalOf<NavHostController> {
    error("no local nav host controller found")
}

/**
 * A simple composable that helps in resolving the current app theme as suggested by the [Gallery.NIGHT_MODE]
 */
@Composable
@NonRestartableComposable
private fun resolveAppThemeState(): Boolean {
    val mode by preference(key = Gallery.NIGHT_MODE)
    return when (mode) {
        NightMode.YES -> true
        NightMode.FOLLOW_SYSTEM -> isSystemInDarkTheme()
        else -> false
    }
}

// Default Enter/Exit Transitions.
@OptIn(ExperimentalAnimationApi::class)
private val EnterTransition = scaleIn(tween(220, 90), 0.98f) + fadeIn(tween(700))
private val ExitTransition = fadeOut(tween(700))

/**
 * The route to permission screen.
 */
private const val PERMISSION_ROUTE = "_route_storage_permission"

/**
 * The Storage Permission to ask for.
 */
private val STORAGE_PERMISSION =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) android.Manifest.permission.READ_MEDIA_IMAGES
    else android.Manifest.permission.WRITE_EXTERNAL_STORAGE


/**
 * The permission screen.
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun Permission() {
    val controller = LocalNavController.current
    // Compose the permission state.
    // Once granted set different route like folders as start.
    // Navigate from here to there.
    val permission = rememberPermissionState(permission = STORAGE_PERMISSION) {
        controller.graph.setStartDestination(Folders.route)
    }
    Surface(
        color = Material.colorScheme.background, modifier = Modifier.fillMaxSize()
    ) {
        Placeholder(
            iconResId = R.raw.lt_permission,
            title = stringResource(R.string.storage_permission),
            message = stringResource(R.string.storage_permission_msg),
        ) {
            OutlinedButton(
                onClick = { permission.launchPermissionRequest() },
                modifier = Modifier.size(width = 200.dp, height = 46.dp)
            ) {
                Text(text = "ALLOW")
            }
        }
    }
}

private val DarkColorScheme = darkColorScheme(background = Color(0xFF0E0E0F))
private val LightColorScheme = lightColorScheme()

@Composable
@NonRestartableComposable
private fun Material(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,  // Dynamic color is available on Android 12+
    content: @Composable () -> Unit,
) {
    // compute the color scheme.
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    // Pass values to the actual composable.
    MaterialTheme(
        colorScheme = colorScheme, content = content
    )
}

/**
 * A simple structure of the NavGraph.
 */
@OptIn(ExperimentalAnimationApi::class)
@NonRestartableComposable
@Composable
private fun NavGraph(
    controller: NavHostController, modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    // Load start destination based on if storage permission is set or not.
    val startDestination = when (context.checkSelfPermission(STORAGE_PERMISSION)) {
        PackageManager.PERMISSION_GRANTED -> Folders.route
        else -> PERMISSION_ROUTE
    }
    // In order to navigate and remove the need to pass controller below UI components.
    // pass controller as composition local.
    CompositionLocalProvider(LocalNavController provides controller, content = {
        // actual paragraph
        Box(modifier) {
            AnimatedNavHost(navController = controller,
                startDestination = startDestination, //
                enterTransition = { EnterTransition },
                exitTransition = { ExitTransition },
                builder = {
                    //Permission
                    composable(PERMISSION_ROUTE) {
                        Permission()
                    }

                    //Folders
                    composable(Folders.route) {
                        val viewModel = hiltViewModel<FoldersViewModel>()
                        Folders(viewModel = viewModel)
                    }

                    //Images
                    composable(Images.route) {
                        val viewModel = hiltViewModel<ImagesViewModel>()
                        Images(viewModel = viewModel)
                    }

                    //Viewer
                    composable(Viewer.route) {
                        val viewModel = hiltViewModel<ViewerViewModel>()
                        Viewer(viewModel = viewModel)
                    }

                    //Settings
                    composable(Settings.route) {
                        val viewModel = hiltViewModel<SettingsViewModel>()
                        Settings(viewModel = viewModel)
                    }
                })
        }
    })
}

@NonRestartableComposable
@Composable
private fun BottomBar(controller: NavHostController) {
    val current by controller.currentBackStackEntryAsState()
    BottomAppBar() {
        Spacer(modifier = Modifier.weight(1f))
        // Photos
        NavigationBarItem2(title = "Timeline",
            icon = Icons.Outlined.Image,
            checked = current?.destination?.route == Images.route,
            onRequestChange = {
                /*TODO: Not Implemented yet.*/
            })
        // Folders
        NavigationBarItem2(title = "Folders",
            icon = Icons.Outlined.Folder,
            checked = current?.destination?.route == Folders.route,
            onRequestChange = {

            })

        // Albums
        val provider = LocalsProvider.current
        NavigationBarItem2(title = "Albums",
            icon = Icons.Outlined.PhotoAlbum,
            checked = false,
            onRequestChange = {
                provider.snack("The feature is currently not available.", "Dismiss")
            })

        //Settings screen
        NavigationBarItem2(title = "Settings",
            icon = Icons.Outlined.Tune,
            checked = current?.destination?.route == Settings.route,
            onRequestChange = { controller.navigate(Settings.direction()) })
        Spacer(modifier = Modifier.weight(1f))
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun Home(channel: SnackbarHostState) {
    val navController = rememberAnimatedNavController()
    val darkTheme = resolveAppThemeState()
    // Observe if the user wants dynamic light.
    // Supports only above android 12+
    val dynamicColor by preference(key = Gallery.DYNAMIC_COLORS)

    // current route.
    val current by navController.currentBackStackEntryAsState()
    val hideNavUI = when (current?.destination?.route) {
        Viewer.route, PERMISSION_ROUTE -> true
        else -> false
    }
    Material(darkTheme, dynamicColor) {
        // handle the color of navBars.
        // handle the color of navBars.
        val view = LocalView.current

        // Observe if the user wants to color the SystemBars
        val colorSystemBars by preference(key = Gallery.COLOR_STATUS_BAR)
        val systemBarsColor =
            if (colorSystemBars) Material.colorScheme.primary else Color.Transparent


        if (!view.isInEditMode) {
            SideEffect {
                val window = (view.context as Activity).window
                window.navigationBarColor = systemBarsColor.toArgb()
                window.statusBarColor = systemBarsColor.toArgb()
                WindowCompat
                    .getInsetsController(window, view)
                    .isAppearanceLightStatusBars = !darkTheme && !colorSystemBars
            }
        }

        val hideStatusBar by preference(key = Gallery.HIDE_STATUS_BAR)
        if (!view.isInEditMode) {
            SideEffect {
                val window = (view.context as Activity).window
                if (hideStatusBar)
                    WindowCompat.getInsetsController(window, view)
                        .hide(WindowInsetsCompat.Type.statusBars())
                else
                    WindowCompat.getInsetsController(window, view)
                        .show(WindowInsetsCompat.Type.statusBars())
            }
        }

        //Place the content.
        Scaffold(
            snackbarHost = { SnackbarHost(hostState = channel) },
            content = { NavGraph(controller = navController, Modifier.padding(it)) },
            bottomBar = {
                if (hideNavUI) return@Scaffold
                // else draw the bottom bar.
                BottomBar(controller = navController)
            },
            contentWindowInsets = WindowInsets(0, 0, 0, 0)
        )
    }
}