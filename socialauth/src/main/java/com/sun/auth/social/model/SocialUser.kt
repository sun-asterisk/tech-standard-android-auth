package com.sun.auth.social.model

import com.google.firebase.auth.AdditionalUserInfo
import com.google.firebase.auth.FirebaseUser

/**
 * Wrapper of social sign in response.
 * @param type Type of the social authentication.
 * @param firebaseUser The [FirebaseUser] object or `null`
 * @param additionalUserInfo The IDP-specific information for the user if the provider is one of
 *      Facebook, Github, Google, or Twitter.
 */
data class SocialUser(
    val type: SocialType,
    val firebaseUser: FirebaseUser?,
    val additionalUserInfo: AdditionalUserInfo? = null
)
