package com.darsuddeen.academy.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.darsuddeen.academy.databinding.FragmentDashBoardBinding

class DashBoardFragment : Fragment() {

    // ViewBinding reference
    private var _binding: FragmentDashBoardBinding? = null
    private val binding get() = _binding!!

    // Inflate layout and return root view
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashBoardBinding.inflate(inflater, container, false)

        // You can use binding.dashboardText.text = "Welcome" etc.

        return binding.root
    }

    // Clean up binding
    override fun onDestroyView() {
        super.onDestroyView()
        _binding=null
        }
}