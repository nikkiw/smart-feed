package com.core.image

import android.content.Context
import android.widget.ImageView

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

interface ImageLoader {
    fun load(
        context: Context,
        imageSource: ImageSource,
        imageView: ImageView,
        options: ImageOptions = ImageOptions()
    )

    fun preload(
        context: Context,
        imageSource: ImageSource,
        options: ImageOptions = ImageOptions()
    )
}