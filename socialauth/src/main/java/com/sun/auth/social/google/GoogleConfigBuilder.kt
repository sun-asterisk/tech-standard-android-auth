package com.sun.auth.social.google

import android.content.Context
import com.sun.auth.social.SocialAuth
import com.sun.auth.social.model.SocialType
import com.sun.auth.social.utils.invoke

/**
 * Setup Google Authentication separately.
 * @param webClientId The web client id, see [this link](https://firebase.google.com/docs/auth/android/google-signin?authuser=0#authenticate_with_firebase) for how to get.
 * @param setup Other Google optional configurations.
 */
fun Context.initGoogleAuth(webClientId: String, setup: GoogleConfig.() -> Unit = {}) {
    val item = SocialType.GOOGLE to GoogleConfig.apply(webClientId, invoke(setup))
    SocialAuth.initialize(this, mapOf(item))
}
