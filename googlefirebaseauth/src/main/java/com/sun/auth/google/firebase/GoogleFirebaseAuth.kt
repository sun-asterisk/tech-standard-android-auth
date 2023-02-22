package com.sun.auth.google.firebase

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserInfo
import com.sun.auth.core.PROVIDER_FACEBOOK
import com.sun.auth.core.PROVIDER_GOOGLE
import com.sun.auth.core.weak

@Suppress("Unused")
object GoogleFirebaseAuth {
    private var context: Context? by weak(null)
    private var config: GoogleConfig? = null
    private var authClient: AuthClient? = null

    /**
     * Init the context and google config.
     * @param context The current context.
     * @param config The GoogleConfig settings.
     */
    @JvmStatic
    internal fun initialize(context: Context, config: GoogleConfig) {
        this.context = context.applicationContext
        this.config = config
    }

    /**
     * Init the google authentication with callbacks.
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
     * Init the google authentication with callbacks.
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
            "You must call initGoogleAuth first!"
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
     * Start SignIn process.
     */
    fun signIn() {
        authClient?.signIn()
    }

    /**
     * Check current SignIn status.
     * @return true if google account is signed in, otherwise false
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
     * Gets the current signed in account.
     * @return the current signed in account or null.
     */
    fun getUser(): FirebaseUser? {
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
