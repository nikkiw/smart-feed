package com.core.image

import android.content.Context
import android.widget.ImageView

/**
 * Configuration options for loading images.
 *
 * @property placeholder Resource ID of the placeholder drawable to show while the image is loading.
 * @property error Resource ID of the error drawable to show if loading fails.
 * @property isCenterCrop Whether to apply center crop scaling to the image.
 * @property isCenterInside Whether to apply center inside scaling to the image.
 * @property isCircular Whether to transform the image into a circular shape.
 * @property isFitCenter Whether to apply fit center scaling to the image.
 * @property withFill Whether to enable filling the entire target view bounds (ignores aspect ratio).
 * @property overrideSize Optional width and height to override the loaded image dimensions (in pixels).
 */
data class ImageOptions(
    @androidx.annotation.DrawableRes
    val placeholder: Int = 0,
    @androidx.annotation.DrawableRes
    val error: Int = 0,
    val isCenterCrop: Boolean = false,
    val isCenterInside: Boolean = false,
    val isCircular: Boolean = false,
    val isFitCenter: Boolean = false,

    val withFill: Boolean = false,
    val overrideSize: Pair<Int, Int>? = null
)

/**
 * Contract for an image loading component that can load and preload images into views.
 */
interface ImageLoader {
    /**
     * Load an image into the given ImageView with the specified options.
     *
     * @param context Context used to access resources and lifecycle.
     * @param imageSource Source of the image (URL, file, resource, etc.).
     * @param imageView Target ImageView in which to display the loaded image.
     * @param options Options to customize how the image is loaded and displayed.
     */
    fun load(
        context: Context,
        imageSource: ImageSource,
        imageView: ImageView,
        options: ImageOptions = ImageOptions()
    )

    /**
     * Preload an image into cache without displaying it, for later display.
     *
     * @param context Context used to access resources and lifecycle.
     * @param imageSource Source of the image (URL, file, resource, etc.).
     * @param options Options to customize how the image is preloaded.
     */
    fun preload(
        context: Context,
        imageSource: ImageSource,
        options: ImageOptions = ImageOptions()
    )
}
