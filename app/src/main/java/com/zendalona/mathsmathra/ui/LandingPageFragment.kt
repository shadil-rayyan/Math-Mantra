package com.zendalona.mathsmathra.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.appbar.MaterialToolbar
import com.zendalona.mathsmathra.R
import com.zendalona.mathsmathra.databinding.FragmentLandingPageBinding

class LandingPageFragment : Fragment() {

    private var _binding: FragmentLandingPageBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentLandingPageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Hide toolbar from activity
        (activity?.findViewById<MaterialToolbar>(R.id.toolbar))?.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Show toolbar again when fragment is destroyed
        (activity?.findViewById<MaterialToolbar>(R.id.toolbar))?.visibility = View.VISIBLE

        _binding = null
    }
}
