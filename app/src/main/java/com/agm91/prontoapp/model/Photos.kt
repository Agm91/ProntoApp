package com.agm91.prontoapp.model

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.google.gson.annotations.SerializedName
import com.squareup.picasso.Picasso

data class Photos(
    @SerializedName("height") val height: Int,
    @SerializedName("html_attributions") val html_attributions: List<String>,
    @SerializedName("photo_reference") val photo_reference: String,
    @SerializedName("width") val width: Int
) {
    companion object {
        @JvmStatic
        @BindingAdapter("imageUrl")
        fun loadImage(view: ImageView, url: String) {
            Picasso.get().load(url).into(view)
        }
    }
}