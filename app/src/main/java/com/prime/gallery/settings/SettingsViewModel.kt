package com.prime.gallery.settings

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.HideImage
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prime.gallery.Gallery
import com.primex.core.Text
import com.primex.preferences.Key
import com.primex.preferences.Preferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

typealias Settings = SettingsViewModel.Companion

@Immutable
data class Preference<out P>(
    val value: P,
    @JvmField val title: Text,
    val vector: ImageVector? = null,
    @JvmField val summery: Text? = null,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferences: Preferences,
    private val channel: SnackbarHostState,
) : ViewModel() {

    companion object {
        const val route = "route_settings"
        fun direction() = route

    }

    @Deprecated("Find new solution.")
    private fun <T> Flow<T>.asComposeState(): State<T> {
        val state = mutableStateOf(runBlocking { first() })
        onEach { state.value = it }.launchIn(viewModelScope)
        return state
    }


    val nightMode = with(preferences) {
        preferences[Gallery.NIGHT_MODE].map {
            Preference(
                value = it,
                title = Text("Dark Mode"),
                summery = Text("Choose the dark mode for the app: Yes, No, or Follow System."),
                vector = Icons.Outlined.Lightbulb
            )
        }.asComposeState()
    }

    val colorSystemBars = with(preferences) {
        preferences[Gallery.COLOR_STATUS_BAR].map {
            Preference(
                vector = null,
                title = Text("Color System Bars"),
                summery = Text("Enable or disable coloring of the system bars."),
                value = it
            )
        }.asComposeState()
    }

    val hideStatusBar = with(preferences) {
        preferences[Gallery.HIDE_STATUS_BAR].map {
            Preference(
                value = it,
                title = Text("Hide Status Bar"),
                summery = Text("Toggle the visibility of the status bar for immersive views."),
                vector = Icons.Outlined.HideImage
            )
        }.asComposeState()
    }

    val dynamicColors = with(preferences) {
        preferences[Gallery.DYNAMIC_COLORS].map {
            Preference(
                value = it,
                title = Text("Dynamic colors"),
                summery = Text("Toggle the usage of dynamic colors. This feature is available only on Android 12 or higher."),
                vector = Icons.Outlined.HideImage
            )
        }.asComposeState()
    }

    val useTrashCan = with(preferences) {
        preferences[Gallery.KEY_USE_TRASH_CAN].map {
            Preference(
                value = it,
                title = Text("Trash Can"),
                summery = Text("The preference key for enabling or disabling the trash can feature."),
                vector = Icons.Outlined.HideImage
            )
        }.asComposeState()
    }

    fun <S, O> set(key: Key<S, O>, value: O) {
        viewModelScope.launch { preferences[key] = value }
    }
}