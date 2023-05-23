package com.prime.gallery.directory.local

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

typealias Viewer = ViewerViewModel.Companion

@HiltViewModel
class ViewerViewModel @Inject constructor(
    handle: SavedStateHandle,
    private val channel: SnackbarHostState,
) : ViewModel() {
    companion object {
        const val route = "_route_viewer"
    }
}


@Composable
fun Viewer(viewModel: ViewerViewModel) {

}