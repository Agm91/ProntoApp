package com.agm91.prontoapp.model

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.agm91.prontoapp.R
import com.google.gson.annotations.SerializedName
import com.squareup.picasso.Picasso
import com.stfalcon.multiimageview.MultiImageView

data class Photos(
    @SerializedName("height") val height: Int,
    @SerializedName("html_attributions") val html_attributions: List<String>,
    @SerializedName("photo_reference") val photo_reference: String,
    @SerializedName("width") val width: Int
) {
    companion object {
        @JvmStatic
        @BindingAdapter("loadPhotos")
        fun loadPhotos(view: MultiImageView, photos: List<Photos>?) {
            view.shape = MultiImageView.Shape.RECTANGLE
            val target = object : com.squareup.picasso.Target {
                override fun onPrepareLoad(placeHolderDrawable: Drawable?) {}
                override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {}
                override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                    bitmap?.let { view.addImage(it) }
                }

            }
            photos?.forEach { photo ->
                val url = view.context.getString(
                    R.string.photo_link,
                    photo.photo_reference,
                    photo.height,
                    photo.width
                )
                Picasso.get().load(url).into(target)
            }
        }

        @JvmStatic
        @BindingAdapter("loadUrlOnMultiImage")
        fun loadUrlOnMultiImage(view: MultiImageView, url: String?) {
            val target = object : com.squareup.picasso.Target {
                override fun onPrepareLoad(placeHolderDrawable: Drawable?) {}
                override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {}
                override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                    bitmap?.let { view.addImage(it) }
                }

            }
            Picasso.get().load(url).into(target)
        }

        @JvmStatic
        @BindingAdapter("loadUrlOnImage")
        fun loadUrlOnImage(view: ImageView, url: String?) {
            Picasso.get().load(url).into(view)
        }
    }
}