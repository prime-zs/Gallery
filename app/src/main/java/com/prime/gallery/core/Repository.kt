package com.prime.gallery.core

import android.content.ContentResolver
import android.content.ContentUris
import android.net.Uri
import android.provider.MediaStore
import androidx.annotation.WorkerThread
import com.prime.gallery.core.db.Photo
import com.prime.gallery.core.db.getPhotos
import com.prime.gallery.core.db.observe
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

/**
 * Repository class for managing Albums and related Photo files. This class is annotated with
 * `@ActivityRetainedScoped` and is designed to be used in Android app development.
 *
 * @property resolver An instance of the Android `ContentResolver` class used to access content providers,
 * such as the device's media store, to retrieve audio files.
 *
 * @constructor Creates a new `Repository2` object with the given `resolver` objects.
 */
@ActivityRetainedScoped
class Repository @Inject constructor(
    private val resolver: ContentResolver
) {

    /**
     * Register an observer class that gets callbacks when data identified by a given content URI
     * changes.
     *
     * @param uri The content URI for which to observe changes.
     *
     * Usage example:
     *
     * ```
     * contentResolver.observe(uri).collect {
     *     // handle changes here
     * }
     * ```
     *
     * @return A [Flow] that emits `true` when the content identified by the given URI changes, and
     * `false` otherwise.
     */
    fun observe(uri: Uri) = resolver.observe(uri)

    suspend fun getPhotos(
        query: String? = null,
        order: String = MediaStore.Audio.Media.TITLE,
        ascending: Boolean = true,
        offset: Int = 0,
        limit: Int = Int.MAX_VALUE
    ) = resolver.getPhotos(query, order, ascending, offset = offset, limit = limit)

    /**
     * Returns a list of photos that are contained within the specified folder.
     *
     * `Usage Example`
     * ```
     * val viewModel = ViewModelProvider(this).get(MyViewModel::class.java)
     * viewModel.getPhotosFromFolder("/storage/emulated/0/Music/MyPlaylist", query = "love").forEach { photo ->
     *     Log.d(TAG, "Phtot: ${audio.title}")
     * }
     * ```
     *
     * @param path The path to the folder to retrieve audios from.
     * @param query An optional search query to filter the audios.
     * @param order The order in which to sort the audios. Defaults to sorting by title.
     * @param ascending A Boolean indicating whether to sort the audios in ascending order. Defaults to true.
     *
     * @return A list of audios contained within the specified folder and matching the specified query and order.
     *
     * @throws SecurityException if the app doesn't have permission to access the audio content provider.
     */
    suspend fun getPhotosOfFolder(
        path: String,
        query: String? = null,
        order: String = MediaStore.Images.Media.DATE_MODIFIED,
        ascending: Boolean = true,
    ): List<Photo> = TODO("Not Implemented yet!!")

}