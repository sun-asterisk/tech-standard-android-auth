package com.sun.auth.social.model

import java.io.Serializable

/**
 * Type of social authentication. Ex: Google, Facebook, Apple...
 * @param configName The name of corresponding configuration class.
 */
enum class SocialType(val configName: String) : Serializable {
    GOOGLE(configName = "GoogleConfig"),
    FACEBOOK(configName = "FacebookConfig"),
}
