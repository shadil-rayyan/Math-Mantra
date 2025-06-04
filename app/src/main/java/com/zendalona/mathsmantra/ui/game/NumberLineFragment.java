package com.zendalona.mathsmantra.ui.game;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.zendalona.mathmantra.R;
import com.zendalona.mathmantra.databinding.DialogResultBinding;
import com.zendalona.mathmantra.databinding.FragmentNumberLineBinding;
import com.zendalona.mathmantra.enums.Topic;
import com.zendalona.mathmantra.utils.AccessibilityUtils;
import com.zendalona.mathmantra.utils.RandomValueGenerator;
import com.zendalona.mathmantra.utils.TTSUtility;
import com.zendalona.mathmantra.viewModels.NumberLineViewModel;

public class NumberLineFragment extends Fragment {

    private FragmentNumberLineBinding binding;
    private NumberLineViewModel viewModel;
    private TTSUtility tts;
    private RandomValueGenerator random;
    private final String CURRENT_POSITION = "You're standing on number : ";
    private int answer;
    private String questionDesc = "";
    private String correctAnswerDesc = "";
    private boolean talkBackEnabled = false;
    private float initialX = 0;
    private boolean isTwoFingerSwipe = false;

    public NumberLineFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(NumberLineViewModel.class);
        tts = new TTSUtility(requireContext());
        tts.setSpeechRate(0.8f);
        talkBackEnabled = AccessibilityUtils.isMathsManthraAccessibilityServiceEnabled(requireContext());
    }

    @Override
    public void onResume() {
        super.onResume();
        viewModel.reset();
        binding.numberLineView.onResume();

        if (talkBackEnabled) {
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                tts.speak("What is " + binding.numberLineQuestion.getText() + "? Swipe left or right to change.");
            }, 4000);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        binding.numberLineView.onPause();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentNumberLineBinding.inflate(inflater, container, false);
        random = new RandomValueGenerator();
        setupObservers();
        correctAnswerDesc = askNewQuestion(0);

        binding.numberLineQuestion.setOnClickListener(v -> tts.speak(questionDesc));
        binding.btnLeft.setOnClickListener(v -> moveLeft());
        binding.btnRight.setOnClickListener(v -> moveRight());

        setupTouchListener();
        return binding.getRoot();
    }

    private void setupTouchListener() {
        binding.getRoot().setOnTouchListener((v, event) -> {
            if (talkBackEnabled) {
                handleTwoFingerSwipe(event);
                return true;
            }
            return false;
        });
    }

    private void handleTwoFingerSwipe(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_POINTER_DOWN:
                if (event.getPointerCount() == 2) {
                    initialX = (event.getX(0) + event.getX(1)) / 2;
                    isTwoFingerSwipe = true;
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (isTwoFingerSwipe && event.getPointerCount() == 2) {
                    float currentX = (event.getX(0) + event.getX(1)) / 2;
                    float deltaX = currentX - initialX;

                    if (Math.abs(deltaX) > 100) {
                        if (deltaX > 0) {
                            moveRight();
                        } else {
                            moveLeft();
                        }
                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            tts.speak("You're standing on " + viewModel.currentPosition.getValue() + ". Swipe left or right.");
                        }, 300);
                        isTwoFingerSwipe = false;
                    }
                }
                break;

            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                isTwoFingerSwipe = false;
                break;
        }
    }

    private void moveLeft() {
        viewModel.moveLeft();
        binding.numberLineView.moveLeft();
    }

    private void moveRight() {
        viewModel.moveRight();
        binding.numberLineView.moveRight();
    }

    private String askNewQuestion(int position) {
        Topic topic = random.generateNumberLineQuestion() ? Topic.ADDITION : Topic.SUBTRACTION;
        int unitsToMove = random.generateNumberForCountGame();
        String operator = (topic == Topic.ADDITION) ? " plus " : " minus ";
        String direction = (topic == Topic.ADDITION) ? " right " : " left ";
        answer = (topic == Topic.ADDITION) ? position + unitsToMove : position - unitsToMove;

        binding.numberLineQuestion.setText("What is " + position + " " + operator.trim() + " " + unitsToMove + "?");
        questionDesc = "You're standing on " + position + ". " +
                "What is " + position + operator + unitsToMove + "? Move " + unitsToMove + " units to the " + direction;

        tts.speak(questionDesc);
        return position + operator + unitsToMove + " equals " + answer;
    }

    private void setupObservers() {
        viewModel.currentPosition.observe(getViewLifecycleOwner(), position -> {
            binding.currentPositionTv.setText(CURRENT_POSITION + position);
            if (position == answer) {
                tts.speak("Correct Answer! " + correctAnswerDesc);
                appreciateUser();
            } else {
                tts.speak("You're standing on " + position + ". Swipe left to decrease, right to increase.");
            }
        });
    }

    private void appreciateUser() {
        LayoutInflater inflater = getLayoutInflater();
        DialogResultBinding dialogBinding = DialogResultBinding.inflate(inflater);
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogBinding.getRoot())
                .create();

        dialogBinding.messageTextView.setText("Good going");
        Glide.with(this).asGif().load(R.drawable.right).into(dialogBinding.gifImageView);

        dialog.show();
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            dialog.dismiss();
            tts.speak("Next question. " + correctAnswerDesc);
            correctAnswerDesc = askNewQuestion(answer);
        }, 2000);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
