package com.sun.auth.facebook.standard

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.facebook.AccessToken
import com.facebook.Profile
import com.facebook.login.widget.LoginButton
import com.sun.auth.core.weak

@Suppress("Unused")
object FacebookStandardAuth {
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
     * Init the Facebook authentication with callbacks.
     * @param fragment The current [Fragment].
     * @param signInCallback The sign in callback.
     * @param signOutCallback The sign out callback.
     */
    @JvmStatic
    fun initialize(
        fragment: Fragment,
        signInCallback: SignInCallback? = null,
        signOutCallback: SignOutCallback? = null,
    ) {
        check(fragment.activity == null || fragment.activity?.isFinishing == true) {
            "The FragmentActivity this fragment is currently associated with is unavailable!"
        }
        initialize(
            activity = fragment.requireActivity(),
            signInCallback = signInCallback,
            signOutCallback = signOutCallback,
        )
    }

    /**
     * Init the Facebook authentication with callbacks.
     * @param activity The current [FragmentActivity].
     * @param signInCallback The sign in callback.
     * @param signOutCallback The sign out callback.
     */
    @JvmStatic
    fun initialize(
        activity: FragmentActivity,
        signInCallback: SignInCallback? = null,
        signOutCallback: SignOutCallback? = null,
    ) {
        check(!activity.isFinishing) {
            "The FragmentActivity is currently unavailable!"
        }
        check(config != null) {
            "You must call initFacebookAuth first!"
        }
        authClient = AuthClient(
            activity,
            config!!,
            signInCallback,
            signOutCallback,
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
     */
    fun signOut() {
        authClient?.signOut(false)
    }

    /**
     * Gets the signed in account profile.
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
}
