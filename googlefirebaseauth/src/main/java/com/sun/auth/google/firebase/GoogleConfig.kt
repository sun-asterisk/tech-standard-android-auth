package com.sun.auth.google.firebase

import com.sun.auth.core.ConfigFunction

class GoogleConfig internal constructor() {
    /**
     * The web client id, see [this link](https://firebase.google.com/docs/auth/android/google-signin?authuser=0#authenticate_with_firebase) for how to get.
     */
    var webClientId = ""

    /**
     * Setting [One Tap Sign In](https://firebase.google.com/docs/auth/android/google-signin?authuser=0#authenticate_with_firebase), default is true.
     */
    var enableOneTapSignIn = false

    /**
     * To prevent a new account being created when the user has an existing account registered with the application, default is true.
     */
    var enableFilterByAuthorizedAccounts = false

    /**
     * To enable linking multi social accounts.
     */
    var enableLinkAccounts = false

    companion object {
        fun apply(
            clientId: String,
            setup: ConfigFunction<GoogleConfig>? = null,
        ): GoogleConfig {
            val config = GoogleConfig().apply { webClientId = clientId }
            setup?.invoke(config)
            return config
        }
    }
}
