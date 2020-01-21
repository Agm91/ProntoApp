package com.agm91.prontoapp.presentation.view

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.view.View
import androidx.databinding.DataBindingUtil
import com.agm91.prontoapp.BR
import com.agm91.prontoapp.R
import com.agm91.prontoapp.databinding.ViewMarkerBinding
import com.agm91.prontoapp.model.Results
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import com.squareup.picasso.Picasso

class CustomMarkerInfoView(private val context: Context) : GoogleMap.InfoWindowAdapter {
    lateinit var binding: ViewMarkerBinding

    private var marker: Marker? = null
    private var listener: GoogleMap.OnMarkerClickListener =
        (context as GoogleMap.OnMarkerClickListener)

    private val markerSet = mutableSetOf<String>()

    val target = object : com.squareup.picasso.Target {
        override fun onPrepareLoad(placeHolderDrawable: Drawable?) {}
        override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {}
        override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
            bitmap?.let { binding.image.addImage(it) }
            marker?.showInfoWindow()
        }
    }

    override fun getInfoContents(p0: Marker?): View {
        marker = p0
        binding = DataBindingUtil.inflate(
            (context as Activity).layoutInflater,
            R.layout.view_marker,
            null,
            false
        )
        val place: Results? = p0?.tag as Results?
        with(binding) {
            root.setOnClickListener {
                listener.onMarkerClick(marker)
            }
            setVariable(BR.place, place)
            executePendingBindings()
        }

        place?.photos?.forEach { photo ->
            val url = context.getString(
                R.string.photo_link,
                photo.photo_reference,
                photo.height,
                photo.width
            )

            if (!markerSet.contains(url)) {
                markerSet.add(url)
                Picasso.get().load(url).into(target)
            }
        }

        return binding.root
    }

    override fun getInfoWindow(p0: Marker?): View? {
        return null
    }
}