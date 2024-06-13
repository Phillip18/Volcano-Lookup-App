package com.example.volcanoes

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.navigation.fragment.findNavController

class MotionFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_motion, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        requireView().findViewById<Button>(R.id.start_button).setOnClickListener {
            findNavController().navigate(R.id.action_motion_to_map)
        }
        super.onViewCreated(view, savedInstanceState)
    }
}