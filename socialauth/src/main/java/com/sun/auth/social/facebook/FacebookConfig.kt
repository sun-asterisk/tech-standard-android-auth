package com.sun.auth.social.facebook

import android.content.Context
import com.facebook.FacebookSdk
import com.sun.auth.social.model.SocialConfig
import com.sun.auth.social.utils.ConfigFunction

class FacebookConfig : SocialConfig() {
    /**
     * The list of read permissions, [see detail here](https://developers.facebook.com/docs/graph-api/overview/access-levels/)
     */
    var readPermissions = listOf<String>()

    /**
     * Auto re-signIn account if can.
     */
    var autoSignIn = true

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

    companion object {
        internal fun apply(
            context: Context,
            appId: String,
            clientToken: String,
            setup: ConfigFunction<FacebookConfig>? = null
        ): FacebookConfig {
            val config = FacebookConfig().apply {
                this.appId = appId
                this.clientToken = clientToken
            }
            check(appId.isNotBlank()) {
                "Setup your facebook application id first!"
            }
            check(clientToken.isNotBlank()) {
                "Setup your facebook client token first!"
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
