package com.sun.auth.sample.biometrics

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupActionBarWithNavController
import com.sun.auth.sample.R

class BiometricActivity : AppCompatActivity() {
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_biometric)
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        setupActionBarWithNavController(navController)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    /*private fun checkAndStart() {
        if (CredentialsAuth.isSignedIn<Token>()) {
            navigateToHome()
        } else {
            navigateToLogin()
        }
    }

    fun navigateToHome() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, HomeFragment.newInstance())
            .commitNow()
    }

    fun navigateToLogin() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, LoginFragment.newInstance())
            .commitNow()
    }*/
}
