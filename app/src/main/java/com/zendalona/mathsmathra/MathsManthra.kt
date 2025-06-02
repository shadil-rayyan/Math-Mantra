package com.zendalona.mathsmathra
import android.app.Application
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat.recreate

class MathsManthra : Application() {
    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    }
}


