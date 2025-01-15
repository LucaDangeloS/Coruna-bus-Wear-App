package com.ldangelo.corunabuswear.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.ldangelo.corunabuswear.R

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Launch SettingsActivity and finish MainActivity
        startActivity(Intent(this, SettingsActivity::class.java))
        finish()
    }
}