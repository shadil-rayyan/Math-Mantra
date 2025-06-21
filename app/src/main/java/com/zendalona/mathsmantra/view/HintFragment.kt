package com.zendalona.mathsmantra.view

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.zendalona.mathsmantra.R
import com.zendalona.mathsmantra.databinding.FragmentHintBinding
import com.zendalona.mathsmantra.utility.excel.ExcelHintReader
import com.zendalona.mathsmantra.utility.settings.LocaleHelper.getLanguage

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



        val mode =
            if (getArguments() != null) requireArguments().getString("mode", "default") else "default"
        var language = getLanguage(requireContext())
        if (TextUtils.isEmpty(language)) language = "en"

        var hintText = ExcelHintReader.getHintFromExcel(requireContext(), language, mode)
        if (TextUtils.isEmpty(hintText)) {
            hintText = getString(R.string.hint_fallback)
        }

        binding!!.theoryText.setText(hintText)



        return binding!!.getRoot()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
