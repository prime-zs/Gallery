package com.prime.gallery.directory.local

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.prime.gallery.LocalNavController
import com.prime.gallery.Material
import com.prime.gallery.R
import com.prime.gallery.core.Anim
import com.prime.gallery.core.ContentPadding
import com.prime.gallery.core.Repository
import com.prime.gallery.core.api.File
import com.prime.gallery.core.api.MediaProvider
import com.prime.gallery.core.compose.rememberVectorPainter
import com.prime.gallery.core.compose.withSpanStyle
import com.prime.gallery.core.util.DateUtil
import com.prime.gallery.core.util.FileUtils
import com.prime.gallery.directory.Action
import com.prime.gallery.directory.Directory
import com.prime.gallery.directory.DirectoryViewModel
import com.prime.gallery.directory.GroupBy
import com.prime.gallery.directory.Mapped
import com.prime.gallery.directory.MetaData
import com.prime.gallery.directory.ViewType
import com.primex.core.Text
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.roundToInt

private const val TAG = "PhotosViewModel"

/**
 * The first character of the title derived from the file name.
 * This property retrieves the first character of the file name, converts it to uppercase, and
 * returns it as a String.
 *
 * @return The first character of the file name as an uppercase String.
 */
private val File.firstTitleChar
    inline get() = name.uppercase()[0].toString()

typealias Images = ImagesViewModel.Companion

private inline fun MetaData(builder: (AnnotatedString.Builder).() -> Unit) =
    MetaData(title = Text(buildAnnotatedString(builder)))

// FixMe: Move impl to separate package.
@HiltViewModel
class ImagesViewModel @Inject constructor(
    handle: SavedStateHandle,
    private val toaster: SnackbarHostState,
    private val repository: Repository,
) : DirectoryViewModel<File>(handle) {

    companion object {

        const val GET_EVERY = "_every"
        const val GET_FROM_FOLDER = "_folder"

        private const val HOST = "_image_explorer"

        const val PARAM_TYPE = "_param_type"

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
            ascending: Boolean = false,
            viewType: ViewType? = null
        ) = compose("$HOST/$of", Uri.encode(key), query, order, ascending, viewType)
    }

    override fun map(id: String): GroupBy {
        return when (id) {
            NULL_STRING -> GroupBy.DateModified
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
            toaster.showSnackbar("ViewType: Toggle not implemented/supported yet.", "Dismiss")
        }
    }

    override val actions: List<Action> =
        mutableStateListOf(Action.Delete, Action.Share, Action.Properties)
    override val orders: List<GroupBy> =
        listOf(GroupBy.None, GroupBy.Name, GroupBy.DateAdded, GroupBy.DateModified, GroupBy.Folder)
    override val mActions: List<Action?> = emptyList()

    // After first initialization
    init {
        // emit ascending by default but only if it is not in the list.
        // TODO: Maybe make the directory interface as abstract class approach does't seem good enough.
        // make it descending by default.
        // FixMe: Change only if not passed through saved state.
        filter(ascending = false)
        meta = MetaData {
            //Title
            append(
                when (type) {
                    GET_EVERY -> "Photos"
                    GET_FROM_FOLDER -> "Folder"
                    else -> error("no such photo type $type.")
                }
            )
            withSpanStyle(fontSize = 9.sp, baselineShift = BaselineShift(0.8f)) {
                // new line
                append("\n")
                // name of the album.
                append(
                    when (type) {
                        GET_EVERY -> "All Local Image Files"
                        GET_FROM_FOLDER -> FileUtils.name(key)
                        else -> key
                    }
                )
            }
        }
    }

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
            GET_EVERY -> repository.getMediaFiles(query, order, ascending)
            GET_FROM_FOLDER -> repository.getMediaFiles(query, order, ascending, parent = key)
            else -> error("invalid type $type")
        }

    private inline val GroupBy.toMediaOrder
        get() = when (this) {
            GroupBy.DateAdded -> MediaStore.Images.Media.DATE_ADDED
            GroupBy.DateModified -> MediaStore.Images.Media.DATE_MODIFIED
            GroupBy.Folder -> MediaStore.Images.Media.DATA
            GroupBy.None, GroupBy.Name -> MediaStore.Images.Media.TITLE
            else -> error("$this order not supported.")
        }

    fun delete() {
        viewModelScope.launch {
            toaster.showSnackbar("Coming soon: This feature will be added soon.")
        }
    }

    fun selectAll() {
        viewModelScope.launch {
            val list = source(filter.value.second, MediaStore.Images.Media.DEFAULT_SORT_ORDER, true)
            list.forEach {
                val key = "${it.id}"
                if (!selected.contains(key))
                    select(key)
            }
        }
    }

    fun share(context: Context) {
        viewModelScope.launch {
            toaster.showSnackbar("Coming soon: This feature will be added soon.")
        }
    }

    override val data: Flow<Mapped<File>> =
        repository.observe(MediaProvider.EXTERNAL_CONTENT_URI) // observe for changes.
            .combine(filter) { f1, f2 -> f2 } // combine with flow
            .map { (order, query, ascending) -> // map
                val list = source(query, order.toMediaOrder, ascending)
                when (order) {
                    // Group the list of photos by the relative time span of their dateAdded
                    GroupBy.DateAdded -> list.groupBy { photo ->
                        Text(DateUtil.formatAsRelativeTimeSpan(photo.dateAdded))
                    }
                    // Group the list of photos by the relative time span of their dateModified
                    GroupBy.DateModified -> list.groupBy { photo ->
                        Text(DateUtil.formatAsRelativeTimeSpan(photo.dateModified))
                    }
                    // Group the list of photos by the parent folder name
                    GroupBy.Folder -> list.groupBy { photo ->
                        Text(FileUtils.name(FileUtils.parent(photo.path)))
                    }
                    // Group the list of photos by the first character of their title
                    GroupBy.Name -> list.groupBy { photo -> Text(photo.firstTitleChar) }
                    // Create a single group with an empty Text as the key
                    GroupBy.None -> mapOf(Text("") to list)
                    else -> error("$order invalid")
                }
            }
            .catch {// Any exception emit it as data.
                toaster.showSnackbar("Error: ${it.message}", "Dismiss")
            }
}

