// MainActivity.kt
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
import com.zendalona.zmantra.core.utility.PermissionManager
import com.zendalona.zmantra.core.utility.accessibility.AccessibilityHelper
import com.zendalona.zmantra.core.utility.accessibility.AccessibilityUtils
import com.zendalona.zmantra.core.utility.accessibility.AccessibilityUtils.Companion.isMyAccessibilityServiceEnabled
import com.zendalona.zmantra.core.utility.accessibility.MathsManthraAccessibilityService
import com.zendalona.zmantra.domain.model.HintIconVisibilityController
import com.zendalona.zmantra.domain.model.Hintable
import com.zendalona.zmantra.presentation.features.hint.HintFragment
import com.zendalona.zmantra.presentation.features.landing.FragmentNavigation
import com.zendalona.zmantra.presentation.features.landing.LandingPageFragment
import com.zendalona.zmantra.presentation.features.setting.util.LocaleHelper
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), FragmentNavigation {

    private lateinit var permissionManager: PermissionManager

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

        supportFragmentManager.addOnBackStackChangedListener {
            val canGoBack = supportFragmentManager.backStackEntryCount > 0
            supportActionBar?.setDisplayHomeAsUpEnabled(canGoBack)
            supportActionBar?.setDisplayShowHomeEnabled(canGoBack)
            supportActionBar?.setHomeActionContentDescription(R.string.back_button_label)
        }

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
         val TAG = "Accessbiliyt check"

        // ✅ Always use AccessibilityUtils for checks
        // Short delay because service state may not be immediately updated
        Handler(Looper.getMainLooper()).postDelayed({
            val serviceEnabled = isMyAccessibilityServiceEnabled(
                this,
                MathsManthraAccessibilityService::class.java
            )
            val isTalkBackEnabled = AccessibilityUtils().isSystemExploreByTouchEnabled(this)

            Log.d(TAG, "Re-check: ServiceEnabled=$serviceEnabled, TalkBack=$isTalkBackEnabled")

            if (!serviceEnabled || !isTalkBackEnabled) {
                Log.d(TAG, "Triggering enforceAccessibilityRequirement()")
                AccessibilityHelper.enforceAccessibilityRequirement(this)
            } else {
                Log.d(TAG, "All accessibility requirements satisfied ✅")
                AccessibilityHelper.dismissAccessibilityDialog()
            }
        }, 1000) // give system 1s to update service state


    }


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

        invalidateOptionsMenu()
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val fragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        val showHint = if (fragment is HintIconVisibilityController) {
            fragment.shouldShowHintIcon()
        } else true
        menu.findItem(R.id.action_hint)?.isVisible = showHint
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_hint -> {
                val fragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
                if (fragment is Hintable) {
                    fragment.showHint()
                } else {
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
        const val TRANSIT_FRAGMENT_OPEN =
            androidx.fragment.app.FragmentTransaction.TRANSIT_FRAGMENT_OPEN
    }
}
