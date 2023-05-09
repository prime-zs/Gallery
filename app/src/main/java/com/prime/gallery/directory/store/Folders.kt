package com.prime.gallery.directory.store

import android.provider.MediaStore
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.material.ContentAlpha
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BrokenImage
import androidx.compose.material.icons.outlined.Error
import androidx.compose.material.icons.outlined.Upcoming
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.prime.gallery.LocalNavController
import com.prime.gallery.Material
import com.prime.gallery.R
import com.prime.gallery.core.ContentElevation
import com.prime.gallery.core.ContentPadding
import com.prime.gallery.core.Repository
import com.prime.gallery.core.compose.Image
import com.prime.gallery.core.compose.ToastHostState
import com.prime.gallery.core.compose.rememberVectorPainter
import com.prime.gallery.core.compose.show
import com.prime.gallery.core.db.Folder
import com.prime.gallery.core.db.name
import com.prime.gallery.directory.Action
import com.prime.gallery.directory.Directory
import com.prime.gallery.directory.DirectoryViewModel
import com.prime.gallery.directory.GroupBy
import com.prime.gallery.directory.Mapped
import com.prime.gallery.directory.MetaData
import com.prime.gallery.directory.ViewType
import com.prime.gallery.overlay
import com.primex.core.Rose
import com.primex.core.Text
import com.primex.material2.Label
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

private const val TAG = "FoldersViewModel"


typealias Folders = FoldersViewModel.Companion

private val Folder.firstTitleChar
    inline get() = name.uppercase()[0].toString()

val FolderShape = GenericShape { size, _ ->
    val x = size.width
    val y = size.height
    val r = 0.1f * x
    val b = 0.4f * x

    moveTo(r, 0f)
    lineTo(b, 0f)
    lineTo(b + r, r)
    lineTo(x - r, r)
    quadraticBezierTo(x, r, x, 2 * r)
    lineTo(x, y - r)
    quadraticBezierTo(x, y, x - r, y)
    lineTo(r, y)
    quadraticBezierTo(0f, y, 0f, y - r)
    lineTo(0f, r)
    quadraticBezierTo(0f, 0f, r, 0f)
    close()
}

@HiltViewModel
class FoldersViewModel @Inject constructor(
    handle: SavedStateHandle,
    private val repository: Repository,
    private val toaster: ToastHostState,
) : DirectoryViewModel<Folder>(handle) {

    companion object {
        private const val HOST = "_local_folders"

        private const val ORDER_BY_NAME = GroupBy.ORDER_BY_NAME
        private const val ORDER_BY_NONE = GroupBy.ORDER_BY_NONE

        val route = compose(HOST)
        fun direction(
            query: String = NULL_STRING,
            order: String = NULL_STRING,
            ascending: Boolean = true,
            viewType: ViewType? = null,
        ) = compose(HOST, NULL_STRING, query, order, ascending, viewType)
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

    init {
        // emit the name to meta
        //TODO: Add other fields in future versions.
        meta = MetaData(Text("Folders"))
        filter(ascending = false)
    }

    override fun toggleViewType() {
        // we only currently support single viewType. Maybe in future might support more.
        viewModelScope.launch {
            toaster.show(
                Text(R.string.coming_soon_msg),
                Text(R.string.coming_soon),
                accent = Color.Red,
                leading = Icons.Outlined.Upcoming
            )
        }
    }

    override val actions: List<Action> = emptyList()
    override val orders: List<GroupBy> = listOf(GroupBy.None, GroupBy.Name)
    override val mActions: List<Action?> = emptyList()

    override val data: Flow<Mapped<Folder>> =
        repository
            // observe the changes to media-store
            .observe(MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            // observe the changes of filter params
            .combine(filter) { f1, f2 -> f2 }
            // map and emit the data
            .map {
                val (order, query, ascending) = it
                val list = repository.getFolders(query, ascending)
                when (order) {
                    GroupBy.None -> mapOf(Text("") to list)
                    GroupBy.Name -> list.groupBy { folder -> Text(folder.firstTitleChar) }
                    else -> error("$order invalid")
                }
            }
            // catch any exception.
            .catch {
                // any exception.
                toaster.show(
                    "Some unknown error occured!. ${it.message}",
                    "Error",
                    leading = Icons.Outlined.Error,
                    accent = Color.Rose,
                    duration = ToastHostState.Duration.Indefinite
                )
            }
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyMap())
}


/**
 * The min size of the single cell in grid.
 */
private val MIN_TILE_SIZE = 100.dp
private val GridItemPadding =
    PaddingValues(vertical = 6.dp, horizontal = 4.dp)

@Composable
fun Folder(
    value: Folder,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = Modifier
            // clip the ripple
            .clip(FolderShape)
            .then(modifier)
            // add padding after size.
            .padding(GridItemPadding)
            // add preferred size with min/max width
            //.then(Modifier.width(MIN_TILE_SIZE))
            // wrap the height of the content
            .wrapContentHeight(),
    ) {
        val x = if (kotlin.random.Random.nextBoolean()) 2 * 0.04f else 0.04f
        Image(
            data = "file://${value.artwork}",
            modifier = Modifier
                .aspectRatio(1.25f)
                .padding(ContentPadding.small)
                // .clip()
                .border(3.dp, Material.colors.onSurface, FolderShape)
                .shadow(ContentElevation.medium, FolderShape)
                .background(
                    Material.colors.overlay
                        .copy(x)
                        .compositeOver(Material.colors.background)
                ),
            error = rememberVectorPainter(
                image = Icons.Outlined.BrokenImage,
                defaultWidth = 16.dp,
                defaultHeight = 16.dp,
                tintColor = Material.colors.onSurface.copy(ContentAlpha.disabled)
            )
        )

        // title
        Label(
            text = value.name,
            maxLines = 2,
            modifier = Modifier
                .padding(top = ContentPadding.medium, start = ContentPadding.medium)
                .align(Alignment.Start),
            style = Material.typography.body2,
            fontWeight = FontWeight.Medium
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Folders(viewModel: FoldersViewModel) {
    val navigator = LocalNavController.current
    Directory(
        viewModel = viewModel,
        cells = GridCells.Adaptive(MIN_TILE_SIZE),
        onAction = {},
        key = { it.path },
        contentPadding = PaddingValues(horizontal = ContentPadding.small),
    ) { folder ->
        Folder(
            folder,
            modifier = Modifier
                .animateItemPlacement()
                .clickable {
                    navigator.navigate(Photos.direction(Photos.GET_FROM_FOLDER, folder.path)) {
                        // Pop up to the start destination of the graph to
                        // avoid building up a large stack of destinations
                        // on the back stack as users select items
                        popUpTo(navigator.graph.findStartDestination().id) {
                            saveState = true
                        }
                        // Avoid multiple copies of the same destination when
                        // re-selecting the same item
                        launchSingleTop = true
                        // Restore state when re-selecting a previously selected item
                        restoreState = true
                    }
                }
        )
    }
}