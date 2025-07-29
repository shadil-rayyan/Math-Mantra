package com.zendalona.zmantra.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import com.zendalona.zmantra.R
import com.zendalona.zmantra.databinding.FragmentUserguideBinding
import com.zendalona.zmantra.model.HintIconVisibilityController

class UserGuideFragment : Fragment(), HintIconVisibilityController {

    override fun shouldShowHintIcon() = false

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

        val rawHtml = getString(R.string.user_guide_text)

        val webView = WebView(requireContext()).apply {
            settings.javaScriptEnabled = false
            settings.defaultTextEncodingName = "utf-8"
            settings.layoutAlgorithm = WebSettings.LayoutAlgorithm.TEXT_AUTOSIZING
            webViewClient = WebViewClient() // Prevent opening links externally
            loadDataWithBaseURL(null, rawHtml, "text/html", "utf-8", null)
            isFocusable = true
            isFocusableInTouchMode = true
            contentDescription = getString(R.string.user_guide_accessibility_text)
        }

        binding.llUserGuideContent.removeAllViews()
        binding.llUserGuideContent.addView(webView)

        binding.goBackButton.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
