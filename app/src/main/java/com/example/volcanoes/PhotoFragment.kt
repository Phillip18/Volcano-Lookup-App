package com.example.volcanoes

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.volcanoes.databinding.FragmentPhotoBinding
import org.koin.android.ext.android.inject

class PhotoFragment : Fragment() {

    private lateinit var binding: FragmentPhotoBinding
    private lateinit var factory: Factory
    private lateinit var viewModel: MyViewModel
    private val savedStateHandle = SavedStateHandle()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_photo, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("status", "photo fragment started")
        setHasOptionsMenu(true)
        val toolbar =
            requireView().findViewById<androidx.appcompat.widget.Toolbar>(R.id.photo_toolbar)
        toolbar.setupWithNavController(findNavController())
        toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        factory = Factory(requireActivity().application, Repository(requireActivity().application), savedStateHandle)
        viewModel = ViewModelProvider(this, factory).get(MyViewModel::class.java)
        val imageUrl = arguments?.getString("imageUrl") ?: ""

        binding.photoLoading.visibility = View.VISIBLE
        viewModel.repository.downloadImage(
            this@PhotoFragment,
            imageUrl,
            binding.image
        ) {
            binding.photoLoading.visibility = View.INVISIBLE
        }
    }
}