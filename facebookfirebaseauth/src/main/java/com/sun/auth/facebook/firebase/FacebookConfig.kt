package com.sun.auth.facebook.firebase

import android.content.Context
import com.facebook.FacebookSdk
import com.sun.auth.core.ConfigFunction

class FacebookConfig internal constructor() {
    /**
     * The list of read permissions, [see detail here](https://developers.facebook.com/docs/graph-api/overview/access-levels/)
     *
     * If app is requesting permissions beyond the default fields and email, you must submit for App Review in [App Review for Facebook Login](https://developers.facebook.com/docs/app-review)
     */
    var readPermissions = listOf<String>()

    /**
     * If user is signed in, a popup notification appears that says // "Logged in as <User Name>"
     */
    var enableLoginStatus = true

    /**
     * The Facebook application Id, [follow this guide](https://firebase.google.com/docs/auth/android/facebook-login#before_you_begin)
     */
    var appId = ""

    /**
     * The Facebook client token, [follow this guide](https://firebase.google.com/docs/auth/android/facebook-login#before_you_begin)
     */
    var clientToken = ""

    /**
     * Sets the auto logging events flag for the application.
     */
    var enableAppEvent = true

    /**
     * True if use default Facebook LoginButton to login.
     */
    var useFacebookLoginButton = false

    /**
     * To enable linking multi social accounts.
     */
    var enableLinkAccounts = false

    companion object {
        internal fun apply(
            context: Context,
            appId: String,
            clientToken: String,
            setup: ConfigFunction<FacebookConfig>? = null,
        ): FacebookConfig {
            check(appId.isNotBlank()) {
                "Setup your facebook application id first!"
            }
            check(clientToken.isNotBlank()) {
                "Setup your facebook client token first!"
            }
            val config = FacebookConfig().apply {
                this.appId = appId
                this.clientToken = clientToken
            }
            FacebookSdk.setApplicationId(appId)
            FacebookSdk.setClientToken(clientToken)

            setup?.invoke(config)

            FacebookSdk.setAutoLogAppEventsEnabled(config.enableAppEvent)
            // Even if this method is deprecated, it needs for library,
            FacebookSdk.sdkInitialize(context.applicationContext)
            return config
        }
    }
}
