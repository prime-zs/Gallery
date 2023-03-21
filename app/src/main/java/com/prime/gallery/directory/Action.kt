package com.prime.gallery.directory

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.vector.ImageVector
import com.primex.core.Text

/**
 * The generic action Impl.
 */
private data class Generic(
    override val id: String,
    override val title: Text,
    override val icon: ImageVector
) : Action

/**
 * interface to represent a generic action in the app.
 * @property id: The unique identifier for the action, used to differentiate it from other actions.
 * @property title: The human-readable title for the action, displayed to the user.
 * @property icon: The icon representing the action, displayed to the user to help with visual recognition.
 * @author Zakir Sheikh.
 * @since 2023
 */
@Stable
sealed interface Action {

    val id: String
    val title: Text
    val icon: ImageVector

    companion object {
        operator fun invoke(id: String, title: Text, icon: ImageVector): Action =
            Generic(id, title, icon)

        operator fun invoke(id: String, title: String, icon: ImageVector): Action =
            Generic(id, Text(title), icon)


        val Share = Action("action_share", "Share", Icons.Outlined.Share)
        val Delete = Action("action_delete", "Delete", Icons.Outlined.Delete)
        val Edit = Action("action_edit", "Edit", Icons.Outlined.Edit)
        val Properties = Action("action_properties", "Properties", Icons.Outlined.Info)
        val SelectAll = Action("action_select_all", "Select All", Icons.Outlined.SelectAll)

    }
}

/**
 * Class to represent a "group by" action in the app.
 *
 * This class is marked as stable, indicating that it's expected to have a long lifespan
 * and should not change significantly.
 *
 * This class extends the [Action] class and represents a specific type of action, a "group by"
 * action. It's marked as a sealed class, meaning that it cannot have subclasses except for the
 * ones defined within the same file.
 *
 * An example of using a `DropDownItem` of `Compose` could look like this:
 * ```
 *     DropDownItem(
 *          value = action,
 *          onClick = {
 *              groupBy(GroupBy.Date)
 *          }
 *     )
 * ```
 * @author Zakir Sheikh
 * @since 2023-02-12
 * @see [Action]
 */

@Stable
data class GroupBy(
    override val id: String,
    override val title: Text,
    override val icon: ImageVector
) : Action {
    constructor(id: String, title: String, icon: ImageVector) : this(id, Text(title), icon)

    companion object {

        const val ORDER_BY_NONE = "order_by_none"
        const val ORDER_BY_NAME = "order_by_name"
        const val ORDER_BY_DATE_MODIFIED = "order_by_date_modified"
        const val ORDER_BY_DATE_ADDED = "order_by_date_added"
        const val ORDER_BY_FOLDER = "order_by_folder"
        const val ORDER_BY_DURATION = "order_by_duration"

        /**
         * GroupBy the title/Name of the item.
         */
        val None = GroupBy(ORDER_BY_NONE, "None", Icons.Outlined.FilterNone)
        val Name = GroupBy(ORDER_BY_NAME, "Name", Icons.Outlined.Title)
        val DateModified =
            GroupBy(ORDER_BY_DATE_MODIFIED, "Date Modified", Icons.Outlined.AccessTime)
        val DateAdded = GroupBy(ORDER_BY_DATE_ADDED, "Date Added", Icons.Outlined.CalendarMonth)
        val Folder = GroupBy(ORDER_BY_FOLDER, "Folder", Icons.Outlined.Folder)
        val Duration = GroupBy(ORDER_BY_DURATION, "Duration", Icons.Outlined.AvTimer)

        /**
         * Maps id with the predefined action.
         */
        fun map(id: String): GroupBy {
            return when (id) {
                ORDER_BY_NONE -> None
                ORDER_BY_DATE_ADDED -> DateAdded
                ORDER_BY_DATE_MODIFIED -> DateModified
                ORDER_BY_FOLDER -> Folder
                ORDER_BY_DURATION -> Duration
                ORDER_BY_NAME -> Name
                else -> error("No such GroupBy algo. $id")
            }
        }
    }
}

/**
 * Class to represent a "view type" action in the app.
 *
 * This class extends the [Action] class and represents a specific type of action, a "view type"
 * action. It's marked as a sealed class, meaning that it cannot have subclasses except for the ones
 * defined within the same file.
 *
 * An example of using this class could look like:
 *
 * ```
 *     val viewType = ViewType("view_type_id", Text("List view"), ImageVector(R.drawable.ic_list_view))
 *     changeViewType(viewType)
 * ```
 * @author: Zakir Sheikh
 * @since: 2023-02-12
 * @see: [Action]
 */
@Stable
sealed class ViewType(
    override val id: String,
    override val title: Text,
    override val icon: ImageVector
) : Action {

    private constructor(id: String, title: String, icon: ImageVector) : this(id, Text(title), icon)

    object List : ViewType(VIEW_TYPE_LIST, "List", Icons.Outlined.List)
    object Grid : ViewType(VIEW_TYPE_GRID, "Grid", Icons.Outlined.GridView)

    companion object {
        private val VIEW_TYPE_LIST = "view_type_list"
        private val VIEW_TYPE_GRID = "view_type_grid"

        /**
         * Maps the given `id` to a `ViewType` instance.
         *
         * This function takes a string `id` as input and returns a `ViewType` instance that
         * corresponds to that `id`. The mapping between `id` and `ViewType` instances is determined
         * by the implementation of this function. The `id` can be passed to the function using a
         * navigator, such as the `handle` method.
         *
         * @author: [Author Name]
         * @since: [Date]
         *
         * An example of using this function could look like:
         *
         * ```
         *     val viewTypeId = "list_view"
         *     val viewType = map(viewTypeId)
         *     changeViewType(viewType)
         * ```
         *
         * @param id The string `id` to map to a `ViewType` instance.
         * @return A `ViewType` instance that corresponds to the given `id`.
         */
        fun map(id: String): ViewType {
            return when (id) {
                VIEW_TYPE_LIST -> List
                VIEW_TYPE_GRID -> Grid
                else -> error("No such view type id: $id")
            }
        }
    }
}