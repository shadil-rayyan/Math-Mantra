package com.zendalona.mathsmantra

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
import com.zendalona.mathsmantra.model.HintIconVisibilityController
import com.zendalona.mathsmantra.model.Hintable
import com.zendalona.mathsmantra.ui.FragmentNavigation
import com.zendalona.mathsmantra.ui.HintFragment
import com.zendalona.mathsmantra.ui.LandingPageFragment
import com.zendalona.mathsmantra.utility.HintVisibilityUtil
import com.zendalona.mathsmantra.utility.accessibility.AccessibilityHelper
import com.zendalona.mathsmantra.utility.PermissionManager
import com.zendalona.mathsmantra.utility.settings.LocaleHelper

class MainActivity : AppCompatActivity(), FragmentNavigation {

    private lateinit var permissionManager: PermissionManager

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Enable back arrow depending on stack
        supportFragmentManager.addOnBackStackChangedListener {
            val canGoBack = supportFragmentManager.backStackEntryCount > 0
            supportActionBar?.setDisplayHomeAsUpEnabled(canGoBack)
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

        // Accessibility dialog
        Handler(Looper.getMainLooper()).postDelayed({
            AccessibilityHelper.enforceAccessibilityRequirement(this)
        }, 500)
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

    fun updateToolbarTitle(title: String) {
        supportActionBar?.title = title
    }

    // Inflate toolbar menu
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.top_menu, menu)
        return true
    }
    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        menu.findItem(R.id.action_hint)?.isVisible = HintVisibilityUtil.shouldShowHint()
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
