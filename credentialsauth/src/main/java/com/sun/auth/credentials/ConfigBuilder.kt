package com.sun.auth.credentials

import android.app.Application
import com.sun.auth.core.invoke
import com.sun.auth.credentials.repositories.model.AuthToken

/**
 * Setup Credentials Authentication config.
 * @param signInUrl The full signIn URL with scheme & path.
 * @param authTokenClazz The [AuthToken] implementation java class. This helps for casting via generic class
 * @param setup Other CredentialsConfig optional configurations.
 */
fun Application.initCredentialsAuth(
    signInUrl: String,
    authTokenClazz: Class<*>,
    setup: CredentialsAuthConfig.() -> Unit = {},
) {
    val config = CredentialsAuthConfig.apply(signInUrl, authTokenClazz, invoke(setup))
    CredentialsAuth.initialize(this, config)
}
