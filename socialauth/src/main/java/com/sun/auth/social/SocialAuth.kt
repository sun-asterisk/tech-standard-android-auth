package com.sun.auth.social

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.sun.auth.social.callback.SocialAuthSignInCallback
import com.sun.auth.social.callback.SocialAuthSignOutCallback
import com.sun.auth.social.model.SocialConfig
import com.sun.auth.social.model.SocialType
import com.sun.auth.social.model.SocialUser
import com.sun.auth.social.utils.weak

object SocialAuth {
    private val socialAuthFactory by lazy { SocialAuthFactory() }
    private val configMap = mutableMapOf<SocialType, SocialConfig>()
    private val authMap = mutableMapOf<SocialType, BaseSocialAuth>()
    private var context: Context? by weak(null)

    /**
     * Init the social authentication with callbacks.
     * @param types The [SocialType]s.
     * @param fragment The current [Fragment].
     * @param signInCallback The sign in callback.
     * @param signOutCallback The sign out callback.
     */
    @JvmStatic
    fun initialize(
        vararg types: SocialType,
        fragment: Fragment,
        signInCallback: SocialAuthSignInCallback?,
        signOutCallback: SocialAuthSignOutCallback?,
    ) {
        check(fragment.activity == null || fragment.activity?.isFinishing == true) {
            "The FragmentActivity this fragment is currently associated with is unavailable!"
        }
        initialize(
            types = types,
            activity = fragment.requireActivity(),
            signInCallback = signInCallback,
            signOutCallback = signOutCallback,
        )
    }

    /**
     * Init the social authentication with callbacks.
     * @param types The [SocialType]s.
     * @param activity The current [FragmentActivity].
     * @param signInCallback The sign in callback.
     * @param signOutCallback The sign out callback.
     */
    @JvmStatic
    fun initialize(
        vararg types: SocialType,
        activity: FragmentActivity,
        signInCallback: SocialAuthSignInCallback?,
        signOutCallback: SocialAuthSignOutCallback?,
    ) {
        check(!activity.isFinishing) {
            "The FragmentActivity is currently unavailable!"
        }
        check(configMap.isNotEmpty()) {
            "You must call initSocialAuth first!"
        }
        for (type in types) {
            authMap[type] = socialAuthFactory.buildSocialAuth(
                type = type,
                activity = activity,
                signInCallback = signInCallback,
                signOutCallback = signOutCallback,
            )
        }
    }

    fun <T : BaseSocialAuth> getAuth(type: SocialType): T? {
        check(authMap.isNotEmpty()) {
            "You must call initialize first!"
        }
        return authMap[type] as? T
    }

    /**
     * Sign In with given [SocialType].
     * @param type The [SocialType].
     */
    @JvmStatic
    fun signIn(type: SocialType) {
        auth(type).signIn()
    }

    /**
     * Sign out with the given [SocialType].
     * @param type The [SocialType].
     * @param clearToken `True` if want to remove the generated token, otherwise `false`. Default is `false`
     */
    @JvmStatic
    fun signOut(type: SocialType, clearToken: Boolean = false) {
        auth(type).signOut(clearToken)
    }

    /**
     * Check the current user is signed in or not.
     * @param type The [SocialType].
     * @return True if user is signed in, otherwise `false`.
     */
    @JvmStatic
    fun isSignedIn(type: SocialType): Boolean {
        return auth(type).isSignedIn()
    }

    /**
     * Gets the current signed in user.
     * @param type The [SocialType].
     * @return the current signed in [SocialUser] or `null`.
     */
    @JvmStatic
    fun getUser(type: SocialType): SocialUser? {
        return auth(type).getUser()
    }

    internal fun initialize(context: Context, typeMap: Map<SocialType, SocialConfig>) {
        this.context = context
        configMap.clear()
        configMap.putAll(typeMap)
    }

    internal fun getSocialConfig(type: SocialType): SocialConfig {
        check(configMap.containsKey(type)) {
            "You must setup ${type.configName} first!"
        }
        return configMap[type]!!
    }

    private fun auth(type: SocialType): BaseSocialAuth {
        checkNotNull(authMap[type]) {
            "You must call initialize(activity) from your activity/fragment first!"
        }
        return authMap[type]!!
    }
}
