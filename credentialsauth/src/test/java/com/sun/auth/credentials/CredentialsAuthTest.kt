package com.sun.auth.credentials

import android.content.Context
import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.sun.auth.core.callPrivateFunc
import com.sun.auth.core.setPrivateProperty
import com.sun.auth.credentials.base.BaseUnitTest
import com.sun.auth.credentials.repositories.AuthRepository
import com.sun.auth.credentials.repositories.model.AuthToken
import com.sun.auth.credentials.results.AuthCallback
import com.sun.auth.credentials.utils.ACTION_REFRESH_TOKEN_EXPIRED
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.runBlocking
import okhttp3.Request
import org.junit.Test

class CredentialsAuthTest : BaseUnitTest() {
    @MockK
    private lateinit var repository: AuthRepository

    @MockK
    private lateinit var context: Context

    @MockK
    private lateinit var config: CredentialsAuthConfig

    @MockK
    private lateinit var refreshTokenRequest: Request

    @MockK
    private lateinit var localBroadcastManager: LocalBroadcastManager

    override fun setup() {
        super.setup()
        MockKAnnotations.init(this, relaxed = true)
        mockkObject(CredentialsAuth, recordPrivateCalls = true)
        CredentialsAuth.setPrivateProperty("context", context)
        CredentialsAuth.setPrivateProperty("repository", repository)
        CredentialsAuth.setPrivateProperty("config", config)
        CredentialsAuth.setPrivateProperty("localBroadcastManager", localBroadcastManager)
        every { config.authTokenClazz } returns AuthToken::class.java
    }

    override fun tearDown() {
        super.tearDown()
        unmockkAll()
    }

    @Test
    fun signIn() {
        data class AuthRequest(val username: String, val password: String)

        val body = slot<AuthRequest>()
        val callback = mockk<AuthCallback<AuthToken>>()
        val token = mockk<AuthToken>()

        every { config.signInUrl } returns "fake_url"

        coEvery { repository.signIn(any(), capture(body), AuthToken::class.java) } returns token
        coEvery { callback.onResult(any()) } just runs

        runBlocking {
            CredentialsAuth.signIn(AuthRequest("", ""), callback)
            coVerify { repository.signIn("fake_url", body.captured, AuthToken::class.java) }
            verify { callback.onResult(token) }
        }
    }

    @Test
    fun isSignedIn() {
        every { CredentialsAuth.getToken<AuthToken>() } returns mockk()
        val isSignedIn = CredentialsAuth.isSignedIn<AuthToken>()
        verify { CredentialsAuth.getToken<AuthToken>() }
        assert(isSignedIn)
    }

    @Test
    fun isNotSignedIn() {
        every { CredentialsAuth.getToken<AuthToken>() } returns null
        val isSignedIn = CredentialsAuth.isSignedIn<AuthToken>()
        verify { CredentialsAuth.getToken<AuthToken>() }
        assert(!isSignedIn)
    }

    @Test
    fun getToken() {
        every { repository.getToken(AuthToken::class.java) } returns mockk()

        CredentialsAuth.getToken<AuthToken>()

        verify { config.authTokenClazz }
        verify { repository.getToken(AuthToken::class.java) }
    }

    @Test
    fun signOut() {
        every { CredentialsAuth.callPrivateFunc("unregisterTokenChanged") } returns mockk()
        every { CredentialsAuth.signOut(mockk()) } just runs

        CredentialsAuth.signOut {}

        verify { CredentialsAuth.removeToken() }
        verify { CredentialsAuth["unregisterTokenChanged"]() }
        verify { repository.removeToken() }
    }

    @Test
    fun refreshTokenPublicOK() {
        val request = Request.Builder().url("https://unused.api.url/").build()

        val callback = mockk<AuthCallback<AuthToken>>()
        val token = mockk<AuthToken>()

        coEvery { repository.refreshToken(any(), AuthToken::class.java) } returns token
        coEvery { callback.onResult(any()) } just runs

        runBlocking {
            CredentialsAuth.refreshToken(request, callback)
            coVerify { CredentialsAuth["getOrCreateClient"]() }
            coVerify { repository.refreshToken(any(), AuthToken::class.java) }
            verify { callback.onResult(token) }
        }
    }

    @Test
    fun refreshTokenPublicFail() {
        val request = Request.Builder().url("https://unused.api.url/").build()

        val callback = mockk<AuthCallback<AuthToken>>()
        val exception = mockk<RuntimeException>()

        coEvery { repository.refreshToken(any(), AuthToken::class.java) } throws exception
        coEvery { callback.onResult(any(), any()) } just runs

        runBlocking {
            CredentialsAuth.refreshToken(request, callback)
            coVerify { CredentialsAuth["getOrCreateClient"]() }
            coVerify { repository.refreshToken(any(), AuthToken::class.java) }
            verify { callback.onResult(error = exception) }
        }
    }

    @Test
    fun refreshTokenInternal() {
        CredentialsAuth.setPrivateProperty("refreshTokenRequest", refreshTokenRequest)

        coEvery { repository.refreshToken(any(), AuthToken::class.java) } returns mockk()

        runBlocking {
            CredentialsAuth.refreshToken<AuthToken>()
            coVerify { CredentialsAuth["getOrCreateClient"]() }
            coVerify { repository.refreshToken(any(), AuthToken::class.java) }
        }
    }

    @Test
    fun saveToken() {
        val token = mockk<AuthToken>()
        every { repository.saveToken(any()) } returns mockk()
        CredentialsAuth.saveToken(token)
        verify { repository.saveToken(token) }
    }

    @Test
    fun removeToken() {
        every { CredentialsAuth.callPrivateFunc("unregisterTokenChanged") } returns mockk()
        every { repository.removeToken() } returns mockk()
        CredentialsAuth.removeToken()
        verify { CredentialsAuth["unregisterTokenChanged"]() }
        verify { repository.removeToken() }
    }

    @Test
    fun notifyTokenExpired() {
        val intentSlot = slot<Intent>()
        mockkConstructor(Intent::class)
        mockkConstructor(LocalBroadcastManager::class)

        every { CredentialsAuth.callPrivateFunc("getContext") } returns context
        every { anyConstructed<Intent>().action } returns ACTION_REFRESH_TOKEN_EXPIRED
        every { anyConstructed<LocalBroadcastManager>().sendBroadcast(capture(intentSlot)) } returns true

        CredentialsAuth.notifyTokenExpired()

        assert(intentSlot.captured.action == ACTION_REFRESH_TOKEN_EXPIRED)
    }
}
