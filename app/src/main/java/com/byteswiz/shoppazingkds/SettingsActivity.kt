package com.byteswiz.shoppazingkds

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.byteswiz.shoppazingkds.databinding.ActivitySettingsBinding
import com.byteswiz.shoppazingkds.fragments.GeneralSettings

class SettingsActivity : AppCompatActivity() {
    lateinit var binding: ActivitySettingsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        //setContentView(R.layout.activity_settings)
        setContentView(binding.root)

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_settings_general, GeneralSettings())
            .commit()


    }
}