/**
 * The min size of the single cell in grid.
 */
private val TILE_WIDTH = 100.dp
private val SELECTED_SHAPE = RoundedCornerShape(13)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Images(viewModel: ImagesViewModel) {
    val navigator = LocalNavController.current
    // calculate size based on the knowledge of the max size the item is going to take.
    val selected = viewModel.selected
    val context = LocalContext.current
    val onPerformAction = { action: Action ->
        when (action) {
            // show dialog
            Action.Share -> viewModel.share(context)
            Action.Delete -> viewModel.delete()
            Action.SelectAll -> viewModel.selectAll()
            else -> error("Action: $action not supported.")
        }
    }

    //
    Directory(
        viewModel = viewModel,
        cells = GridCells.Adaptive(TILE_WIDTH + 8.dp),
        onAction = onPerformAction,
        key = { it.id },
        contentPadding = PaddingValues(horizontal = ContentPadding.small),
    ) { file ->

        val checked by remember { derivedStateOf { viewModel.selected.contains("${file.id}") } }
        Media(
            value = file, checked = checked, modifier = Modifier.combinedClickable(
                // selects: responsible for invoking selection logic.
                onLongClick = { viewModel.select("${file.id}") },
                //
                onClick = {
                    when {
                        // if the selection is non-empty i.e., active inoke it else do the normal click.
                        selected.isNotEmpty() -> viewModel.select("${file.id}")
                        // launch the viewer
                        /*  else -> navigator.navigate(
                              viewModel.direction(photo.data)
                          )*/
                    }
                },
            )
        )
    }
}

@Composable
@NonRestartableComposable
private fun Media(
    value: File,
    checked: Boolean,
    modifier: Modifier = Modifier
) {
    val sizePx = with(LocalDensity.current) { TILE_WIDTH.toPx().roundToInt() }
    val context = LocalContext.current
    val imageVectorChecked = rememberVectorPainter(
        image = ImageVector.vectorResource(id = R.drawable.ic_checked_24),
        tintColor = Material.colorScheme.primary.copy(0.65f)
    )

    val checkedModifier = if (checked) Modifier.drawWithContent {
        drawContent()
        with(imageVectorChecked) {
            draw(size)
        }
    }
    else Modifier
    val shade = Material.colorScheme.onSurface.copy(if (value.id % 2 == 0L) 0.04f else 0.03f)
    AsyncImage(
        contentDescription = value.name,
        error = rememberVectorPainter(
            image = ImageVector.vectorResource(id = R.drawable.ic_error_24),
            tintColor = Material.colorScheme.onSurface.copy(0.65f)
        ),
        contentScale = ContentScale.Crop,
        alignment = Alignment.Center,
        model = remember(value.id) {
            ImageRequest.Builder(context).data("file://${value.path}")
                .size(sizePx, sizePx)
                .crossfade(Anim.DefaultDurationMillis)
                .build()
        },
        modifier = Modifier
            .padding(horizontal = 1.dp, vertical = 1.dp)
            .then(checkedModifier)
            .background(shade)
            .scale(if (checked) 0.7f else 1f)
            .clip(if (checked) SELECTED_SHAPE else RectangleShape)
            .aspectRatio(1.0f)
            .then(modifier),
    )
}