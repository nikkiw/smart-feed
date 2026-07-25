@file:Suppress("ktlint:standard:function-naming")

package com.feature.feed.component.list.ui.compose

import android.widget.ImageView
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.core.image.ImageLoader
import com.core.image.ImageOptions
import com.core.image.ImageSource
import com.feature.feed.R

@Composable
fun ArticleCard(
    model: ArticleCardUiModel,
    imageLoader: ImageLoader,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onImageViewAttached: (ImageView) -> Unit = {},
    onImageViewReleased: (ImageView) -> Unit = {},
) {
    Card(
        modifier =
            modifier
                .padding(8.dp)
                .fillMaxWidth()
                .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = 4.dp,
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            ArticleImage(
                imageUrl = model.imageUrl,
                imageLoader = imageLoader,
                onImageViewAttached = onImageViewAttached,
                onImageViewReleased = onImageViewReleased,
            )
            Text(
                text = model.title,
                style = MaterialTheme.typography.h6,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(bottom = 4.dp),
            )
            Text(
                text = model.shortDescription,
                style = MaterialTheme.typography.body2,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(bottom = 6.dp),
            )
            Text(
                text = model.date,
                style = MaterialTheme.typography.caption,
                modifier = Modifier.padding(bottom = 6.dp),
            )
            Row(
                modifier = Modifier.fillMaxWidth().clipToBounds().padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                model.tags.forEach { tag ->
                    Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colors.surface) {
                        Text(
                            text = tag,
                            style = MaterialTheme.typography.caption,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ArticleImage(
    imageUrl: String,
    imageLoader: ImageLoader,
    onImageViewAttached: (ImageView) -> Unit,
    onImageViewReleased: (ImageView) -> Unit,
) {
    val context = LocalContext.current
    val contentDescription = context.getString(R.string.item_article_image_content_description)
    AndroidView(
        modifier = Modifier.fillMaxWidth().height(180.dp).padding(bottom = 8.dp),
        factory = {
            ImageView(it).apply {
                scaleType = ImageView.ScaleType.CENTER_CROP
                this.contentDescription = contentDescription
                onImageViewAttached(this)
            }
        },
        update = { imageView ->
            if (imageUrl.isEmpty()) {
                imageLoader.load(
                    context = imageView.context,
                    imageSource = ImageSource.Empty,
                    imageView = imageView,
                )
            } else {
                imageLoader.load(
                    context = imageView.context,
                    imageSource = ImageSource.Url(imageUrl),
                    imageView = imageView,
                    options = ImageOptions(isCenterCrop = true),
                )
            }
        },
        onRelease = { imageView ->
            imageLoader.load(
                context = imageView.context,
                imageSource = ImageSource.Empty,
                imageView = imageView,
            )
            onImageViewReleased(imageView)
        },
    )
}
