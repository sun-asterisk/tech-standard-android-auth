package com.sun.auth.sample

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.sun.auth.sample.credentials.other.CredentialAuthActivity
import com.sun.auth.sample.credentials.suntech.SunTechActivity
import com.sun.auth.sample.databinding.ActivityMainBinding
import com.sun.auth.sample.facebook.firebase.FacebookFirebaseAuthActivity
import com.sun.auth.sample.facebook.standard.FacebookAuthActivity
import com.sun.auth.sample.google.firebase.GoogleFirebaseAuthActivity
import com.sun.auth.sample.google.standard.GoogleAuthActivity

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
        binding.suntech.setOnClickListener {
            navigateTo(SunTechActivity::class.java)
        }
        binding.google.setOnClickListener {
            navigateTo(GoogleAuthActivity::class.java)
        }
        binding.googleFirebase.setOnClickListener {
            navigateTo(GoogleFirebaseAuthActivity::class.java)
        }
        binding.facebook.setOnClickListener {
            navigateTo(FacebookAuthActivity::class.java)
        }
        binding.facebookFirebase.setOnClickListener {
            navigateTo(FacebookFirebaseAuthActivity::class.java)
        }
    }

    private fun navigateTo(clazz: Class<*>) {
        startActivity(Intent(this, clazz))
    }
}
