package com.zendalona.zmantra.presentation.features.hint

import android.os.Bundle
import android.text.TextUtils
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.zendalona.zmantra.R
import com.zendalona.zmantra.core.utility.excel.ExcelHintReader
import com.zendalona.zmantra.presentation.features.setting.util.LocaleHelper
import com.zendalona.zmantra.databinding.FragmentHintBinding

class HintFragment : Fragment() {
    private var binding: FragmentHintBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHintBinding.inflate(inflater, container, false)

        // Get mode and language
        val mode = if (arguments != null) requireArguments().getString("mode", "default") else "default"
        var language = LocaleHelper.getLanguage(requireContext())
        if (TextUtils.isEmpty(language)) language = "en"

        // Get the hint from Excel or fallback
        var hintText = ExcelHintReader.getHintFromExcel(requireContext(), language, mode)
        if (TextUtils.isEmpty(hintText)) {
            hintText = getString(R.string.hint_fallback)
        }

        // Convert the hint text into HTML
        val hintHtml = convertHintToHtml(hintText)

        // Load the generated HTML into the WebView
        binding!!.webView.loadDataWithBaseURL(null, hintHtml, "text/html", "UTF-8", null)

        // Set up the "Go Back" button click listener
        binding!!.goBackButton.setOnClickListener {

            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        return binding!!.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
    private fun getThemeColor(attrRes: Int): String {
        val typedValue = TypedValue()
        val theme = requireContext().theme
        theme.resolveAttribute(attrRes, typedValue, true)
        val colorInt = typedValue.data
        return String.format("#%06X", 0xFFFFFF and colorInt)
    }

    // Convert hint text to HTML format
    fun convertHintToHtml(hintText: String): String {
        val lines = hintText.split("\n").filter { it.isNotEmpty() }

        val backgroundColor = getThemeColor(com.google.android.material.R.attr.colorSecondary)
        val textColor = getThemeColor(com.google.android.material.R.attr.colorOnSecondary)

        val htmlBuilder = StringBuilder()
        htmlBuilder.append(
            """
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
                ul { padding-left: 20px; }
            </style>
        </head>
        <body>
        """.trimIndent()
        )

        lines.forEach { line ->
            when {
                line.contains("Objective", ignoreCase = true) -> {
                    htmlBuilder.append("<h2><strong>Objective of the Game</strong></h2>")
                    htmlBuilder.append("<p>$line</p>")
                }
                line.contains("Tips", ignoreCase = true) -> {
                    htmlBuilder.append("<h2><strong>Tips</strong></h2>")
                    htmlBuilder.append("<ul><li>$line</li></ul>")
                }
                line.contains("Accessibility Features", ignoreCase = true) -> {
                    htmlBuilder.append("<h2><strong>Accessibility Features</strong></h2>")
                    htmlBuilder.append("<p>$line</p>")
                }
                else -> {
                    htmlBuilder.append("<p>$line</p>")
                }
            }
        }

        htmlBuilder.append("</body></html>")
        return htmlBuilder.toString()
    }

}