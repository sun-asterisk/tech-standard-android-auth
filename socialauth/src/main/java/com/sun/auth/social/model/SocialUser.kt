package com.sun.auth.social.model

import com.google.firebase.auth.FirebaseUser

/**
 * Wrapper of social sign in response.
 * @param type Type of the social authentication.
 * @param user The [FirebaseUser] object or `null`
 */
data class SocialUser(val type: SocialType, val user: FirebaseUser?)
