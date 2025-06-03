package com.core.image

import android.net.Uri
import androidx.annotation.DrawableRes

// 1. Description of the wrapper for different image sources
sealed class ImageSource {
    data class Url(val url: String) : ImageSource()               // external URL (http/https)
    data class Resource(@DrawableRes val resId: Int) : ImageSource() // resource from drawable or mipmap
    data class FilePath(val path: String) : ImageSource()         // local file path
    data class UriSource(val uri: Uri) : ImageSource()            // Uri (e.g., from gallery)
    object Empty : ImageSource()                                  // empty source (can show a placeholder)
}
