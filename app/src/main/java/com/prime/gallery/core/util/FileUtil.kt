package com.prime.gallery.core.util

import org.jetbrains.annotations.Contract

/**
 * A scope for [FileUtils] functions.
 */
object FileUtils {

    /**
     * The Unix separator character.
     */
    const val PATH_SEPARATOR = '/'

    /**
     * The extension separator character.
     * @since 1.4
     */
    const val EXTENSION_SEPARATOR = '.'

    /**
     * The pattern for hidden files and directories in Unix-based systems.
     */
    const val HIDDEN_PATTERN = "/."

    /**
     * Returns the name of the file without the path.
     *
     * @param path The path of the file.
     * @return The name of the file without the path.
     */
    fun name(path: String): String = path.substring(path.lastIndexOf(PATH_SEPARATOR) + 1)

    /**
     * Returns the parent directory of the file.
     *
     * @param path The path of the file.
     * @return The parent directory of the file.
     */
    fun parent(path: String): String = path.replace("$PATH_SEPARATOR${name(path = path)}", "")

    /**
     * Returns the extension of the file or null if there is no extension.
     *
     * @param url The URL of the file.
     * @return The extension of the file or null if there is no extension.
     */
    fun extension(url: String): String? =
        if (url.contains(EXTENSION_SEPARATOR))
            url.substring(url.lastIndexOf(EXTENSION_SEPARATOR) + 1).lowercase()
        else null

    /**
     * Checks if the file or any of its ancestors are hidden in the system.
     *
     * @param path The path of the file.
     * @return True if the file or any of its ancestors are hidden in the system, false otherwise.
     */
    @Contract(pure = true)
    fun areAncestorsHidden(path: String): Boolean = path.contains(HIDDEN_PATTERN)

}









