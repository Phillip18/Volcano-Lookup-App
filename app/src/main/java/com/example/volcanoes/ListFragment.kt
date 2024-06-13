package com.example.volcanoes

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.koin.android.ext.android.inject

class ListFragment : Fragment() {

    private lateinit var toolbar: Toolbar
    private lateinit var factory: Factory
    private lateinit var viewModel: MyViewModel
    private val savedStateHandle = SavedStateHandle()
    private lateinit var adapter: VolcanoAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpToolbar()
        factory = Factory(requireActivity().application, Repository(requireActivity().application), savedStateHandle)
        viewModel = ViewModelProvider(this, factory).get(MyViewModel::class.java)
        adapter = VolcanoAdapter(viewModel.list.value!!)
        val recyclerView = requireView().findViewById<RecyclerView>(R.id.recycler_view)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireActivity())
        adapter.callback = { positionInList ->
            val volcano = viewModel.getVolcanoInList(positionInList)
            val bundle = Bundle()
            bundle.putString("name", volcano.name)
            bundle.putInt("height", volcano.height)
            bundle.putString("url", volcano.url)
            bundle.putString("imageUrl", volcano.imageUrl)
            findNavController().navigate(R.id.action_list_to_details, bundle)
        }
    }

    private fun setUpToolbar() {
        setHasOptionsMenu(true)
        toolbar = requireView().findViewById(R.id.list_toolbar)
        toolbar.setupWithNavController(findNavController())
        toolbar.inflateMenu(R.menu.menu)
        toolbar.menu.findItem(R.id.switchBetweenMapAndList).title = "Map"
        toolbar.setOnMenuItemClickListener {
            findNavController().navigate(R.id.action_list_to_map)
            true
        }
    }
}