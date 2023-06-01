package com.prime.gallery.directory

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.foundation.lazy.grid.LazyGridItemSpanScope
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckBox
import androidx.compose.material.icons.outlined.CheckBoxOutlineBlank
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.RadioButtonChecked
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material.icons.outlined.ReplyAll
import androidx.compose.material.icons.outlined.Sort
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.prime.gallery.LocalNavController
import com.prime.gallery.Material
import com.prime.gallery.R
import com.prime.gallery.core.ContentPadding
import com.prime.gallery.core.compose.IconButton
import com.prime.gallery.core.compose.Placeholder
import com.prime.gallery.core.compose.Text
import com.prime.gallery.core.compose.stringResource
import com.primex.core.Text
import com.primex.core.drawHorizontalDivider
import com.primex.core.get
import com.primex.core.raw
import com.primex.core.rememberState
import com.primex.core.stringResource

/**
 * The visual representation of the [Action] as a DropDownMenu item.
 *
 * @param value The action to display.
 * @param modifier A modifier to apply to the action composable.
 * @param checked Indicates whether the action is checked or not.
 * @param enabled Indicates whether the action is enabled or not.
 * @param onAction A function that is called when the action is performed.
 *
 * Usage:
 * ```
 * DropDownMenu(
 *     toggle = { /* toggle menu */ },
 *     dropDownModifier = Modifier.padding(16.dp),
 *     expanded = state.expanded,
 *     dropDownContent = {
 *         Action(
 *             value = Action.Download,
 *             checked = state.checked,
 *             onAction = { /* handle action */ }
 *         )
 *         // other actions
 *     }
 * )
 * ```
 *
 * @author Zakir Sheikh
 * @since 2.0
 */
@Composable
@NonRestartableComposable
private fun Action(
    value: Action,
    modifier: Modifier = Modifier,
    checked: Boolean = false,
    enabled: Boolean = !checked,
    onAction: () -> Unit
) {
    val color = if (checked) Material.colorScheme.primary else LocalContentColor.current
    DropdownMenuItem(
        text = { Text(value.title.get) },
        onClick = onAction,
        modifier = modifier,
        enabled = enabled,
        colors = MenuDefaults.itemColors(color, color)
    )
}

/**
 * A composable that represents the toolbar for the directory.
 *
 * The toolbar shows the following actions:
 * 1. Navigation button (back button)
 * 2. Directory name
 * 3. Search view toggle
 * 4. View type toggle
 * 5. Group by
 * 6. More actions as defined in the `mActions` of the `viewModel`
 *
 * @param resolver The view model for the directory.
 * @param onAction A function that is called when an action is performed.
 * @param modifier A modifier to apply to the toolbar composable.
 *
 * Usage:
 * ```
 * Toolbar(
 *     resolver = viewModel,
 *     onAction = { action -> /* handle action */ },
 *     modifier = Modifier.fillMaxWidth()
 * )
 * ```
 *
 * @author Zakir Sheikh
 * @since 2.0
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T : Any> Toolbar(
    resolver: DirectoryViewModel<T>,
    onAction: (action: Action) -> Unit,
    modifier: Modifier = Modifier
) {
    //shown only if list meta is simple.
    val title = stringResource(value = resolver.meta?.title) ?: ""
    androidx.compose.material3.CenterAlignedTopAppBar(
        title = { Text(text = title, style = Material.typography.bodyLarge) },
        modifier = modifier,
        navigationIcon = {
            val navigator = LocalNavController.current
            IconButton(
                icon = Icons.Outlined.ReplyAll,
                contentDescription = null,
                onClick = {
                    // remove focus else navigateUp
                    if (resolver.focused.isNotBlank())
                        resolver.focused = ""
                    else
                        navigator.navigateUp()
                },
            )
        },
        actions = {
            // viewType
            // toggle between the different viewTypes.
            // maybe show message in toggleViewType corresponding messages.
            val viewType = resolver.viewType
            IconButton(
                onClick = { resolver.toggleViewType() },
                icon = viewType.icon,
                contentDescription = null,
                enabled = true
            )
            // SortBy
            var showOrderMenu by rememberState(initial = false)
            IconButton(
                onClick = { showOrderMenu = true },
                content = {
                    Icon(imageVector = Icons.Outlined.Sort, contentDescription = null)
                    val actions = resolver.orders
                    // search
                    // toggles on and off the search
                    // the search turns on off as soon as you push null or empty string in it.
                    val filter by resolver.filter.collectAsState()
                    DropdownMenu(
                        expanded = showOrderMenu,
                        onDismissRequest = { showOrderMenu = false },
                        content = {
                            // ascending descending logic
                            val ascending = filter.third
                            DropdownMenuItem(
                                text = { com.prime.gallery.core.compose.Text(text = "Ascending") },
                                onClick = {
                                    resolver.filter(ascending = !ascending); showOrderMenu = false
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = if (ascending) Icons.Outlined.CheckBox else Icons.Outlined.CheckBoxOutlineBlank,
                                        contentDescription = null
                                    )
                                },
                            )
                            Divider()
                            // The different sort-orders.
                            // TODO: Highlight the current selected action.
                            // also disable the click on current if it is selected.
                            actions.forEach {
                                val checked = filter.first.id == it.id
                                Action(value = it, checked = checked) {
                                    resolver.filter(order = it); showOrderMenu = false
                                }
                            }
                        }
                    )
                }
            )

            // The other actions/Main actions.
            // main actions excluding first as it will be shown as fab
            // only show if it is > 1 because fab exclusion.
            val actions = resolver.mActions
            val from = if (resolver.meta?.isSimple != false) 1 else 3
            if (actions.size > from) {
                var showActionMenu by rememberState(initial = false)
                IconButton(
                    onClick = { showActionMenu = true },
                    content = {
                        Icon(imageVector = Icons.Outlined.MoreVert, contentDescription = null)
                        // more options show in both cases if something is selected or not.
                        // if nothing is selected given condition will perform the action on all items
                        // else the selected ones.
                        DropdownMenu(
                            expanded = showActionMenu,
                            onDismissRequest = { showActionMenu = false },
                            content = {
                                for (i in from until actions.size) {
                                    val action = actions[i]
                                    // don't include null.
                                    if (action != null)
                                        Action(action) {
                                            onAction(action)
                                            showActionMenu = false
                                        }
                                }
                            }
                        )
                    }
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp))
    )
}


/**
 * A composable that represents the action bar for the directory.
 *
 * The action bar shows the actions available for the selected items in the directory.
 * It is only shown when the selected items count in the [DirectoryViewModel] is greater than 0.
 * * The navigation button is a cross that clears the selection.
 * * The count displays the number of selected items.
 * * The `action1` and `action2` are the first 2 actions from the actions list in [DirectoryViewModel].
 * * and more is only shown when actions.size > 2
 *
 * @param resolver The view model for the directory.
 * @param modifier A modifier to apply to the action bar composable.
 * @param onAction A function that is called when an action is performed.
 *
 * Usage:
 * ```
 * ActionBar(
 *     resolver = viewModel,
 *     onAction = { action -> /* handle action */ },
 *     modifier = Modifier.fillMaxWidth()
 * )
 * ```
 *
 * @author Zakir Sheikh
 * @since 2.0
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T : Any> ActionBar(
    resolver: DirectoryViewModel<T>,
    modifier: Modifier = Modifier,
    onAction: (action: Action) -> Unit
) {
    androidx.compose.material3.TopAppBar(
        title = {
            val count = resolver.selected.size
            Text(text = "$count selected", style = Material.typography.bodyLarge)
        },
        modifier = modifier.padding(top = ContentPadding.medium),
        // here the navigation icon is the clear button.
        // clear selection if selected > 0
        navigationIcon = {
            IconButton(
                onClick = { resolver.clear() },
                icon = Icons.Outlined.Close,
                contentDescription = null
            )
        },
        // the action that needs to be shown
        // when selection is > 0
        actions = {
            val actions = resolver.actions
            //first action.
            val first = actions.getOrNull(0)
            if (first != null)
                IconButton(
                    onClick = { onAction(first) },
                    icon = first.icon,
                    contentDescription = null
                )
            // second action.
            val second = actions.getOrNull(1)
            if (second != null)
                IconButton(
                    onClick = { onAction(second) },
                    icon = second.icon,
                    contentDescription = null,
                )
            // The remain actions.
            // only show if it is > 2
            if (actions.size > 2) {
                var showActionMenu by rememberState(initial = false)
                IconButton(onClick = { showActionMenu = true }) {
                    Icon(imageVector = Icons.Outlined.MoreVert, contentDescription = null)
                    // more options show in both cases if something is selected or not.
                    // if nothing is selected given condition will perform the action on all items
                    // else the selected ones.
                    DropdownMenu(
                        expanded = showActionMenu,
                        onDismissRequest = { showActionMenu = false },
                        content = {
                            for (i in 2 until actions.size) {
                                val action = actions[i]
                                // don't include null.
                                Action(value = action) { onAction(action); showActionMenu = false }
                            }
                        }
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.secondary)
    )
}

/**
 * A composable that represents the top app bar for the directory.
 *
 * The top app bar displays either the toolbar or the action bar based on whether selected is greater
 * than 0 in [DirectoryViewModel].
 */
@Composable
private fun <T : Any> TopAppBar(
    resolver: DirectoryViewModel<T>,
    onAction: (action: Action) -> Unit,
    modifier: Modifier = Modifier,
) {
    val showActionBar by remember { derivedStateOf { resolver.selected.isNotEmpty() } }
    // show/ hide action bar
    Crossfade(
        targetState = showActionBar,
        modifier = modifier,
        label = "directory_top_bar"
    ) { show ->
        when (show) {
            true -> ActionBar(resolver, onAction = onAction)
            else -> Toolbar(resolver, onAction = onAction)
        }
    }
}

/**
 * Item header.
 * //TODO: Handle padding in parent composable.
 */
@Composable
@NonRestartableComposable
private fun Header(
    value: Text,
    modifier: Modifier = Modifier
) {
    val title = stringResource(value = value)
    if (title.isBlank())
        return Spacer(modifier = modifier)

    val color = LocalContentColor.current
    Crossfade(
        targetState = title.length == 1,
        modifier = Modifier
            .padding(bottom = ContentPadding.normal)
            .drawHorizontalDivider(color = color)
            .padding(bottom = ContentPadding.small)
            .fillMaxWidth()
            .then(modifier),
        label = "directory_content_text_header"
    ) { single ->
        when (single) {
            // draw a single char/line header
            // in case the length of the title string is 1
            true -> Text(
                text = title,
                style = Material.typography.displayLarge,
                fontWeight = FontWeight.Bold,
                color = color,
                modifier = Modifier
                    .padding(top = ContentPadding.normal)
                    .padding(horizontal = ContentPadding.large),
            )
            // draw a multiline line header
            // in case the length of the title string is 1
            else -> Text(
                text = title,
                color = color,
                maxLines = 2,
                fontWeight = FontWeight.Normal,
                style = Material.typography.headlineSmall,
                modifier = Modifier
                    // don't fill whole line.
                    .fillMaxWidth(0.7f)
                    .padding(top = ContentPadding.large, bottom = ContentPadding.medium)
                    .padding(horizontal = ContentPadding.normal)
            )
        }
    }
}

// recyclable items.
private const val CONTENT_TYPE_HEADER = "_header"
private const val CONTENT_TYPE_LIST_META = "_list_meta"
private const val CONTENT_TYPE_LIST_ITEM = "_list_item"
private const val CONTENT_TYPE_BANNER = "_banner_ad"
private const val CONTENT_TYPE_SEARCH_VIEW = "_search_view"
private const val CONTENT_TYPE_PINNED_SPACER = "_pinned_spacer"


private val fullLineSpan: (LazyGridItemSpanScope.() -> GridItemSpan) =
    { GridItemSpan(maxLineSpan) }

@OptIn(ExperimentalFoundationApi::class)
private inline fun <T> LazyGridScope.content(
    items: Mapped<T>,
    noinline key: ((item: T) -> Any)? = null,
    noinline itemContent: @Composable LazyGridItemScope.(T) -> Unit
) {
    // actual list
    items.forEach { (header, list) ->
        //emit  list header
        item(
            key = header.raw,
            contentType = CONTENT_TYPE_HEADER,
            span = fullLineSpan,
            content = {
                Header(
                    value = header,
                    modifier = Modifier.animateItemPlacement()
                )
            }
        )

        // emit list of items.
        items(
            list,
            key = if (key == null) null else { item -> key(item) },
            contentType = { CONTENT_TYPE_LIST_ITEM },
            itemContent = { item ->
                itemContent(item)
            }
        )
    }
}

/**
 * A composable that displays a list of items as grid of cells.
 *
 * @param modifier The modifier to be applied to the list.
 * @param resolver The [DirectoryViewModel] for the list.
 * @param cells The number of cells per row.
 * @param key A key function to extract a unique identifier from the item.
 * @param itemContent The composable function that represents each item in the list.
 */
@Suppress("UNCHECKED_CAST")
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun <T : Any> List(
    modifier: Modifier = Modifier,
    resolver: DirectoryViewModel<T>,
    cells: GridCells,
    key: ((item: T) -> Any)? = null,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    onAction: (action: Action) -> Unit,
    itemContent: @Composable LazyGridItemScope.(T) -> Unit
) {
    //TODO: Currently we are only representing the items. However, this logic will be moved
    //outside in the future.
    //Currently, we only support mapped data, but we aim to support paged data in the future."

    val data by resolver.data.collectAsState(initial = null) as State<Mapped<T>?>

    // The data can be in the following cases:
    // Case 1: data is null, which means the initial loading state.
    // Case 2: data is empty, which means there is no literary content available.
    // Case 3: data contains a list of items to be plotted.
    // Additionally, apply a fade effect when transitioning between states.
    val state by remember {
        derivedStateOf {
            when {
                data == null -> 0 // initial loading
                data.isNullOrEmpty() -> 1// empty state
                else -> 2// normal state.
            }
        }
    }

    // plot the list
    val map = data ?: emptyMap()
    LazyVerticalGrid(
        columns = cells,
        modifier = modifier,
        contentPadding = contentPadding
    ) {
        // used to pin the list to top.
        // such data search bar is not opened in hiding.
        item(
            contentType = CONTENT_TYPE_PINNED_SPACER,
            span = fullLineSpan
        ) {
            Spacer(modifier = Modifier.padding(ContentPadding.small))
        }

        // actual content
        content(map, key, itemContent)

        // show placeholders as required.
        item(span = fullLineSpan, contentType = "list_state") {
            when (state) {
                0 -> Placeholder(
                    title = "Loading",
                    iconResId = R.raw.lt_loading_dots_blue,
                    modifier = Modifier.wrapContentHeight()
                )

                1 -> Placeholder(
                    title = "Oops Empty!!",
                    iconResId = R.raw.lt_empty_box,
                    modifier = Modifier.wrapContentHeight()
                )
            }
        }
    }
}

/**
 * Represents the abstract directory.
 *
 * @param viewModel The view model associated with the directory.
 * @param cells The number of cells in the grid of the directory.
 * @param key A function that returns a unique key for each item in the directory.
 * @param onAction A function that is called when an action occurs.
 * @param itemContent A composable function that displays the content of each item in the directory.
 *
 * The reasons for abstracting the directory are as follows:
 * 1. To ensure that common features such as the toolbar, search query, order header, actions, etc.
 * are present in each directory.
 * 2. To ensure that a single type is represented by a single directory, making the logic simple and
 * easy. This also ensures that a single layout needs to be implemented among different directories,
 * with only the layout of the individual items needing to be changed.
 *
 * The directory supports the following features:
 *
 * - Toolbar
 * - ActionBar shown when selected items count is greater than 0
 * - SearchView: Shown when the query is non-null.
 * - Selection of ViewTypes.
 *
 * Note: The directory is only available in portrait mode. The data must be provided as either
 * a paged list (upcoming) or a map of `Text` to a list of `T`.
 *
 * Usage:
 * ```
 * Directory(
 *     viewModel = myViewModel,
 *     cells = GridCells.Square,
 *     key = { item -> item.id },
 *     onAction = { action -> /* handle action */ },
 *     itemContent = { item -> /* display item content */ }
 * )
 * ```
 *
 * @author Zakir Sheikh
 * @since 2.0
 *
 * @see [DirectoryViewModel]
 */
@Composable
fun <T : Any> Directory(
    viewModel: DirectoryViewModel<T>,
    cells: GridCells,
    key: ((item: T) -> Any)? = null,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    onAction: (action: Action) -> Unit,
    itemContent: @Composable LazyGridItemScope.(T) -> Unit,
) {
    // collapse if expanded and
    // back button is clicked.
    // TODO: Maybe check if filter is null or not.
    BackHandler(viewModel.selected.isNotEmpty() || viewModel.focused.isNotBlank()) {
        if (viewModel.focused.isNotBlank())
            viewModel.focused = ""
        else if (viewModel.selected.isNotEmpty())
            viewModel.clear()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                resolver = viewModel,
                onAction = onAction
            )
        },
        // mainAction
        floatingActionButton = {
            // main action
            val action = viewModel.mActions.firstOrNull()
            if (action != null) {
                // actual content
                FloatingActionButton(
                    onClick = { onAction(action) },
                    shape = RoundedCornerShape(30),
                    content = {
                        // the icon of the Fab
                        Icon(
                            imageVector = action.icon,
                            contentDescription = null
                        )
                    }
                )
            }
        },
        content = {
            List(
                resolver = viewModel,
                cells = cells,
                modifier = Modifier.padding(it),
                contentPadding = contentPadding,
                itemContent = itemContent,
                key = key,
                onAction = onAction
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    )
}