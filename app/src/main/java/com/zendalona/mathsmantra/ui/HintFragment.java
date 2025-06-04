package com.zendalona.mathsmantra.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.zendalona.mathsmantra.databinding.FragmentHintBinding;
import com.zendalona.mathsmantra.utility.common.TTSUtility;

import java.util.ArrayList;

public class HintFragment extends Fragment {

    private FragmentHintBinding binding;
    private TTSUtility tts;

    private ArrayList<String> theoryContents;

    private int currentIndex = 0;

    public HintFragment() {
        // Required empty public constructor
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
        Bundle args = getArguments();
        assert args != null;
        if(!args.isEmpty()){
            theoryContents = args.getStringArrayList("contents");
        }

        updateTheoryContent();

        binding.repeatButton.setOnClickListener(v -> tts.speak(binding.theoryText.getText().toString()));

        binding.previousButton.setOnClickListener(v -> {
            if(currentIndex == 0) currentIndex++;
            currentIndex = (currentIndex - 1) % theoryContents.size();
            updateTheoryContent();
        });

        binding.nextButton.setOnClickListener(v -> {
            currentIndex = (currentIndex + 1) % theoryContents.size();
            updateTheoryContent();
        });
        return binding.getRoot();
    }

    private void updateTheoryContent() {
        String content = theoryContents.get(currentIndex);
        binding.theoryText.setText(content);
        tts.speak(content);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        tts.shutdown();
    }
}