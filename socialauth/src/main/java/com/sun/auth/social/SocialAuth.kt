package com.sun.auth.social

import android.content.Context
import androidx.fragment.app.FragmentActivity
import com.sun.auth.social.callback.SocialAuthSignInCallback
import com.sun.auth.social.callback.SocialAuthSignOutCallback
import com.sun.auth.social.model.SocialType
import com.sun.auth.social.model.SocialConfig
import com.sun.auth.social.model.SocialUser
import com.sun.auth.social.utils.weak

object SocialAuth {
    private val socialAuthFactory by lazy { SocialAuthFactory() }
    private val configMap = mutableMapOf<SocialType, SocialConfig>()
    private val authMap = mutableMapOf<SocialType, BaseSocialAuth>()
    private var context: Context? by weak(null)

    /**
     * Init the social authentication with callbacks.
     * @param activity The current [FragmentActivity].
     * @param signInCallback The sign in callback.
     * @param signOutCallback The sign out callback.
     */
    @JvmStatic
    fun initialize(
        activity: FragmentActivity,
        signInCallback: SocialAuthSignInCallback?,
        signOutCallback: SocialAuthSignOutCallback?
    ) {
        check(configMap.isNotEmpty()) {
            "You must call initSocialAuth first!"
        }

        configMap.mapKeys { entry ->
            authMap.put(
                key = entry.key,
                value = socialAuthFactory.buildSocialAuth(
                    type = entry.key,
                    activity = activity,
                    signInCallback = signInCallback,
                    signOutCallback = signOutCallback
                )
            )
        }
    }

    /**
     * Sign In with given [SocialType].
     * @param type The [SocialType].
     */
    @JvmStatic
    fun signIn(type: SocialType) {
        socialAuth(type).signIn()
    }

    /**
     * Sign out with the given [SocialType].
     * @param type The [SocialType].
     * @param clearToken `True` if want to remove the generated token, otherwise `false`. Default is `false`
     */
    @JvmStatic
    fun signOut(type: SocialType, clearToken: Boolean = false) {
        socialAuth(type).signOut(clearToken)
    }

    /**
     * Check the current user is signed in or not.
     * @param type The [SocialType].
     * @return True if user is signed in, otherwise `false`.
     */
    @JvmStatic
    fun isSignedIn(type: SocialType): Boolean {
        return socialAuth(type).isSignedIn()
    }

    /**
     * Gets the current signed in user.
     * @param type The [SocialType].
     * @return the current signed in [SocialUser] or `null`.
     */
    @JvmStatic
    fun getUser(type: SocialType): SocialUser? {
        return socialAuth(type).getUser()
    }

    internal fun initialize(context: Context, typeMap: MutableMap<SocialType, SocialConfig>) {
        this.context = context
        configMap.clear()
        configMap.putAll(typeMap)
    }

    internal fun getPlatformConfig(type: SocialType): SocialConfig {
        check(configMap.containsKey(type)) {
            "You must setup ${typeConfig(type)} first!"
        }
        return configMap[type]!!
    }

    private fun typeConfig(type: SocialType): String {
        return type.configName
    }

    private fun socialAuth(type: SocialType): BaseSocialAuth {
        checkNotNull(authMap[type]) {
            "You must call initialize(activity) from your activity/fragment first!"
        }
        return authMap[type]!!
    }
}