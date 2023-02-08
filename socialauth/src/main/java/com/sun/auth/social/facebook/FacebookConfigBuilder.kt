package com.sun.auth.social.facebook

import android.content.Context
import com.sun.auth.social.SocialAuth
import com.sun.auth.social.model.SocialType
import com.sun.auth.social.utils.invoke

/**
 * Setup Facebook Authentication separately.
 * @param appId The Facebook application Id, [follow this guide](https://firebase.google.com/docs/auth/android/facebook-login#before_you_begin)
 * @param clientToken The Facebook client token, [follow this guide](https://firebase.google.com/docs/auth/android/facebook-login#before_you_begin)
 * @param setup Other Facebook optional configurations.
 */
fun Context.initFacebookAuth(
    appId: String,
    clientToken: String,
    setup: FacebookConfig.() -> Unit = {},
) {
    val item = SocialType.FACEBOOK to FacebookConfig.apply(this, appId, clientToken, invoke(setup))
    SocialAuth.initialize(this, mapOf(item))
}
