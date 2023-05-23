package com.prime.gallery.core.compose

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.prime.gallery.Material
import com.prime.gallery.core.ContentPadding


@Composable
fun NavigationBarItem2(
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    checked: Boolean = false,
    onRequestChange: () -> Unit
) {
    Surface(
        selected = checked,
        onClick = onRequestChange,
        shape = CircleShape,
        modifier = modifier,
        color = if (checked) Material.colorScheme.primaryContainer else Color.Transparent,
        contentColor = if (checked) Material.colorScheme.onPrimaryContainer else LocalContentColor.current,
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = ContentPadding.medium)
                .animateContentSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = null)
            if (checked) Text(
                text = title,
                modifier = Modifier.padding(start = ContentPadding.medium),
                style = Material.typography.labelSmall,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}