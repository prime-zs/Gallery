package com.prime.gallery.directory.store

import android.net.Uri
import android.provider.MediaStore
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Slideshow
import androidx.compose.material.icons.outlined.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.prime.gallery.LocalNavController
import com.prime.gallery.Material
import com.prime.gallery.core.Repository
import com.prime.gallery.core.compose.ToastHostState
import com.prime.gallery.core.compose.show
import com.prime.gallery.core.db.Photo
import com.prime.gallery.core.util.FileUtils
import com.prime.gallery.directory.*
import com.prime.gallery.overlay
import com.primex.core.*
import com.primex.material2.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject
import com.primex.core.Text

private const val TAG = "AudiosViewModel"

private val Photo.firstTitleChar
    inline get() = title.uppercase()[0].toString()

typealias Photos = PhotosViewModel.Companion

@HiltViewModel
class PhotosViewModel @Inject constructor(
    handle: SavedStateHandle,
    private val toaster: ToastHostState,
    private val repository: Repository,
) : DirectoryViewModel<Photo>(handle) {

    companion object {

        const val GET_EVERY = "_every"
        const val GET_FROM_FOLDER = "_folder"

        private const val HOST = "_local_audios"

        private const val PARAM_TYPE = "_param_type"

        const val ORDER_BY_NONE = GroupBy.ORDER_BY_NONE
        const val ORDER_BY_NAME = GroupBy.ORDER_BY_NAME
        const val ORDER_BY_DATE_MODIFIED = GroupBy.ORDER_BY_DATE_MODIFIED
        const val ORDER_BY_DATE_ADDED = GroupBy.ORDER_BY_DATE_ADDED
        const val ORDER_BY_FOLDER = GroupBy.ORDER_BY_FOLDER

        val route = compose("$HOST/{${PARAM_TYPE}}")
        fun direction(
            of: String,
            key: String = NULL_STRING,
            query: String = NULL_STRING,
            order: String = NULL_STRING,
            ascending: Boolean = true,
            viewType: ViewType? = null
        ) = compose("$HOST/$of", Uri.encode(key), query, order, ascending, viewType)
    }

    override fun map(id: String): GroupBy {
        return when (id) {
            NULL_STRING -> GroupBy.Name
            else -> GroupBy.map(id)
        }
    }

    override fun viewType(id: String): ViewType {
        return when (id) {
            NULL_STRING -> ViewType.Grid
            else -> ViewType.map(id)
        }
    }

    private val type: String = handle.get<String>(PARAM_TYPE) ?: GET_EVERY


    override fun toggleViewType() {
        // we only currently support single viewType. Maybe in future might support more.
        viewModelScope.launch {
            toaster.show("Toggle not implemented/supported yet.", "ViewType")
        }
    }

    override val actions: List<Action> =
        mutableStateListOf(
            Action.Delete,
            Action.Share,
            Action.Properties,
        )

    init {
        meta = MetaData(
            Text(
                buildAnnotatedString {
                    append(
                        when (type) {
                            GET_EVERY -> "Photos"
                            GET_FROM_FOLDER -> "Folder"
                            else -> error("no such photo type $type.")
                        }
                    )
                    withStyle(SpanStyle(fontSize = 9.sp)) {
                        // new line
                        append("\n")
                        // name of the album.
                        append(
                            when (type) {
                                GET_EVERY -> "All Local Audio Files"
                                GET_FROM_FOLDER -> FileUtils.name(key)
                                else -> key
                            }
                        )
                    }

                }
            )
        )
    }

    override val orders: List<GroupBy> =
        listOf(GroupBy.None, GroupBy.Name, GroupBy.DateAdded, GroupBy.DateModified, GroupBy.Folder)
    override val mActions: List<Action?> =
        listOf(null, Action("slide_show", "Slide Show", Icons.Default.Slideshow))


    /**
     * Retrieves a list of audio sources based on the specified query, order, and sort order.
     *
     * @param query The search query used to filter the results.
     * @param order The property used to order the results.
     * @param ascending Whether to sort the results in ascending or descending order.
     * @return A list of audio sources based on the specified criteria.
     */
    private suspend fun source(query: String?, order: String, ascending: Boolean) =
        when (type) {
            GET_EVERY -> repository.getPhotos(query, order, ascending)
            GET_FROM_FOLDER -> TODO("Not Implemented yet!")
            else -> error("invalid type $type")
        }

    inline val GroupBy.toMediaOrder
        get() = when (this) {
            GroupBy.DateAdded -> MediaStore.Audio.Media.DATE_ADDED
            GroupBy.DateModified -> MediaStore.Audio.Media.DATE_MODIFIED
            GroupBy.Folder -> MediaStore.Audio.Media.DATA
            GroupBy.None, GroupBy.Name -> MediaStore.Audio.Media.TITLE
            else -> error("$this order not supported.")
        }


    override val data: Flow<Mapped<Photo>> =
        repository.observe(MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            .combine(filter) { f1, f2 -> f2 }
            .map {
                val (order, query, ascending) = it
                val list = source(query, order.toMediaOrder, ascending)

                // Don't know if this is correct place to emit changes to Meta.
                val latest = list.maxByOrNull { it.dateModified }
                meta = meta?.copy(
                    artwork = latest?.let { "file://${it.data}" },
                    cardinality = list.size,
                    dateModified = latest?.dateModified ?: -1
                )

                when (order) {
                    GroupBy.DateAdded -> TODO()
                    GroupBy.DateModified -> TODO()
                    GroupBy.Folder -> TODO()
                    GroupBy.Name -> list.groupBy { audio -> Text(audio.firstTitleChar) }
                    GroupBy.None -> mapOf(Text("") to list)
                    else -> error("$order invalid")
                }
            }
            .catch {
                // any exception.
                toaster.show(
                    "Some unknown error occured!.",
                    "Error",
                    leading = Icons.Outlined.Error,
                    accent = Color.Rose,
                    duration = ToastHostState.Duration.Indefinite
                )
            }
}

private val TILE_WIDTH = 100.dp


@Composable
private fun Photo(
    value: Photo,
    modifier: Modifier = Modifier,
    checked: Boolean = false
) {
    com.prime.gallery.core.compose.Image(
        data = "file://${value.data}",
        modifier = Modifier
            .padding(horizontal = 1.dp, vertical = 1.dp)
            .background(Material.colors.overlay)
            .fillMaxWidth()
            .aspectRatio(1.0f)
            .then(modifier)
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Photos(viewModel: PhotosViewModel) {
    val navigator = LocalNavController.current
    // calculate size based on the knowledge of the max size the item is going to take.

    Directory(
        viewModel = viewModel,
        cells = GridCells.Adaptive(TILE_WIDTH + (4.dp * 2)),
        onAction = {},
        key = { it.id },
    ) {
        Photo(it, modifier = Modifier.animateItemPlacement())
    }
}
