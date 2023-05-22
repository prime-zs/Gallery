package com.prime.gallery.core.compose

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter


/**
 * A simple `[IconButton] composable that takes [painter] as content instead of content composable.
 * @see IconButton
 */
@Composable
inline fun IconButton(
    icon: Painter,
    contentDescription: String?,
    noinline onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: IconButtonColors = IconButtonDefaults.iconButtonColors(),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    IconButton(
        onClick = onClick,
        modifier,
        enabled,
        colors,
        interactionSource
    ) {
        Icon(painter = icon, contentDescription = contentDescription)
    }
}

/**
 * @see IconButton
 */
@Composable
inline fun IconButton(
    icon: ImageVector,
    contentDescription: String?,
    noinline onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: IconButtonColors = IconButtonDefaults.iconButtonColors(),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
){
    IconButton(rememberVectorPainter(image = icon), contentDescription, onClick, modifier, enabled, colors, interactionSource)
}

