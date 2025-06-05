package com.zendalona.mathsmantra.ui.game;

import static android.text.Selection.moveLeft;
import static android.text.Selection.moveRight;

import android.app.AlertDialog;
import android.content.pm.ActivityInfo;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.zendalona.mathsmantra.R;
import com.zendalona.mathsmantra.databinding.DialogResultBinding;
import com.zendalona.mathsmantra.databinding.FragmentGameNumberLineBinding;
import com.zendalona.mathsmantra.Enum.Topic;
import com.zendalona.mathsmantra.utility.RandomValueGenerator;
import com.zendalona.mathsmantra.utility.common.TTSUtility;
import com.zendalona.mathsmantra.viewModel.NumberLineViewModel;

import java.util.HashMap;
import java.util.Map;

public class NumberLineFragment extends Fragment {

    private FragmentGameNumberLineBinding binding;
    private NumberLineViewModel viewModel;
    private TTSUtility tts;
    private RandomValueGenerator random;
    private final String CURRENT_POSITION = "You're standing on number : \n";
    private int answer;
    private String questionDesc = "";
    private String correctAnswerDesc = "";

    public NumberLineFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(NumberLineViewModel.class);
        tts = new TTSUtility(requireContext());
        tts.setSpeechRate(0.8f);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Lock orientation to landscape when this fragment is visible
//        requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        viewModel.reset();
    }

    @Override
    public void onPause() {
        super.onPause();
//        requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentGameNumberLineBinding.inflate(inflater, container, false);
//        tts.speak("You're standing on the start of number line, at position 0.");

        random = new RandomValueGenerator();
        setupObservers();

        correctAnswerDesc = askNewQuestion(0);

        binding.numberLineQuestion.setOnClickListener(v -> tts.speak(questionDesc));
        binding.btnLeft.setOnClickListener(v -> {
            viewModel.moveLeft();
            binding.numberLineView.moveLeft();
        });
        binding.btnRight.setOnClickListener(v -> {
            viewModel.moveRight();
            binding.numberLineView.moveRight();
        });
//        setupTouchListener();


        return binding.getRoot();
    }
//    private void setupTouchListener() {
//        binding.getRoot().setOnTouchListener((v, event) -> {
//            if (talkBackEnabled) {
//                handleTwoFingerSwipe(event);
//                return true;
//            }
//            return false;
//        });
//    }

    private String askNewQuestion(int position) {

        Topic topic = random.generateNumberLineQuestion()? Topic.ADDITION : Topic.SUBTRACTION;
        int unitsToMove = random.generateNumberForCountGame();
        String operator = " plus ";
        String direction = " right ";
        Map<String, String> operatorMap = new HashMap<>();
        operatorMap.put("plus","+");
        operatorMap.put("minus","-");

        switch (topic){
            case ADDITION:
                operator = " plus ";
                direction = " right ";
                answer = position + unitsToMove;
                break;
            case SUBTRACTION:
                operator = " minus ";
                direction = " left ";
                answer = position - unitsToMove;
                break;
        }
        String questionBrief = "What is " + position + operatorMap.get(operator.trim()) + unitsToMove + "?";
        binding.numberLineQuestion.setText(questionBrief);
        questionDesc =
                "You're standing on " + position + "."
                        + "What is  " + position + operator + unitsToMove + "?"
                        + "Move  " + unitsToMove + "units to the" + direction + "of Number line.";

        tts.speak(questionDesc);

        return position + operator + unitsToMove + " equals " + answer;
    }

    private void setupObservers() {
        viewModel.lineStart.observe(getViewLifecycleOwner(), start -> {
            int end = viewModel.lineEnd.getValue() != null ? viewModel.lineEnd.getValue() : start + 10;
            int position = viewModel.currentPosition.getValue() != null ? viewModel.currentPosition.getValue() : start;
            binding.numberLineView.updateNumberLine(start, end, position);
        });

        viewModel.lineEnd.observe(getViewLifecycleOwner(), end -> {
            int start = viewModel.lineStart.getValue() != null ? viewModel.lineStart.getValue() : end - 10;
            int position = viewModel.currentPosition.getValue() != null ? viewModel.currentPosition.getValue() : start;
            binding.numberLineView.updateNumberLine(start, end, position);
        });

        viewModel.currentPosition.observe(getViewLifecycleOwner(), position -> {
            binding.currentPositionTv.setText(CURRENT_POSITION + position);
//            tts.speak(Integer.toString(position));
            if(position == answer) {
                tts.speak("Correct Answer! " + correctAnswerDesc + ".");
                appreciateUser();
            }
        });
    }

    private void appreciateUser() {
        String message = "Good going";
        int gifResource = R.drawable.right;

        LayoutInflater inflater = getLayoutInflater();
        DialogResultBinding dialogBinding = DialogResultBinding.inflate(inflater);
        View dialogView = dialogBinding.getRoot();

        // Load the GIF using Glide
        Glide.with(this)
                .asGif()
                .load(gifResource)
                .into(dialogBinding.gifImageView);

        dialogBinding.messageTextView.setText(message);


        new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setPositiveButton("Continue", (dialog, which) -> {
                    dialog.dismiss();
                    correctAnswerDesc = askNewQuestion(answer);
                })
                .create()
                .show();
//        tts.speak("Click on continue!");
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}