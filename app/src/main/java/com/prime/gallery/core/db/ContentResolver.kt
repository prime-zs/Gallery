package com.prime.gallery.core.db

import android.content.ContentResolver
import android.database.ContentObserver
import android.database.Cursor
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.compose.runtime.Stable
import com.primex.core.runCatching
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext as using

private const val TAG = "ContentResolver"


private const val DUMMY_SELECTION = "${MediaStore.Audio.Media._ID} != 0"

/**
 * An advanced of [ContentResolver.query]
 * @see ContentResolver.query
 * @param order valid column to use for orderBy.
 */
suspend fun ContentResolver.query2(
    uri: Uri,
    projection: Array<String>? = null,
    selection: String = DUMMY_SELECTION,
    args: Array<String>? = null,
    order: String = MediaStore.MediaColumns._ID,
    ascending: Boolean = true,
    offset: Int = 0,
    limit: Int = Int.MAX_VALUE
): Cursor? {
    return using(Dispatchers.Default) {
        // use only above android 10
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // compose the args
            val args2 = Bundle().apply {
                // Limit & Offset
                putInt(ContentResolver.QUERY_ARG_LIMIT, limit)
                putInt(ContentResolver.QUERY_ARG_OFFSET, offset)

                // order
                putStringArray(ContentResolver.QUERY_ARG_SORT_COLUMNS, arrayOf(order))
                putInt(
                    ContentResolver.QUERY_ARG_SORT_DIRECTION,
                    if (ascending) ContentResolver.QUERY_SORT_DIRECTION_ASCENDING else ContentResolver.QUERY_SORT_DIRECTION_DESCENDING
                )
                // Selection and groupBy
                if (args != null) putStringArray(ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS, args)
                // add selection.
                // TODO: Consider adding group by.
                // currently I experienced errors in android 10 for groupBy and arg groupBy is supported
                // above android 10.
                putString(ContentResolver.QUERY_ARG_SQL_SELECTION, selection)
            }
            query(uri, projection, args2, null)
        }
        // below android 0
        else {
            //language=SQL
            val order2 =
                order + (if (ascending) " ASC" else " DESC") + " LIMIT $limit OFFSET $offset"
            // compose the selection.
            query(uri, projection, selection, args, order2)
        }
    }
}

/**
 * @see query2
 */
internal suspend inline fun <T> ContentResolver.query2(
    uri: Uri,
    projection: Array<String>? = null,
    selection: String = DUMMY_SELECTION,
    args: Array<String>? = null,
    order: String = MediaStore.MediaColumns._ID,
    ascending: Boolean = true,
    offset: Int = 0,
    limit: Int = Int.MAX_VALUE,
    transform: (Cursor) -> T
): T? {
    return query2(uri, projection, selection, args, order, ascending, offset, limit)?.use(transform)
}


// data classes.
@Stable
data class Photo(
    @JvmField val id: Long,
    @JvmField val title: String,
    @JvmField val dateAdded: Long,
    @JvmField val dateModified: Long,
    @JvmField val dateTaken: Long,
    @JvmField val size: Int,
    @JvmField val mimeType: String,
    @JvmField val desc: String?,
    @JvmField val orientation: Int,
    @JvmField val height: Int,
    @JvmField val width: Int,
    @JvmField val data: String,
    @JvmField val latitude: Float,
    @JvmField val longitude: Float,
)

//language=SQL
private val PHOTO_PROJECTION =
    arrayOf(
        MediaStore.Images.ImageColumns._ID, // 0
        MediaStore.Images.ImageColumns.TITLE, // 1
        MediaStore.Images.ImageColumns.DATE_ADDED, // 2
        MediaStore.Images.ImageColumns.DATE_MODIFIED, // 3
        MediaStore.Images.ImageColumns.SIZE, // 4
        MediaStore.Images.ImageColumns.MIME_TYPE, // 5
        MediaStore.Images.ImageColumns.DESCRIPTION, // 6
        MediaStore.Images.ImageColumns.ORIENTATION, // 7
        MediaStore.Images.ImageColumns.HEIGHT,// 8
        MediaStore.Images.ImageColumns.WIDTH, // 9
        MediaStore.Images.ImageColumns.DATA, // 10
        MediaStore.Images.ImageColumns.DATE_TAKEN // 11
    )

private val ExifInterface.latLong: FloatArray
    get() {
        val array = FloatArray(2)
        getLatLong(array)
        return array
    }

private val Cursor.toPhoto: Photo
    inline get() {
        val path = getString(10)
        //val loc = runCatching(TAG) { ExifInterface(path).latLong }
        return Photo(
            id = getLong(0),
            title = getString(1),
            dateAdded = getLong(2) * 1000,
            dateModified = getLong(3) * 1000,
            size = getInt(4),
            mimeType = getString(5),
            desc = getString(6),
            orientation = getInt(7),
            height = getInt(8),
            width = getInt(9),
            data = path,
            dateTaken = getLong(11) * 1000,
            longitude = 0.0f,
            latitude =  0.0f
        )
    }

/**
 * @return [Audio]s from the [MediaStore].
 */
//@RequiresPermission(anyOf = [READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE])
suspend fun ContentResolver.getPhotos(
    filter: String? = null,
    order: String = MediaStore.Images.Media.DEFAULT_SORT_ORDER,
    ascending: Boolean = true,
    offset: Int = 0,
    limit: Int = Int.MAX_VALUE
): List<Photo> {
    return query2(
        uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        projection = PHOTO_PROJECTION,
        ascending = ascending,
        selection = DUMMY_SELECTION + if (filter != null) " AND ${MediaStore.Audio.Media.TITLE} LIKE ?" else "",
        args = if (filter != null) arrayOf("%$filter%") else null,
        order = order,
        offset = offset,
        limit = limit,
        transform = { c ->
            List(c.count) {
                c.moveToPosition(it);
                c.toPhoto
            }
        },
    ) ?: emptyList()
}

/**
 * Register [ContentObserver] for change in [uri]
 */
inline fun ContentResolver.register(uri: Uri, crossinline onChanged: () -> Unit): ContentObserver {
    val observer = object : ContentObserver(null) {
        override fun onChange(selfChange: Boolean) {
            onChanged()
        }
    }
    registerContentObserver(uri, false, observer)
    return observer
}

/**
 * Register an observer class that gets callbacks when data identified by a given content URI
 * changes.
 */
fun ContentResolver.observe(uri: Uri) = callbackFlow {
    val observer = object : ContentObserver(null) {
        override fun onChange(selfChange: Boolean) {
            trySend(selfChange)
        }
    }
    registerContentObserver(uri, true, observer)
    // trigger first.
    trySend(false)
    awaitClose {
        unregisterContentObserver(observer)
    }
}
