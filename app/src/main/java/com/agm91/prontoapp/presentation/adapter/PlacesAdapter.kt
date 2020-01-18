package com.agm91.prontoapp.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.agm91.prontoapp.BR
import com.agm91.prontoapp.R
import com.agm91.prontoapp.databinding.FragmentListPlacesBinding
import com.agm91.prontoapp.databinding.ViewMarkerBinding
import com.agm91.prontoapp.model.Results

class PlacesAdapter :
    RecyclerView.Adapter<PlacesAdapter.ViewHolder>() {
    private var data = listOf<Results>()

    fun load(places: List<Results>) {
        data = places
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding =
            DataBindingUtil.inflate<ViewMarkerBinding>(
                layoutInflater,
                R.layout.view_marker,
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

    inner class ViewHolder(private val binding: ViewMarkerBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(place: Results) {
            with(binding) {
                setVariable(BR.place, place)
                executePendingBindings()
            }
        }
    }
}