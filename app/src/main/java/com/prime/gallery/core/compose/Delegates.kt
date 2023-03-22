package com.prime.gallery.core.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.NonRestartableComposable
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.primex.core.Text

/**
 * Returns a string resource if the Text value is not null.
 *
 * @param value The Text value to be used to retrieve the string resource.
 * @return The string resource if the Text value is not null, otherwise null.
 */
@Composable
inline fun stringResource(value: Text?) =
    if (value == null) null else com.primex.core.stringResource(value = value)


/**
 * Returns a Composable function if the condition is true, otherwise returns null.
 *
 * @param condition The boolean condition that determines if the composable function should be returned.
 * @param content The composable function to be returned if the condition is true.
 * @return The composable function if the condition is true, otherwise null.
 */
fun composableOrNull(condition: Boolean, content: @Composable () -> Unit) =
    when (condition) {
        true -> content
        else -> null
    }

/**
 * Returns the current route of the [NavHostController]
 */
val NavHostController.current
    @Composable inline get() = currentBackStackEntryAsState().value?.destination?.route
