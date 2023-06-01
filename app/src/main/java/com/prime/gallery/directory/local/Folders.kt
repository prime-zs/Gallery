package com.prime.gallery.directory.local

import android.provider.MediaStore
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import coil.compose.AsyncImage
import com.prime.gallery.LocalNavController
import com.prime.gallery.Material
import com.prime.gallery.R
import com.prime.gallery.core.ContentElevation
import com.prime.gallery.core.ContentPadding
import com.prime.gallery.core.Repository
import com.prime.gallery.core.api.Folder
import com.prime.gallery.core.compose.FolderShape
import com.prime.gallery.core.compose.Image
import com.prime.gallery.core.compose.Text
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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "FoldersViewModel"


typealias Folders = FoldersViewModel.Companion

private val Folder.firstTitleChar
    inline get() = name.uppercase()[0].toString()

@HiltViewModel
class FoldersViewModel @Inject constructor(
    handle: SavedStateHandle,
    private val repository: Repository,
    private val toaster: SnackbarHostState,
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
        meta = MetaData(Text(value ="Folders"))
        filter(ascending = false)
    }

    override fun toggleViewType() {
        // we only currently support single viewType. Maybe in future might support more.
        viewModelScope.launch {
            toaster.showSnackbar(
                "Coming soon",
                "Dismiss"
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
                    GroupBy.None -> mapOf(Text(value = "") to list)
                    GroupBy.Name -> list.groupBy { folder -> Text(value =folder.firstTitleChar) }
                    else -> error("$order invalid")
                }
            }
            // catch any exception.
            .catch {
                // any exception.
                toaster.showSnackbar(
                    "Some unknown error occurred!. ${it.message}",
                    "Error",
                    duration = SnackbarDuration.Indefinite
                )
            }
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyMap())
}


/**
 * The min size of the single cell in grid.
 */
private val MIN_TILE_SIZE = 96.dp
private val GridItemPadding =
    PaddingValues(vertical = 6.dp, horizontal = 4.dp)

private val FOLDER_SHAPE = RoundedCornerShape(20)

@Composable
fun Folder(
    value: Folder,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = Modifier
            // clip the ripple
            .clip(FOLDER_SHAPE)
            .then(modifier)
            // add padding after size.
            .padding(GridItemPadding)
            // wrap the height of the content
            .wrapContentHeight(),
    ) {
        val x = if (kotlin.random.Random.nextBoolean()) 2 * 0.04f else 0.04f
        Image(
            data = "file://${value.artwork}",
            modifier = Modifier
                .aspectRatio(1.0f)
                .padding(ContentPadding.small)
                // .clip()
                //.border(1.dp, Material.colors.onSurface, FOLDER_SHAPE)
                .shadow(ContentElevation.medium, FOLDER_SHAPE)
                .background(
                    Material.colorScheme.surfaceTint
                        .copy(x)
                        .compositeOver(Material.colorScheme.background)
                ),
            error = rememberVectorPainter(
                image = ImageVector.vectorResource(id = R.drawable.ic_error_24),
                //tintColor = Material.colors.onSurface.copy(ContentAlpha.medium),
            ),
        )

        // title
        Text(
            text = value.name,
            maxLines = 2,
            modifier = Modifier
                .padding(top = ContentPadding.medium, start = ContentPadding.medium)
                .align(Alignment.Start),
            style = Material.typography.labelMedium,
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
                    //navigator.navigate(Photos.direction(Photos.GET_FROM_FOLDER, folder.path))
                }
        )
    }
}


