package com.image.glide

import android.content.Context
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.module.AppGlideModule

/**
 * Glide module to generate the Glide API for the Gallery sample.
 *
 * Applying custom options here allows configuration of
 * Glide’s behavior (e.g., memory and disk caching, logging).
 */
@GlideModule
class GlideApp : AppGlideModule() {

    /**
     * Called to apply options to the [GlideBuilder] before the generated API is initialized.
     *
     * @param context The application [Context].
     * @param builder The [GlideBuilder] used to configure Glide.
     * @see AppGlideModule.applyOptions
     */
    override fun applyOptions(context: Context, builder: GlideBuilder) {
        // TODO: Read and apply user preferences or app settings
    }
}

