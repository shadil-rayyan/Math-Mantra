package com.zendalona.mathsmantra.ui.game

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.zendalona.mathsmantra.Enum.Topic
import com.zendalona.mathsmantra.R
import com.zendalona.mathsmantra.databinding.DialogResultBinding
import com.zendalona.mathsmantra.databinding.FragmentGameNumberLineBinding
import com.zendalona.mathsmantra.model.Hintable
import com.zendalona.mathsmantra.ui.HintFragment
import com.zendalona.mathsmantra.utility.RandomValueGenerator
import com.zendalona.mathsmantra.utility.accessibility.AccessibilityHelper
import com.zendalona.mathsmantra.utility.common.TTSUtility
import com.zendalona.mathsmantra.viewModel.NumberLineViewModel

class NumberLineFragment : Fragment(), Hintable {

    companion object {
        private const val TAG = "NumberLineFragment"
        private const val SWIPE_THRESHOLD = 100
        private const val SWIPE_VELOCITY_THRESHOLD = 100
    }

    private var binding: FragmentGameNumberLineBinding? = null
    private lateinit var viewModel: NumberLineViewModel
    private var tts: TTSUtility? = null
    private var random: RandomValueGenerator? = null
    private var CURRENT_POSITION: String? = null
    private var answer = 0
    private var questionDesc = ""
    private var correctAnswerDesc = ""

    private lateinit var gestureDetector: GestureDetector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[NumberLineViewModel::class.java]
        CURRENT_POSITION = getString(R.string.current_position_label)

        tts = TTSUtility(requireContext()).apply {
            setSpeechRate(0.8f)
        }

        random = RandomValueGenerator()

        gestureDetector = GestureDetector(requireContext(), SwipeGestureListener())
    }

    override fun onResume() {
        super.onResume()
        viewModel.reset()
        AccessibilityHelper.getAccessibilityService()?.let {
            AccessibilityHelper.disableExploreByTouch(it)
        }
    }

    override fun onPause() {
        super.onPause()
        AccessibilityHelper.getAccessibilityService()?.let {
            AccessibilityHelper.resetExploreByTouch(it)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentGameNumberLineBinding.inflate(inflater, container, false)
        setupObservers()
        setupUI()
        correctAnswerDesc = askNewQuestion(0)

        // Set touch listener on root view to detect swipe gestures anywhere in fragment
        binding?.root?.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            true
        }

        return binding!!.root
    }

    private fun setupObservers() {
        val updateNumberLineView = {
            val start = viewModel.lineStart.value ?: -5
            val end = viewModel.lineEnd.value ?: 5
            val position = viewModel.currentPosition.value ?: 0

            binding?.numberLineView?.updateNumberLine(start, end, position)
            binding?.currentPositionTv?.text = "$CURRENT_POSITION $position"

            if (position == answer) {
                tts?.speak("Correct Answer! $correctAnswerDesc.")
                appreciateUser()
            }
        }

        viewModel.lineStart.observe(viewLifecycleOwner) { updateNumberLineView() }
        viewModel.lineEnd.observe(viewLifecycleOwner) { updateNumberLineView() }
        viewModel.currentPosition.observe(viewLifecycleOwner) { updateNumberLineView() }
    }

    private fun setupUI() {
        binding?.numberLineQuestion?.setOnClickListener {
            tts?.speak(questionDesc)
        }

        binding?.btnLeft?.setOnClickListener {
            viewModel.moveLeft()
        }

        binding?.btnRight?.setOnClickListener {
            viewModel.moveRight()
        }

        // Removed NumberLineView swipe listener since swipe is now handled on fragment root
    }

    private fun askNewQuestion(position: Int): String {
        val topic = if (random!!.generateNumberLineQuestion()) Topic.ADDITION else Topic.SUBTRACTION
        val units = random!!.generateNumberForCountGame()
        val operator: String
        val direction: String

        answer = when (topic) {
            Topic.ADDITION -> {
                operator = getString(R.string.plus)
                direction = getString(R.string.right)
                position + units
            }

            Topic.SUBTRACTION -> {
                operator = getString(R.string.minus)
                direction = getString(R.string.left)
                position - units
            }

            else -> {
                operator = "?"
                direction = "?"
                position
            }
        }

        val questionBrief =
            getString(R.string.what_is, position.toString(), operator, units.toString())
        questionDesc = getString(R.string.standing_on, position.toString()) +
                getString(R.string.what_is, position.toString(), operator, units.toString()) +
                getString(R.string.units_to_direction, units.toString(), direction)

        binding?.numberLineQuestion?.text = questionBrief
        tts?.speak(questionDesc)

        return "$position $operator $units equals $answer"
    }


    private fun appreciateUser() {
        val dialogBinding = DialogResultBinding.inflate(layoutInflater)

        Glide.with(this)
            .asGif()
            .load(R.drawable.right)
            .into(dialogBinding.gifImageView)

        dialogBinding.messageTextView.text = getString(R.string.appreciation_message)

        AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .setPositiveButton(getString(R.string.continue_button)) { dialog, _ ->
                dialog.dismiss()
                correctAnswerDesc = askNewQuestion(answer)
            }
            .create()
            .show()
    }



    private inner class SwipeGestureListener : android.view.GestureDetector.SimpleOnGestureListener() {

        override fun onDown(e: MotionEvent): Boolean {
            return true
        }

        override fun onFling(
            e1: MotionEvent?,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            if (e1 == null || e2 == null) return false

            val diffX = e2.x - e1.x
            val diffY = e2.y - e1.y

            if (kotlin.math.abs(diffX) > kotlin.math.abs(diffY)) {
                if (kotlin.math.abs(diffX) > SWIPE_THRESHOLD && kotlin.math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffX > 0) {
                        Log.d(TAG, "Swipe Right detected in Fragment")
                        viewModel.moveRight()
                    } else {
                        Log.d(TAG, "Swipe Left detected in Fragment")
                        viewModel.moveLeft()
                    }
                    return true
                }
            }
            return false
        }
    }
    override fun showHint() {
        val bundle = Bundle().apply {
            putString("filepath", "hint/game/numberline.txt")
        }
        val hintFragment = HintFragment().apply { arguments = bundle }

        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, hintFragment)
            .addToBackStack(null)
            .commit()
    }
    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

}
