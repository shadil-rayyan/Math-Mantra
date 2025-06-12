package com.zendalona.mathsmantra.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.zendalona.mathsmantra.databinding.FragmentGamePageBinding
import com.zendalona.mathsmantra.ui.game.AngleFragment
import com.zendalona.mathsmantra.ui.game.CompassFragment
import com.zendalona.mathsmantra.ui.game.DayFragment
import com.zendalona.mathsmantra.ui.game.DrawingFragment
import com.zendalona.mathsmantra.ui.game.MentalCalculationFragment
import com.zendalona.mathsmantra.ui.game.NumberLineFragment
import com.zendalona.mathsmantra.ui.game.ShakeFragment
import com.zendalona.mathsmantra.ui.game.SterioFragment
import com.zendalona.mathsmantra.ui.game.TapFragment
import com.zendalona.mathsmantra.ui.game.TouchScreenFragment

class GameFragment : Fragment() {
    private var binding: FragmentGamePageBinding? = null
    private var navigationListener: FragmentNavigation? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is FragmentNavigation) {
            navigationListener = context as FragmentNavigation
        } else {
            throw RuntimeException(context.toString() + " must implement FragmentNavigation")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentGamePageBinding.inflate(inflater, container, false)

        // Set up OnClickListeners for buttons
        binding!!.shakeButton.setOnClickListener(View.OnClickListener { v: View? ->
            if (navigationListener != null) {
                navigationListener!!.loadFragment(
                    ShakeFragment(),
                    FragmentTransaction.TRANSIT_FRAGMENT_OPEN
                )
            }
        })
        binding!!.tapButton.setOnClickListener(View.OnClickListener { v: View? ->
            if (navigationListener != null) {
                navigationListener!!.loadFragment(
                    TapFragment(),
                    FragmentTransaction.TRANSIT_FRAGMENT_OPEN
                )
            }
        })
        binding!!.angleButton.setOnClickListener(View.OnClickListener { v: View? ->
            if (navigationListener != null) {
                navigationListener!!.loadFragment(
                    AngleFragment(),
                    FragmentTransaction.TRANSIT_FRAGMENT_OPEN
                )
            }
        })
        binding!!.drawingButton.setOnClickListener(View.OnClickListener { v: View? ->
            if (navigationListener != null) {
                navigationListener!!.loadFragment(
                    DrawingFragment(),
                    FragmentTransaction.TRANSIT_FRAGMENT_OPEN
                )
            }
        })
        binding!!.directionButton.setOnClickListener(View.OnClickListener { v: View? ->
            if (navigationListener != null) {
                navigationListener!!.loadFragment(
                    CompassFragment(),
                    FragmentTransaction.TRANSIT_FRAGMENT_OPEN
                )
            }
        })
        binding!!.numberlineButton.setOnClickListener(View.OnClickListener { v: View? ->
            if (navigationListener != null) {
                navigationListener!!.loadFragment(
                    NumberLineFragment(),
                    FragmentTransaction.TRANSIT_FRAGMENT_OPEN
                )
            }
        })
        binding!!.stereoSoundButton.setOnClickListener(View.OnClickListener { v: View? ->
            if (navigationListener != null) {
                navigationListener!!.loadFragment(
                    SterioFragment(),
                    FragmentTransaction.TRANSIT_FRAGMENT_OPEN
                )
            }
        })
        binding!!.mentalCalculationButton.setOnClickListener(View.OnClickListener { v: View? ->
            if (navigationListener != null) {
                navigationListener!!.loadFragment(
                    MentalCalculationFragment(),
                    FragmentTransaction.TRANSIT_FRAGMENT_OPEN
                )
            }
        })
        binding!!.touchTheScreenColorPrimary.setOnClickListener(View.OnClickListener { v: View? ->
            if (navigationListener != null) {
                navigationListener!!.loadFragment(
                    TouchScreenFragment(),
                    FragmentTransaction.TRANSIT_FRAGMENT_OPEN
                )
            }
        })
        binding!!.mcqButton.setOnClickListener(View.OnClickListener { v: View? ->
            if (navigationListener != null) {
                navigationListener!!.loadFragment(
                    DayFragment(),
                    FragmentTransaction.TRANSIT_FRAGMENT_OPEN
                )
            }
        })

        return binding!!.getRoot()
    }
}
