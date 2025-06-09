package com.image.glide

import android.content.Context
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.module.AppGlideModule

/**
 * Ensures that Glide's generated API is created for the Gallery sample.
 */
@GlideModule
class GlideApp : AppGlideModule() {
    override fun applyOptions(context: Context, builder: GlideBuilder) {
     // TODO preferences app settings
    }
}

