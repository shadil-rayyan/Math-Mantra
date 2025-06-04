package com.zendalona.mathsmantra

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.appbar.MaterialToolbar
import com.zendalona.mathsmantra.ui.FragmentNavigation
import com.zendalona.mathsmantra.ui.LandingPageFragment
import com.zendalona.mathsmantra.utility.accessibility.AccessibilityHelper
import com.zendalona.mathsmantra.utility.PermissionManager
import com.zendalona.mathsmantra.utility.accessibility.MathsManthraAccessibilityService
import com.zendalona.mathsmantra.utility.settings.LocaleHelper

class MainActivity : AppCompatActivity(), FragmentNavigation {

    private lateinit var permissionManager: PermissionManager

    override fun attachBaseContext(newBase: Context) {
        val context = LocaleHelper.onAttach(newBase)  // apply locale here
        super.attachBaseContext(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState) // locale already applied

        setContentView(R.layout.activity_main)
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Show/hide back button depending on back stack
        supportFragmentManager.addOnBackStackChangedListener {
            val canGoBack = supportFragmentManager.backStackEntryCount > 0
            supportActionBar?.setDisplayHomeAsUpEnabled(canGoBack)
        }

        // Load landing page initially without backstack
        if (savedInstanceState == null) {
            val landingPageFragment = LandingPageFragment()
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, landingPageFragment)
                .commit()
        }

        // Handle permissions
        permissionManager = PermissionManager(this, object : PermissionManager.PermissionCallback {
            override fun onPermissionGranted() {
                Log.d("PermissionManager", "Granted!")
            }

            override fun onPermissionDenied() {
                Log.w("PermissionManager", "Denied!")
            }
        })
        permissionManager.requestMicrophonePermission()

        // Check and show accessibility service dialog if needed
        AccessibilityHelper.checkAndShowAccessibilityDialog(this)
    }

    // Handle toolbar back button pressed
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun loadFragment(fragment: Fragment, transit: Int) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .setTransition(transit)
            .addToBackStack(null)  // important: add to back stack to enable back button
            .commit()
    }

    // Optional: function to update toolbar title from fragments
    fun updateToolbarTitle(title: String) {
        supportActionBar?.title = title
    }



    // Handle touch events for two-finger swipe gestures (back navigation)

}
