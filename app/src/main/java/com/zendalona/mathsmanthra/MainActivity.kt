package com.zendalona.mathsmanthra

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.appbar.MaterialToolbar
import com.zendalona.mathsmanthra.ui.FragmentNavigation
import com.zendalona.mathsmanthra.ui.LandingPageFragment
import com.zendalona.mathsmanthra.utility.settings.LocaleHelper
import com.zendalona.mathsmanthra.R

class MainActivity : AppCompatActivity(), FragmentNavigation {

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
}
