package com.zendalona.zmantra.presentation.features.userguide

import android.os.Bundle
import android.text.TextUtils
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.R
import com.zendalona.zmantra.core.utility.settings.LocaleHelper
import com.zendalona.zmantra.databinding.FragmentUserguideBinding
import com.zendalona.zmantra.domain.model.HintIconVisibilityController

class UserGuideFragment : Fragment(), HintIconVisibilityController {

    override fun shouldShowHintIcon() = false

    private var binding: FragmentUserguideBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentUserguideBinding.inflate(inflater, container, false)

        // Get language setting
        var language = LocaleHelper.getLanguage(requireContext())
        if (TextUtils.isEmpty(language)) language = "en"

        // Load HTML from assets based on language
        val rawHtml = try {
            requireContext().assets.open("userguide/$language.html").bufferedReader().use { it.readText() }
        } catch (_: Exception) {
            "<p>Could not load user guide.</p>"
        }

        // Style it using theme colors
        val styledHtml = convertHtmlToStyledHtml(rawHtml)

        // Display in WebView
        binding!!.webView.loadDataWithBaseURL(null, styledHtml, "text/html", "UTF-8", null)

        // Set up the back button
        binding!!.goBackButton.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        return binding!!.root
    }

    private fun getThemeColor(attrRes: Int): String {
        val typedValue = TypedValue()
        val theme = requireContext().theme
        theme.resolveAttribute(attrRes, typedValue, true)
        val colorInt = typedValue.data
        return String.format("#%06X", 0xFFFFFF and colorInt)
    }

    private fun convertHtmlToStyledHtml(htmlBody: String): String {
        val backgroundColor = getThemeColor(R.attr.colorSecondary)
        val textColor = getThemeColor(R.attr.colorOnSecondary)


        return """
        <html>
        <head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
            <style>
                body {
                    background-color: $backgroundColor;
                    color: $textColor;
                    font-size: 18px;
                    font-family: sans-serif;
                    padding: 16px;
                }
                h2 { margin-top: 20px; }
                a { color: #64B5F6; }
            </style>
        </head>
        <body>
            $htmlBody
        </body>
        </html>
        """.trimIndent()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}