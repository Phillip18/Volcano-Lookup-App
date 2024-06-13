package com.example.volcanoes

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.volcanoes.databinding.FragmentDetailsBinding

class DetailsFragment : Fragment() {

    private lateinit var binding: FragmentDetailsBinding
    private lateinit var factory: Factory
    private lateinit var viewModel: DetailViewModel
    private val savedStateHandle = SavedStateHandle()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_details, container, false)
        binding.detailsToolbar.setupWithNavController(findNavController())
        binding.detailsToolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        binding.name.text = arguments?.getString("name")
        binding.height.text = "${arguments?.getInt("height")} m"
        factory = Factory(
            requireActivity().application,
            Repository(requireActivity().application),
            savedStateHandle
        )
        viewModel = ViewModelProvider(this, factory).get(DetailViewModel::class.java)
        viewModel.text.observe(viewLifecycleOwner) { text ->
            if (text == "error") {
                binding.description.text = getString(R.string.error)
            } else {
                binding.description.text = text
            }
            binding.detailsLoading.visibility = View.INVISIBLE
        }
        viewModel.loading.observe(viewLifecycleOwner) { loading ->
            if (loading) {
                binding.detailsLoading.visibility = View.VISIBLE
            } else {
                binding.detailsLoading.visibility = View.INVISIBLE
            }
        }
        viewModel.getText(arguments?.getString("url").toString())
        binding.seeImage.setOnClickListener {
            val bundle = Bundle().apply {
                putString("imageUrl", arguments?.getString("imageUrl"))
            }
            findNavController().navigate(R.id.action_details_to_photo, bundle)
        }
        return binding.root
    }
}
