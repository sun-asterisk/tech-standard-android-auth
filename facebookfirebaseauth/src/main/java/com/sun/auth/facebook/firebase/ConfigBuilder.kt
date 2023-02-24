package com.sun.auth.facebook.firebase

import android.app.Application
import com.sun.auth.core.invoke

/**
 * Setup Facebook Authentication config.
 * @param appId The Facebook application Id, [follow this guide](https://firebase.google.com/docs/auth/android/facebook-login#before_you_begin).
 * @param clientToken The Facebook client token, [follow this guide](https://firebase.google.com/docs/auth/android/facebook-login#before_you_begin)
 * @param setup Other Facebook optional configurations.
 */
fun Application.initFacebookFirebaseAuth(
    appId: String,
    clientToken: String,
    setup: FacebookConfig.() -> Unit = {},
) {
    val config = FacebookConfig.apply(this, appId, clientToken, invoke(setup))
    FacebookFirebaseAuth.initialize(this, config)
}
