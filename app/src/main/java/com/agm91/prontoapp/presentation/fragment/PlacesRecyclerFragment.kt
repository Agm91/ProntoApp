package com.agm91.prontoapp.presentation.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.agm91.prontoapp.R
import com.agm91.prontoapp.databinding.FragmentListPlacesBinding
import com.agm91.prontoapp.presentation.adapter.PlacesAdapter
import com.agm91.prontoapp.presentation.adapter.SnappingLinearLayoutManager
import com.google.android.gms.maps.model.Marker

class PlacesRecyclerFragment : Fragment() {
    private lateinit var binding: FragmentListPlacesBinding
    private lateinit var adapter: PlacesAdapter

    private var listener: PlacesAdapter.OnItemClick? = null

    private lateinit var markers: List<Marker>

    override fun onAttach(context: Context) {
        //(context.applicationContext as MyApplication).appComponent.inject(this)
        super.onAttach(context)
    }

    fun setListener(listener: PlacesAdapter.OnItemClick) {
        adapter = PlacesAdapter(listener)
        this.listener = listener
    }

    fun load(markers: List<Marker>) {
        this.markers = markers
        adapter.load(markers)
    }

    fun moveTo(marker: Marker) {
        binding.recycler.smoothScrollToPosition(markers.indexOf(marker))
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil
            .inflate(inflater, R.layout.fragment_list_places, container, false)
        bind()
        return binding.root
    }

    fun show() {
        binding.recycler.visibility = View.VISIBLE
        binding.showButton.setImageResource(android.R.drawable.arrow_down_float)
    }

    private fun bind() {
        binding.recycler.layoutManager =
            context?.let { SnappingLinearLayoutManager(it, LinearLayout.HORIZONTAL, false) }
        binding.recycler.adapter = adapter
        binding.showButton.setOnClickListener {
            if (binding.recycler.visibility == View.GONE) {
                binding.recycler.visibility = View.VISIBLE
                binding.showButton.setImageResource(android.R.drawable.arrow_down_float)
            } else {
                binding.recycler.visibility = View.GONE
                binding.showButton.setImageResource(android.R.drawable.arrow_up_float)
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(bundle: Bundle?) =
            PlacesRecyclerFragment().apply {
                arguments = bundle
                setHasOptionsMenu(true)
            }
    }
}
