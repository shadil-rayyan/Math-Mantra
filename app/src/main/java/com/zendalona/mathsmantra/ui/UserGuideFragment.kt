package com.zendalona.mathsmantra.ui

import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.zendalona.mathsmantra.databinding.FragmentUserguideBinding
import com.zendalona.mathsmantra.R

class UserGuideFragment : Fragment() {

    private var _binding: FragmentUserguideBinding? = null
    private val binding get() = _binding!!

            override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserguideBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userGuideText = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            Html.fromHtml(getString(R.string.user_guide_text), Html.FROM_HTML_MODE_LEGACY)
        } else {
            Html.fromHtml(getString(R.string.user_guide_text))
        }

        binding.tvUserGuide.text = userGuideText

        binding.btnGoToTop.setOnClickListener {
            binding.scrollView.smoothScrollTo(0, 0)
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
