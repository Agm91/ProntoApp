package com.agm91.prontoapp.presentation.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.agm91.prontoapp.R
import com.agm91.prontoapp.databinding.FragmentListPlacesBinding
import com.agm91.prontoapp.model.Results
import com.agm91.prontoapp.presentation.adapter.PlacesAdapter

class PlacesRecyclerFragment : Fragment() {
    private lateinit var binding: FragmentListPlacesBinding
    private val adapter = PlacesAdapter()

    override fun onAttach(context: Context) {
        //(context.applicationContext as MyApplication).appComponent.inject(this)
        super.onAttach(context)
    }

    fun load(places: List<Results>) {
        adapter.load(places)
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

    private fun bind() {
        binding.recycler?.adapter = adapter
        binding.showButton.setOnClickListener {
            if (binding.recycler.visibility == View.GONE) {
                binding.recycler.visibility = View.VISIBLE
                binding.showButton.setImageResource(android.R.drawable.arrow_down_float)
            }else {
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
