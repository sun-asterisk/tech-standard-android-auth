package com.sun.auth.social.google

import com.sun.auth.social.model.SocialConfig
import com.sun.auth.social.utils.ConfigFunction

class GoogleConfig : SocialConfig() {
    /**
     * The web client id, see [this link](https://firebase.google.com/docs/auth/android/google-signin?authuser=0#authenticate_with_firebase) for how to get.
     */
    var webClientId = ""

    /**
     * Setting [One Tap Sign In](https://firebase.google.com/docs/auth/android/google-signin?authuser=0#authenticate_with_firebase), default is true
     */
    var enableOneTapSignIn = true

    companion object {
        internal fun apply(
            clientId: String,
            setup: ConfigFunction<GoogleConfig>? = null
        ): GoogleConfig {
            val config = GoogleConfig().apply { webClientId = clientId }
            setup?.invoke(config)
            return config
        }
    }
}
