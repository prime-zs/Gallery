package com.prime.gallery

import androidx.compose.runtime.Composable
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Album
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Image
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
import com.prime.gallery.directory.store.Photos
import com.prime.gallery.directory.store.PhotosViewModel
import com.primex.core.SignalWhite
import com.primex.core.TrafficBlack
import com.primex.core.blend
import com.primex.core.rememberState
import com.primex.material2.Label
import com.primex.material2.dialog.NavigationBarProperties


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
@NonRestartableComposable
private fun NavigationBar(
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .navigationBarsPadding()
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
            var selected by rememberState(initial = 0)
            NavTab(title = "Photos", icon = Icons.Outlined.Image, checked = selected == 0) {
                selected = 0
            }
            NavTab(title = "Folders", icon = Icons.Outlined.Folder, checked = selected == 1) {
                selected = 1
            }
            NavTab(title = "Albums", icon = Icons.Outlined.Album, checked = selected == 2) {
                selected = 2
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun Home() {
    Scaffold2(navBar = { NavigationBar() }) {
        val controller =  rememberAnimatedNavController()
       CompositionLocalProvider(LocalNavController provides controller) {
           val viewModel = hiltViewModel<PhotosViewModel>()
           Photos(viewModel = viewModel)
       }
    }
}