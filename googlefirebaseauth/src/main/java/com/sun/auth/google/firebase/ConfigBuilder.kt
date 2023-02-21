package com.sun.auth.google.firebase

import android.app.Application
import com.sun.auth.core.invoke

/**
 * Setup Google Authentication config.
 * @param webClientId The web client id, see [this link](https://firebase.google.com/docs/auth/android/google-signin?authuser=0#authenticate_with_firebase) for how to get.
 * @param setup Other Google optional configurations.
 */
fun Application.initGoogleAuth(webClientId: String, setup: GoogleConfig.() -> Unit = {}) {
    val config = GoogleConfig.apply(webClientId, invoke(setup))
    GoogleFirebaseAuth.initialize(this, config)
}
