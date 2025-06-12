package com.zendalona.mathsmantra.ui.game

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.zendalona.mathsmantra.Enum.Topic
import com.zendalona.mathsmantra.R
import com.zendalona.mathsmantra.databinding.DialogResultBinding
import com.zendalona.mathsmantra.databinding.FragmentGameNumberLineBinding
import com.zendalona.mathsmantra.utility.RandomValueGenerator
import com.zendalona.mathsmantra.utility.common.TTSUtility
import com.zendalona.mathsmantra.viewModel.NumberLineViewModel

class NumberLineFragment : Fragment() {
    private var binding: FragmentGameNumberLineBinding? = null
    private lateinit var viewModel: NumberLineViewModel
    private var tts: TTSUtility? = null
    private var random: RandomValueGenerator? = null
    private var CURRENT_POSITION: String? = null
    private var answer = 0
    private var questionDesc = ""
    private var correctAnswerDesc = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this).get(NumberLineViewModel::class.java)
        CURRENT_POSITION = getString(R.string.current_position_label)

        tts = TTSUtility(requireContext())
        tts!!.setSpeechRate(0.8f)
    }

    override fun onResume() {
        super.onResume()
        // Lock orientation to landscape when this fragment is visible
//        requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        viewModel!!.reset()
    }

    override fun onPause() {
        super.onPause()
        //        requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentGameNumberLineBinding.inflate(inflater, container, false)

        //        tts.speak("You're standing on the start of number line, at position 0.");
        random = RandomValueGenerator()
        setupObservers()

        correctAnswerDesc = askNewQuestion(0)

        binding!!.numberLineQuestion.setOnClickListener(View.OnClickListener { v: View? ->
            tts!!.speak(
                questionDesc
            )
        })
        binding!!.btnLeft.setOnClickListener(View.OnClickListener { v: View? ->
            viewModel!!.moveLeft()
            binding!!.numberLineView.moveLeft()
        })
        binding!!.btnRight.setOnClickListener(View.OnClickListener { v: View? ->
            viewModel!!.moveRight()
            binding!!.numberLineView.moveRight()
        })


        //        setupTouchListener();
        return binding!!.getRoot()
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
    private fun askNewQuestion(position: Int): String {
        val topic = if (random!!.generateNumberLineQuestion()) Topic.ADDITION else Topic.SUBTRACTION
        val unitsToMove = random!!.generateNumberForCountGame()
        val operator: String
        val direction: String?

        when (topic) {
            Topic.ADDITION -> {
                operator = getString(R.string.plus) // "+" or "plus"
                direction = getString(R.string.right) // "right"
                answer = position + unitsToMove
            }

            Topic.SUBTRACTION -> {
                operator = getString(R.string.minus) // "-" or "minus"
                direction = getString(R.string.left) // "left"
                answer = position - unitsToMove
            }

            else -> {
                operator = "?"
                direction = "?"
            }
        }

        // ✅ use %1$s in strings.xml and pass strings
        val questionBrief =
            getString(R.string.what_is, position.toString(), operator, unitsToMove.toString())
        questionDesc = getString(R.string.standing_on, position.toString()) +
                getString(R.string.what_is, position.toString(), operator, unitsToMove.toString()) +
                getString(R.string.units_to_direction, unitsToMove.toString(), direction)

        binding!!.numberLineQuestion.setText(questionBrief)
        tts!!.speak(questionDesc)

        return position.toString() + operator + unitsToMove + " equals " + answer
    }

    private fun setupObservers() {
        viewModel.lineStart.observe(viewLifecycleOwner) { startNullable ->
            val start = startNullable ?: 0
            val end = viewModel.lineEnd.value ?: (start + 10)
            val position = viewModel.currentPosition.value ?: start
            binding?.numberLineView?.updateNumberLine(start, end, position)
        }

        viewModel.lineEnd.observe(viewLifecycleOwner) { endNullable ->
            val end = endNullable ?: 10
            val start = viewModel.lineStart.value ?: (end - 10)
            val position = viewModel.currentPosition.value ?: start
            binding?.numberLineView?.updateNumberLine(start, end, position)
        }

        viewModel.currentPosition.observe(viewLifecycleOwner) { positionNullable ->
            val position = positionNullable ?: 0
            binding?.currentPositionTv?.text = "$CURRENT_POSITION $position"
            if (position == answer) {
                tts?.speak("Correct Answer! $correctAnswerDesc.")
                appreciateUser()
            }
        }
    }

    private fun appreciateUser() {
        val message = "Good going"
        val gifResource = R.drawable.right

        val inflater = getLayoutInflater()
        val dialogBinding = DialogResultBinding.inflate(inflater)
        val dialogView: View = dialogBinding.getRoot()

        // Load the GIF using Glide
        Glide.with(this)
            .asGif()
            .load(gifResource)
            .into(dialogBinding.gifImageView)

        dialogBinding.messageTextView.setText(getString(R.string.appreciation_message))


        AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setPositiveButton(
                getString(R.string.continue_button),
                DialogInterface.OnClickListener { dialog: DialogInterface?, which: Int ->
                    dialog!!.dismiss()
                    correctAnswerDesc = askNewQuestion(answer)
                })
            .create()
            .show()
        //        tts.speak("Click on continue!");
    }


    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}