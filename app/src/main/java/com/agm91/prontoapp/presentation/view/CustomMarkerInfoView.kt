package com.agm91.prontoapp.presentation.view

import android.app.Activity
import android.content.Context
import android.view.View
import androidx.databinding.DataBindingUtil
import com.agm91.prontoapp.BR
import com.agm91.prontoapp.R
import com.agm91.prontoapp.databinding.ViewMarkerBinding
import com.agm91.prontoapp.model.Results
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso


class CustomMarkerInfoView(private val context: Context) : GoogleMap.InfoWindowAdapter {
    lateinit var binding: ViewMarkerBinding
    private var marker: Marker? = null

    private val markerSet = mutableSetOf<String>()

    private val listener = object : Callback {
        override fun onSuccess() {
            marker?.showInfoWindow()
        }

        override fun onError(e: Exception?) {
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
            setVariable(BR.place, place)
            executePendingBindings()
        }

        val url = context.getString(
            R.string.photo_link, place?.photos?.get(0)?.photo_reference,
            place?.photos?.get(0)?.height, place?.photos?.get(0)?.width
        )
        if (markerSet.contains(url)) {
            Picasso.get().load(url).into(binding.image)
        } else {
            markerSet.add(url)
            Picasso.get().load(url).into(binding.image, listener)
        }

        return binding.root
    }

    override fun getInfoWindow(p0: Marker?): View? {
        return null
    }
}