package com.zendalona.mathsmantra.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.zendalona.mathsmantra.R;
import com.zendalona.mathsmantra.databinding.FragmentHintBinding;
import com.zendalona.mathsmantra.model.HintIconVisibilityController;
import com.zendalona.mathsmantra.utility.common.TTSUtility;
import com.zendalona.mathsmantra.utility.settings.LocaleHelper;
import com.zendalona.mathsmantra.utility.excel.ExcelHintReader;

public class HintFragment extends Fragment implements HintIconVisibilityController {

    private FragmentHintBinding binding;
    private TTSUtility tts;

    public HintFragment() {}

    @Override
    public boolean shouldShowHintIcon() {
        return false;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tts = new TTSUtility(requireContext());
        tts.setSpeechRate(0.9f);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHintBinding.inflate(inflater, container, false);

        String mode = getArguments() != null ? getArguments().getString("mode", "default") : "default";
        String language = LocaleHelper.getLanguage(requireContext());
        if (TextUtils.isEmpty(language)) language = "en";

        String hintText = ExcelHintReader.getHintFromExcel(requireContext(), language, mode);
        if (TextUtils.isEmpty(hintText)) {
            hintText = getString(R.string.hint_fallback);
        }

        binding.theoryText.setText(hintText);
        tts.speak(hintText);

        binding.repeatButton.setOnClickListener(v ->
                tts.speak(binding.theoryText.getText().toString()));

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        tts.shutdown();
        binding = null;
    }
}
