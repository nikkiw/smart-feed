package com.image.glide

import android.content.Context
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.core.image.ImageLoader
import com.core.image.ImageOptions
import com.core.image.ImageSource

/**
 * Glide-based implementation of ImageLoader.
 */
class GlideImageLoader : ImageLoader {

    override fun load(
        context: Context,
        imageSource: ImageSource,
        imageView: ImageView,
        options: ImageOptions
    ) {
        // Build a RequestOptions object from ImageOptions
        val requestOptions = buildRequestOptions(options)

        // Determine the model to load based on ImageSource
        val glideRequest = when (imageSource) {
            is ImageSource.Url -> Glide.with(context).load(imageSource.url)
            is ImageSource.Resource -> Glide.with(context).load(imageSource.resId)
            is ImageSource.FilePath -> Glide.with(context).load(imageSource.path)
            is ImageSource.UriSource -> Glide.with(context).load(imageSource.uri)
            ImageSource.Empty -> {
                // If source is empty, just load placeholder or clear the view
                if (options.placeholder != 0) {
                    Glide.with(context)
                        .load(options.placeholder)
                        .apply(requestOptions)
                        .into(imageView)
                } else {
                    // Clear the ImageView if no placeholder is provided
                    Glide.with(context).clear(imageView)
                }
                return
            }
        }

        // Apply RequestOptions and into(imageView)
        glideRequest
            .apply(requestOptions)
            .into(imageView)
    }

    override fun preload(
        context: Context,
        imageSource: ImageSource,
        options: ImageOptions
    ) {
        val requestOptions = buildRequestOptions(options)

        when (imageSource) {
            is ImageSource.Url -> {
                Glide.with(context)
                    .load(imageSource.url)
                    .apply(requestOptions)
                    .preload()
            }

            is ImageSource.Resource -> {
                Glide.with(context)
                    .load(imageSource.resId)
                    .apply(requestOptions)
                    .preload()
            }

            is ImageSource.FilePath -> {
                Glide.with(context)
                    .load(imageSource.path)
                    .apply(requestOptions)
                    .preload()
            }

            is ImageSource.UriSource -> {
                Glide.with(context)
                    .load(imageSource.uri)
                    .apply(requestOptions)
                    .preload()
            }

            ImageSource.Empty -> {
                // Nothing to preload if source is empty
            }
        }
    }

    /**
     * Helper to build Glide RequestOptions from our ImageOptions.
     */
    private fun buildRequestOptions(options: ImageOptions): RequestOptions {
        var ro = RequestOptions()

        // Placeholder and error drawables
        if (options.placeholder != 0) {
            ro = ro.placeholder(options.placeholder)
        }
        if (options.error != 0) {
            ro = ro.error(options.error)
        }

        // Transformations: circleCrop, centerCrop, centerInside, fitCenter
        when {
            options.isCircular -> {
                ro = ro.circleCrop()
            }

            options.isCenterCrop -> {
                ro = ro.centerCrop()
            }

            options.isCenterInside -> {
                ro = ro.centerInside()
            }

            options.isFitCenter -> {
                ro = ro.fitCenter()
            }
        }

        // Override size if provided
        options.overrideSize?.let { (width, height) ->
            ro = ro.override(width, height)
        } ?: run {
            // If withFill is true but no explicit override, we let Glide decide to fill the view bounds
            if (options.withFill) {
                // Note: Glide's "override" is the only direct way to manipulate size; if withFill
                // is requested without explicit dimensions, one might rely on default behavior.
                // We'll skip override here and rely on centerCrop/fitCenter to fill.
            }
        }

        return ro
    }
}