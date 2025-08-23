package com.zendalona.zmantra.presentation.features.userguide

import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.zendalona.zmantra.databinding.FragmentUserguideBinding
import com.zendalona.zmantra.domain.model.HintIconVisibilityController
import com.zendalona.zmantra.presentation.features.setting.util.LocaleHelper
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UserGuideFragment : Fragment(), HintIconVisibilityController {

    override fun shouldShowHintIcon() = false

    private var binding: FragmentUserguideBinding? = null
    private val viewModel: UserGuideViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentUserguideBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val language = LocaleHelper.getLanguage(requireContext()).ifEmpty { "en" }
        val themeColors = Pair(
            getThemeColor(com.google.android.material.R.attr.colorSecondary),
            getThemeColor(com.google.android.material.R.attr.colorOnSecondary)
        )

        observeViewModel()
        viewModel.loadUserGuide(language, themeColors)

        binding!!.goBackButton.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun observeViewModel() {
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            if (state.isError) {
                binding?.webView?.loadData("<p>Error loading guide</p>", "text/html", "UTF-8")
            } else {
                state.styledHtml?.let {
                    binding?.webView?.loadDataWithBaseURL(null, it, "text/html", "UTF-8", null)
                }
            }
        }
    }

    private fun getThemeColor(attrRes: Int): String {
        val typedValue = TypedValue()
        requireContext().theme.resolveAttribute(attrRes, typedValue, true)
        val colorInt = typedValue.data
        return String.format("#%06X", 0xFFFFFF and colorInt)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
