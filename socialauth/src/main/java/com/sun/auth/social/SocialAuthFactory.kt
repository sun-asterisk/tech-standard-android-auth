package com.sun.auth.social

import androidx.fragment.app.FragmentActivity
import com.sun.auth.social.callback.SocialAuthSignInCallback
import com.sun.auth.social.callback.SocialAuthSignOutCallback
import com.sun.auth.social.facebook.FacebookAuth
import com.sun.auth.social.google.GoogleAuth
import com.sun.auth.social.model.SocialType

internal class SocialAuthFactory {
    fun buildSocialAuth(
        type: SocialType,
        activity: FragmentActivity,
        signInCallback: SocialAuthSignInCallback? = null,
        signOutCallback: SocialAuthSignOutCallback? = null
    ): BaseSocialAuth {
        return when (type) {
            SocialType.GOOGLE -> GoogleAuth(
                activity = activity,
                signInCallback = signInCallback,
                signOutCallback = signOutCallback
            ).apply {
                activity.lifecycle.addObserver(this)
            }
            SocialType.FACEBOOK -> FacebookAuth(
                activity = activity,
                signInCallback = signInCallback,
                signOutCallback = signOutCallback
            ).apply {
                activity.lifecycle.addObserver(this)
            }
        }
    }
}
