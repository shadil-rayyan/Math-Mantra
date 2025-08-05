package com.zendalona.zmantra.view

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.zendalona.zmantra.databinding.FragmentGamePageBinding
import com.zendalona.zmantra.domain.model.HintIconVisibilityController
import com.zendalona.zmantra.presentation.features.game.angle.AngleFragment
import com.zendalona.zmantra.presentation.features.game.compass.CompassFragment
import com.zendalona.zmantra.presentation.features.game.day.DayFragment
import com.zendalona.zmantra.presentation.features.game.drawing.DrawingFragment
import com.zendalona.zmantra.presentation.features.game.mentalcalculation.MentalCalculationFragment
import com.zendalona.zmantra.presentation.features.game.numberline.NumberLineFragment
import com.zendalona.zmantra.presentation.features.game.shake.ShakeFragment
import com.zendalona.zmantra.presentation.features.game.sterio.SterioFragment
import com.zendalona.zmantra.presentation.features.game.tap.TapFragment
import com.zendalona.zmantra.presentation.features.game.touchscreen.TouchScreenFragment


class GameFragment : Fragment() ,HintIconVisibilityController {

    override fun shouldShowHintIcon() = false
    var binding: FragmentGamePageBinding? = null
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
        binding!!.dayButton.setOnClickListener(View.OnClickListener { v: View? ->
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
