package com.sun.auth.social

import android.content.Context
import com.sun.auth.social.facebook.FacebookConfig
import com.sun.auth.social.google.GoogleConfig
import com.sun.auth.social.model.SocialConfig
import com.sun.auth.social.model.SocialType
import com.sun.auth.social.utils.invoke

/**
 * Setup Social authentication settings for each [SocialType].
 * @param setup Setting configurations. Ex
 * ```kt
 * initSocialAuth {
 *      google(getString(R.string.google_web_client_id)) {
 *          useOneTapSignIn = true
 *      }
 * }
 * ```
 */
fun Context.initSocialAuth(setup: ConfigBuilder.() -> Unit) {
    ConfigBuilder(this).apply(setup).build()
}

class ConfigBuilder internal constructor(private val context: Context) {
    private val typeMap: MutableMap<SocialType, SocialConfig> = mutableMapOf()

    /**
     * Config for google signIn.
     * @param webClientId The web client id, see [this link](https://firebase.google.com/docs/auth/android/google-signin?authuser=0#authenticate_with_firebase) for how to get.
     * @param setup other optional [GoogleConfig] settings.
     */
    fun google(webClientId: String, setup: GoogleConfig.() -> Unit = {}) {
        typeMap[SocialType.GOOGLE] = GoogleConfig.apply(webClientId, invoke(setup))
    }

    /**
     * Config for facebook signIn.
     * @param appId The Facebook application Id, [follow this guide](https://firebase.google.com/docs/auth/android/facebook-login#before_you_begin)
     * @param clientToken The Facebook client token, [follow this guide](https://firebase.google.com/docs/auth/android/facebook-login#before_you_begin)
     * @param setup Other Facebook optional configurations.
     */
    fun facebook(appId: String, clientToken: String, setup: FacebookConfig.() -> Unit = {}) {
        typeMap[SocialType.FACEBOOK] =
            FacebookConfig.apply(context, appId, clientToken, invoke(setup))
    }

    fun build() {
        SocialAuth.initialize(context, typeMap)
    }
}
