package com.zendalona.zmantra

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.appbar.MaterialToolbar
import com.zendalona.zmantra.model.HintIconVisibilityController
import com.zendalona.zmantra.model.Hintable
import com.zendalona.zmantra.view.FragmentNavigation
import com.zendalona.zmantra.view.HintFragment
import com.zendalona.zmantra.view.LandingPageFragment
import com.zendalona.zmantra.utility.accessibility.AccessibilityHelper
import com.zendalona.zmantra.utility.PermissionManager
import com.zendalona.zmantra.utility.accessibility.AccessibilityUtils
import com.zendalona.zmantra.utility.settings.LocaleHelper

class MainActivity : AppCompatActivity(), FragmentNavigation {

    private lateinit var permissionManager: PermissionManager

    // SharedPreferences to store whether the accessibility dialog was shown
    private val prefs by lazy {
        getSharedPreferences("AccessibilityPrefs", Context.MODE_PRIVATE)
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // Enable back arrow depending on stack
        supportFragmentManager.addOnBackStackChangedListener {
            val canGoBack = supportFragmentManager.backStackEntryCount > 0
            supportActionBar?.setDisplayHomeAsUpEnabled(canGoBack)
            supportActionBar?.setDisplayShowHomeEnabled(canGoBack)

            // Set localized content description (for accessibility)
            supportActionBar?.setHomeActionContentDescription(R.string.back_button_label)
        }

        // Initial fragment
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, LandingPageFragment())
                .commit()
        }

        // Permissions
        permissionManager = PermissionManager(this, object : PermissionManager.PermissionCallback {
            override fun onPermissionGranted() {
                Log.d("PermissionManager", "Granted!")
            }

            override fun onPermissionDenied() {
                Log.w("PermissionManager", "Denied!")
            }
        })
        permissionManager.requestMicrophonePermission()

        // Check if the custom accessibility service is enabled
        val isServiceEnabled = AccessibilityHelper.isMathsManthraAccessibilityServiceEnabled(this)
        val isTalkBackEnabled = AccessibilityUtils().isSystemExploreByTouchEnabled(this)

        // If the service or TalkBack is not enabled, prompt the user for action
        if (!isServiceEnabled || !isTalkBackEnabled) {
            Handler(Looper.getMainLooper()).postDelayed({
                AccessibilityHelper.enforceAccessibilityRequirement(this)
            }, 500)
        }
    }

    // This will be called after the dialog is shown, marking the flag
    fun markDialogAsShown() {
        val editor = prefs.edit()
        editor.putBoolean("ACCESSIBILITY_DIALOG_SHOWN", true)
        editor.apply()
    }

    // Handle back button in toolbar
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun loadFragment(fragment: Fragment, transit: Int) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .setTransition(transit)
            .addToBackStack(null)
            .commit()

        invalidateOptionsMenu() // 🔄 Ensures menu updates
    }

    // Inflate toolbar menu
    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val fragment = supportFragmentManager.findFragmentById(R.id.fragment_container)

        val showHint = if (fragment is HintIconVisibilityController) {
            fragment.shouldShowHintIcon()
        } else {
            true // default fallback if fragment doesn't implement interface
        }

        menu.findItem(R.id.action_hint)?.isVisible = showHint
        return super.onPrepareOptionsMenu(menu)
    }

    // Handle toolbar icon clicks
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_hint -> {
                val fragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
                if (fragment is Hintable) {
                    fragment.showHint()
                } else {
                    // fallback filepath if fragment doesn't implement Hintable
                    val hintFragment = HintFragment().apply {
                        arguments = Bundle().apply {
                            putString("filepath", "en/hint/default.txt")
                        }
                    }
                    loadFragment(hintFragment, TRANSIT_FRAGMENT_OPEN)
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    companion object {
        const val TRANSIT_FRAGMENT_OPEN = androidx.fragment.app.FragmentTransaction.TRANSIT_FRAGMENT_OPEN
    }
}
