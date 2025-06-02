package com.zendalona.mathsmathra.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.appbar.MaterialToolbar
import com.zendalona.mathsmathra.R
import com.zendalona.mathsmathra.databinding.FragmentLandingPageBinding

interface FragmentNavigation {
    fun loadFragment(fragment: Fragment, transit: Int)
}

class LandingPageFragment : Fragment() {

    private var _binding: FragmentLandingPageBinding? = null
    private val binding get() = _binding!!
    private var navigationListener: FragmentNavigation? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentLandingPageBinding.inflate(inflater, container, false)

        binding.settings.setOnClickListener {
            navigationListener?.loadFragment(SettingFragment(), FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.findViewById<MaterialToolbar>(R.id.toolbar)?.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        activity?.findViewById<MaterialToolbar>(R.id.toolbar)?.visibility = View.VISIBLE
        _binding = null
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is FragmentNavigation) {
            navigationListener = context
        }
    }

    override fun onDetach() {
        super.onDetach()
        navigationListener = null
    }
}
