package com.core.image

import android.net.Uri
import androidx.annotation.DrawableRes

/**
 * Wrapper for different sources from which an image can be loaded.
 *
 * Represents various origins of an image, such as external URLs, drawable resources,
 * local files, URIs from content providers, or an empty placeholder source.
 */
sealed class ImageSource {
    /**
     * External URL source (http or https).
     *
     * @property url The web address of the image to load.
     */
    data class Url(val url: String) : ImageSource()

    /**
     * Drawable or mipmap resource from the app's resources.
     *
     * @property resId The resource ID of the drawable to load.
     */
    data class Resource(@DrawableRes val resId: Int) : ImageSource()

    /**
     * Local file path on device storage.
     *
     * @property path The filesystem path to the image file.
     */
    data class FilePath(val path: String) : ImageSource()

    /**
     * Content URI, e.g., from gallery or other content provider.
     *
     * @property uri The URI referencing the image content.
     */
    data class UriSource(val uri: Uri) : ImageSource()

    /**
     * Represents an empty image source, commonly used to display a placeholder.
     */
    object Empty : ImageSource()
}
