package com.sun.auth.sample

import android.app.Application
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.gson.JsonObject
import com.sun.auth.credentials.CredentialsAuth
import com.sun.auth.credentials.results.AuthTokenChanged
import com.sun.auth.facebook.firebase.initFacebookFirebaseAuth
import com.sun.auth.facebook.standard.initFacebookAuth
import com.sun.auth.google.firebase.initGoogleAuth
import com.sun.auth.google.standard.initGoogleAuth
import com.sun.auth.sample.credentials.suntech.SunToken
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class SampleApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        setupSunTechAuth()
        setupGoogleStandardAuth()
        setupGoogleFirebaseAuth()
        setupFacebookStandardAuth()
        setupFacebookFirebaseAuth()
    }

    private fun setupSunTechAuth() {
        CredentialsAuth.Builder()
            .context(applicationContext)
            .signInUrl(url = "http://10.0.5.78:8001/api/login")
            .authTokenClazz(SunToken::class.java)
            .authTokenChanged(object : AuthTokenChanged<SunToken> {
                // do this when you want to refresh token automatically
                override fun onTokenUpdate(token: SunToken?): Request {
                    return buildSunTechRefreshTokenRequest(token)
                }
            })
            .build()
    }

    private fun buildSunTechRefreshTokenRequest(token: SunToken?): Request {
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
            readPermissions = listOf("email", "public_profile")
            enableAppEvent = false
            enableLoginStatus = true
        }
    }
}
