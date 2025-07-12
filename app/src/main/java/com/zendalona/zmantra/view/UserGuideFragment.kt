package com.zendalona.zmantra.view

import android.os.Build
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.setPadding
import androidx.fragment.app.Fragment
import com.zendalona.zmantra.databinding.FragmentUserguideBinding
import com.zendalona.zmantra.R
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

        val paragraphs = rawHtml.split("<br/><br/>").map { it.trim() }.filter { it.isNotEmpty() }

        val layoutInflater = LayoutInflater.from(requireContext())

        for (paragraph in paragraphs) {
            val textView = TextView(requireContext())
            val spannedText = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Html.fromHtml(paragraph, Html.FROM_HTML_MODE_LEGACY)
            } else {
                Html.fromHtml(paragraph)
            }

            textView.text = spannedText
            textView.setTextColor(resources.getColor(android.R.color.black, null))
            textView.textSize = 16f
            textView.setPadding(16)

            binding.llUserGuideContent.addView(textView)
        }

        binding!!.goBackButton.setOnClickListener {

            requireActivity().onBackPressed()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
