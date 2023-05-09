package com.prime.gallery.core

import android.content.ContentResolver
import android.net.Uri
import android.provider.MediaStore
import com.prime.gallery.core.db.Photo
import com.prime.gallery.core.db.getFolders
import com.prime.gallery.core.db.getImages
import com.prime.gallery.core.db.getImagesOfFolder
import com.prime.gallery.core.db.observe
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.flow.Flow
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

    suspend fun getImages(
        query: String? = null,
        order: String = MediaStore.Audio.Media.TITLE,
        ascending: Boolean = true,
        offset: Int = 0,
        limit: Int = Int.MAX_VALUE
    ) = resolver.getImages(query, order, ascending, offset = offset, limit = limit)


    /**
     * Returns a list of [Folder]s that match the given [filter], ordered in either ascending or
     * descending order based on the [ascending] parameter.
     *
     * @param filter A string to filter the folders by, or null if no filtering is desired (default: null).
     * @param ascending A boolean indicating whether to sort the folders in ascending (true)
     * or descending (false) order (default: true).
     *
     * Usage Example:
     * ```
     * val folders: List<Folder> = getFolders("My Music", false)
     * ```
     *
     * @return A list of [Folder]s that match the given [filter], ordered in either ascending or
     * descending order based on the [ascending] parameter.
     *
     * @throws SecurityException If the app does not have permission to access the audio files.
     * @throws IllegalStateException If the [MediaResolver] is not initialized.
     */
    suspend fun getFolders(
        filter: String? = null,
        ascending: Boolean = true,
    ) = resolver.getFolders(filter, ascending)

    /**
     * Returns a list of photos that are contained within the specified folder.
     *
     * `Usage Example`
     * ```
     * val viewModel = ViewModelProvider(this).get(MyViewModel::class.java)
     * viewModel.getPhotosFromFolder("/storage/emulated/0/Music/MyPlaylist", query = "love").forEach { photo ->
     *     Log.d(TAG, "Photo: ${photo.title}")
     * }
     * ```
     *
     * @param path The path to the folder to retrieve audios from.
     * @param query An optional search query to filter the audios.
     * @param order The order in which to sort the audios. Defaults to sorting by title.
     * @param ascending A Boolean indicating whether to sort the audios in ascending order. Defaults to true.
     *
     * @return A list of photos contained within the specified folder and matching the specified query and order.
     *
     * @throws SecurityException if the app doesn't have permission to access the audio content provider.
     */
    suspend fun getImagesOfFolder(
        path: String,
        filter: String? = null,
        order: String = MediaStore.Images.Media.TITLE,
        ascending: Boolean = true,
        offset: Int = 0,
        limit: Int = Int.MAX_VALUE
    ): List<Photo> = resolver.getImagesOfFolder(path, filter, order, ascending, offset, limit)
}