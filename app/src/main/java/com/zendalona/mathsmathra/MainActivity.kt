package com.zendalona.mathsmathra

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.zendalona.mathsmathra.ui.LandingPageFragment

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            val landingPageFragment = LandingPageFragment()
            val fragmentManager = supportFragmentManager
            val fragmentTransaction = fragmentManager.beginTransaction()
            fragmentTransaction.replace(R.id.fragment_container, landingPageFragment)
            fragmentTransaction.commit()
        }
    }
}