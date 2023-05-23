package com.prime.gallery.settings

import androidx.compose.material3.SnackbarHostState
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

typealias Settings = SettingsViewModel.Companion

@HiltViewModel
class SettingsViewModel @Inject constructor(
    handle: SavedStateHandle,
    private val channel: SnackbarHostState,
) : ViewModel() {
    companion object {
        const val route = "_route_settings"
    }
}