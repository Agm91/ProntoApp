package com.agm91.prontoapp.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.agm91.prontoapp.BR
import com.agm91.prontoapp.R
import com.agm91.prontoapp.databinding.ItemPlaceBinding
import com.agm91.prontoapp.model.Results
import com.google.android.gms.maps.model.Marker
import com.stfalcon.multiimageview.MultiImageView

class PlacesAdapter(val listener: OnItemClick) :
    RecyclerView.Adapter<PlacesAdapter.ViewHolder>() {
    private var data = listOf<Marker>()

    fun load(places: List<Marker>) {
        data = places
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding =
            DataBindingUtil.inflate<ItemPlaceBinding>(
                layoutInflater,
                R.layout.item_place,
                parent,
                false
            )
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(data[position])
    }

    inner class ViewHolder(private val binding: ItemPlaceBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(marker: Marker) {
            with(binding) {
                root.setOnClickListener {
                    listener.onItemClickListener(marker)
                }
                setVariable(BR.place, marker.tag as Results?)
                executePendingBindings()
            }
        }
    }

    interface OnItemClick {
        fun onItemClickListener(marker: Marker)
    }
}