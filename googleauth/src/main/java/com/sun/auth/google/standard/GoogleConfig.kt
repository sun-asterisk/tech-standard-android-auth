package com.sun.auth.google.standard

import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.sun.auth.core.ConfigFunction

class GoogleConfig internal constructor() {
    /**
     * The web client id, see [this link](https://firebase.google.com/docs/auth/android/google-signin?authuser=0#authenticate_with_firebase) for how to get.
     */
    var webClientId = ""

    /**
     * Setting [One Tap Sign In](https://firebase.google.com/docs/auth/android/google-signin?authuser=0#authenticate_with_firebase), default is true.
     */
    var enableOneTapSignIn = true

    /**
     * Define your login scope via Google SignIn Options.
     */
    lateinit var signInOptions: GoogleSignInOptions

    /**
     * To prevent a new account being created when the user has an existing account registered with the application, default is true.
     */
    var enableFilterByAuthorizedAccounts = true

    companion object {
        internal fun apply(
            clientId: String,
            options: GoogleSignInOptions,
            setup: ConfigFunction<GoogleConfig>? = null,
        ): GoogleConfig {
            val config = GoogleConfig().apply {
                webClientId = clientId
                signInOptions = options
            }
            setup?.invoke(config)
            return config
        }
    }
}
