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
import com.zendalona.mathsmantra.utility.common.TTSUtility;
import com.zendalona.mathsmantra.utility.settings.DifficultyPreferences;
import com.zendalona.mathsmantra.utility.settings.LocaleHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
public class HintFragment extends Fragment {

    private FragmentHintBinding binding;
    private TTSUtility tts;
    private ArrayList<String> theoryContents;
    private int currentIndex = 0;
    private String lang;
    private String filepath;

    public HintFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tts = new TTSUtility(requireContext());
        tts.setSpeechRate(0.9f);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHintBinding.inflate(inflater, container, false);

        Bundle args = getArguments();
        filepath = (args != null) ? args.getString("filepath", "en/hint/default.txt") : "en/hint/default.txt";

        String fullHintText = loadHintFileAsText(filepath);
        binding.theoryText.setText(fullHintText);
        tts.speak(fullHintText);

        updateTheoryContent();

        binding.repeatButton.setOnClickListener(v -> tts.speak(binding.theoryText.getText().toString()));


        return binding.getRoot();
    }

    private void updateTheoryContent() {
        if (theoryContents == null || theoryContents.isEmpty()) return;
        String content = theoryContents.get(currentIndex);
        binding.theoryText.setText(content);
        tts.speak(content);
    }

    private String loadHintFileAsText(String filepath) {
        lang = LocaleHelper.getLanguage(requireContext());
        if (TextUtils.isEmpty(lang)) lang = "en";

        String fullPath = lang + "/" + filepath;
        StringBuilder builder = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(requireContext().getAssets().open(fullPath)))) {

            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line).append("\n");  // preserve line breaks
            }

        } catch (IOException e) {
            showToast("Failed to load hints from: " + fullPath);
            e.printStackTrace();
            return getString(R.string.hint_fallback);
        }

        return builder.toString().trim();
    }

    private void showToast(String msg) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        tts.shutdown();
        binding = null;
    }
}
