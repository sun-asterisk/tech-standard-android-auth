package com.sun.auth.google.standard

import android.app.Application
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.sun.auth.core.invoke

/**
 * Setup Google Authentication config.
 * @param webClientId The web client id, see [this link](https://firebase.google.com/docs/auth/android/google-signin?authuser=0#authenticate_with_firebase) for how to get.
 * @param signInOptions The options used to configure the GoogleSignIn API.
 * @param setup Other Google optional configurations.
 */
fun Application.initGoogleAuth(
    webClientId: String,
    signInOptions: GoogleSignInOptions,
    setup: GoogleConfig.() -> Unit = {},
) {
    val config = GoogleConfig.apply(webClientId, signInOptions, invoke(setup))
    GoogleStandardAuth.initialize(this, config)
}
