package com.sun.auth.sample

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.sun.auth.sample.credentials.CredentialAuthActivity
import com.sun.auth.sample.databinding.ActivityMainBinding
import com.sun.auth.sample.google.GoogleAuthActivity

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupViews()
    }

    private fun setupViews() {
        binding.credential.setOnClickListener {
            navigateTo(CredentialAuthActivity::class.java)
        }
        binding.google.setOnClickListener {
            navigateTo(GoogleAuthActivity::class.java)
        }
    }

    private fun navigateTo(clazz: Class<*>) {
        startActivity(Intent(this, clazz))
    }
}