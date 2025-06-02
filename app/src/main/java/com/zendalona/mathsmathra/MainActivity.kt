package com.zendalona.mathsmathra

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.appbar.MaterialToolbar
import com.zendalona.mathsmathra.ui.FragmentNavigation
import com.zendalona.mathsmathra.ui.LandingPageFragment
import com.zendalona.mathsmathra.utility.settings.LocaleHelper

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

        if (savedInstanceState == null) {
            val landingPageFragment = LandingPageFragment()
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, landingPageFragment)
                .commit()
        }
    }

    override fun loadFragment(fragment: Fragment, transit: Int) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .setTransition(transit)
            .addToBackStack(null)
            .commit()
    }
}
