package com.sun.auth.facebook.firebase

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.facebook.AccessToken
import com.facebook.Profile
import com.facebook.login.widget.LoginButton
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserInfo
import com.sun.auth.core.*
import com.sun.auth.core.callback.SignInCallback
import com.sun.auth.core.callback.SignOutCallback

@Suppress("Unused")
object FacebookFirebaseAuth {
    private var context: Context? by weak(null)
    private var config: FacebookConfig? = null
    private var authClient: AuthClient? = null

    /**
     * Init the context and Facebook config.
     * @param context The current context.
     * @param config The FacebookConfig settings.
     */
    @JvmStatic
    internal fun initialize(context: Context, config: FacebookConfig) {
        this.context = context.applicationContext
        this.config = config
    }

    /**
     * Init the Facebook authentication with callbacks. Call it before Fragment STARTED state.
     * @param fragment The current [Fragment].
     * @param signInCallback The sign in callback.
     */
    @JvmStatic
    fun initialize(fragment: Fragment, signInCallback: SignInCallback<AuthResult>? = null) {
        check(fragment.activity != null || fragment.activity?.isFinishing != true) {
            "The FragmentActivity this fragment is currently associated with is unavailable!"
        }
        check(config != null) {
            "You must call initFacebookFirebaseAuth first!"
        }
        authClient = AuthClient(
            fragment.requireActivity(),
            config!!,
            signInCallback,
        ).apply {
            fragment.lifecycle.addObserver(this)
        }
    }

    /**
     * Init the Facebook authentication with callbacks. Call it before Activity STARTED state.
     * @param activity The current [FragmentActivity].
     * @param signInCallback The sign in callback.
     */
    @JvmStatic
    fun initialize(activity: FragmentActivity, signInCallback: SignInCallback<AuthResult>? = null) {
        check(!activity.isFinishing) {
            "The FragmentActivity is currently unavailable!"
        }
        check(config != null) {
            "You must call initFacebookFirebaseAuth first!"
        }
        authClient = AuthClient(
            activity,
            config!!,
            signInCallback,
        ).apply {
            activity.lifecycle.addObserver(this)
        }
    }

    /**
     * Start Facebook SignIn process.
     *
     * If use Facebook LoginButton, no need to call this function.
     */
    fun signIn() {
        authClient?.signIn()
    }

    /**
     * Sets the Facebook login button.
     *
     * Make sure you set permission for button or FacebookConfig while init.
     */
    fun setLoginButton(button: LoginButton) {
        authClient?.setLoginButton(button)
    }

    /**
     * Check current SignIn status.
     * @return true if Facebook account is signed in, otherwise false.
     */
    fun isSignedIn(): Boolean {
        return authClient?.isSignedIn() ?: false
    }

    /**
     * Sign out the current account.
     * @param signOutCallback The sign out callback.
     */
    fun signOut(signOutCallback: SignOutCallback? = null) {
        authClient?.signOut(false, signOutCallback)
    }

    /**
     * Gets the signed in facebook profile.
     * @return Signed in Profile or null.
     */
    fun getProfile(): Profile? {
        return authClient?.getProfile()
    }

    /**
     * Gets the facebook accessToken.
     * @return Facebook accessToken or null.
     */
    fun getAccessToken(): AccessToken? {
        return authClient?.getAccessToken()
    }

    /**
     * Gets the current signed in firebase account.
     * @return the current signed in account or null.
     */
    fun getFirebaseUser(): FirebaseUser? {
        return authClient?.getUser()
    }

    /**
     * Gets the current Linked UserInfo in firebase.
     * @param provider The [PROVIDER_GOOGLE] or [PROVIDER_FACEBOOK]
     * @return Firebase UserInfo or null.
     */
    fun getLinkedAccounts(provider: String): UserInfo? {
        return authClient?.getLinkedAccounts(provider)
    }
}
