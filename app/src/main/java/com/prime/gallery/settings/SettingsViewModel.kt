package com.prime.gallery.settings


import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.HideImage
import androidx.compose.material.icons.outlined.Lightbulb
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
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject


        context (Preferences, ViewModel)

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
    private val preferences: Preferences
) : ViewModel() {

    companion object {
        const val route = "settings"
    }

    private fun <T> Flow<T>.asComposeState(): State<T> {
        val state = mutableStateOf(runBlocking { first() })
        onEach { state.value = it }.launchIn(viewModelScope)
        return state
    }

    val darkUiMode = with(preferences) {
        preferences[Gallery.NIGHT_MODE].map {
            Preference(
                value = it,
                title = Text("Dark Mode"),
                summery = Text("Click to change the app night/light mode."),
                vector = Icons.Outlined.Lightbulb
            )
        }.asComposeState()
    }

    
    val colorStatusBar = with(preferences) {
        preferences[Gallery.COLOR_STATUS_BAR].map {
            Preference(
                vector = null,
                title = Text("Color Status Bar"),
                summery = Text("Force color status bar."),
                value = it
            )
        }.asComposeState()
    }

    val hideStatusBar = with(preferences) {
        preferences[Gallery.HIDE_STATUS_BAR].map {
            Preference(
                value = it,
                title = Text("Hide Status Bar"),
                summery = Text("hide status bar for immersive view"),
                vector = Icons.Outlined.HideImage
            )
        }.asComposeState()
    }

    val forceAccent = with(preferences) {
        preferences[Gallery.FORCE_COLORIZE].map {
            Preference(
                value = it,
                title = Text("Force Accent Color"),
                summery = Text("Normally the app follows the rule of using 10% accent color. But if this setting is toggled it can make it use  more than 30%")
            )
        }.asComposeState()
    }


    fun <S, O> set(key: Key<S, O>, value: O) {
        viewModelScope.launch {
            preferences[key] = value
        }
    }
}

