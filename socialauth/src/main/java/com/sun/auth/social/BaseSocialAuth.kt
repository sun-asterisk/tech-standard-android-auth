package com.sun.auth.social

import androidx.activity.result.ActivityResult
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.sun.auth.social.callback.SocialAuthSignInCallback
import com.sun.auth.social.callback.SocialAuthSignOutCallback
import com.sun.auth.social.model.SocialUser
import com.sun.auth.social.utils.weak

internal abstract class BaseSocialAuth(
    childActivity: FragmentActivity,
    protected val signInCallback: SocialAuthSignInCallback? = null,
    protected val signOutCallback: SocialAuthSignOutCallback? = null
) : DefaultLifecycleObserver {

    protected var activity: FragmentActivity? by weak(null)
    protected val firebaseAuth by lazy { Firebase.auth }

    init {
        activity = childActivity
    }

    abstract fun signIn()
    abstract fun handleSignInResult(result: ActivityResult)
    abstract fun isSignedIn(): Boolean
    abstract fun signOut(clearToken: Boolean)
    abstract fun getUser(): SocialUser?
    abstract fun revoke()
}
