package com.prime.gallery.core

import android.content.ContentResolver
import android.net.Uri
import android.provider.MediaStore
import com.prime.gallery.core.api.MediaProvider
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
    private val provider: MediaProvider
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
    fun observe(uri: Uri) = provider.observe(uri)

    /**
     * @see MediaProvider.getMediaFiles
     */
    suspend fun getMediaFiles(
        query: String? = null,
        order: String = MediaStore.Audio.Media.TITLE,
        ascending: Boolean = true,
        offset: Int = 0,
        parent: String? = null,
        limit: Int = Int.MAX_VALUE
    ) = provider.getMediaFiles(query, order, ascending, parent, offset, limit)

    /**
     * @see MediaProvider.getFolders
     */
    suspend fun getFolders(
        filter: String? = null,
        ascending: Boolean = true,
    ) = provider.getFolders(filter, ascending)
}