package com.sun.auth.sample

import android.app.Application
import android.security.keystore.KeyProperties
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.gson.JsonObject
import com.sun.auth.biometricauth.initBiometricAuth
import com.sun.auth.credentials.CredentialsAuth
import com.sun.auth.credentials.initCredentialsAuth
import com.sun.auth.credentials.results.AuthTokenChanged
import com.sun.auth.facebook.firebase.initFacebookFirebaseAuth
import com.sun.auth.facebook.standard.initFacebookAuth
import com.sun.auth.google.firebase.initGoogleAuth
import com.sun.auth.google.standard.initGoogleAuth
import com.sun.auth.sample.model.Token
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class SampleApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        setupCredentialsAuth()
        setupBiometricAuth()
        setupGoogleStandardAuth()
        setupGoogleFirebaseAuth()
        setupFacebookStandardAuth()
        setupFacebookFirebaseAuth()
    }

    private fun setupCredentialsAuth() {
        initCredentialsAuth(
            signInUrl = "http://10.0.5.78:8001/api/login",
            authTokenClazz = Token::class.java,
        ) {
            authTokenChanged = object : AuthTokenChanged<Token> {
                override fun onTokenUpdate(token: Token?): Request {
                    return buildRefreshTokenRequest(token)
                }
            }
        }
    }

    private fun setupBiometricAuth() {
        initBiometricAuth(
            allowDeviceCredentials = true,
        ) {
            keystoreAlias = "sample_key_name"
            keySize = 256
            algorithm = KeyProperties.KEY_ALGORITHM_AES
            blockMode = KeyProperties.BLOCK_MODE_CBC
            padding = KeyProperties.ENCRYPTION_PADDING_PKCS7
        }
    }

    private fun buildRefreshTokenRequest(token: Token?): Request {
        val json = JsonObject().apply {
            addProperty("refresh_token", token?.crRefreshToken.orEmpty())
        }
        val body = json.toString().toRequestBody(CredentialsAuth.JSON_MEDIA_TYPE)
        return Request.Builder()
            .url("http://10.0.5.78:8001/api/refresh")
            .post(body)
            .build()
    }

    private fun setupGoogleStandardAuth() {
        initGoogleAuth(
            webClientId = getString(R.string.google_web_client_id),
            signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestIdToken(getString(R.string.google_web_client_id))
                .requestProfile()
                .build(),
        ) {
            enableOneTapSignIn = true
            enableFilterByAuthorizedAccounts = true
        }
    }

    private fun setupGoogleFirebaseAuth() {
        initGoogleAuth(
            webClientId = getString(R.string.google_web_client_id),
        ) {
            enableLinkAccounts = true
            enableOneTapSignIn = true
            enableFilterByAuthorizedAccounts = true
        }
    }

    private fun setupFacebookStandardAuth() {
        initFacebookAuth(
            appId = getString(R.string.facebook_app_id),
            clientToken = getString(R.string.facebook_client_token),
        ) {
            readPermissions = listOf("email", "public_profile")
            enableAppEvent = false
            useFacebookLoginButton = true
        }
    }

    private fun setupFacebookFirebaseAuth() {
        initFacebookFirebaseAuth(
            appId = getString(R.string.facebook_app_id),
            clientToken = getString(R.string.facebook_client_token),
        ) {
            enableLinkAccounts = true
            readPermissions = listOf("email", "public_profile")
            enableAppEvent = false
            enableLoginStatus = true
        }
    }
}
