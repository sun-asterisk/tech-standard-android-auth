package com.sun.auth.facebook.standard

import com.facebook.login.widget.LoginButton
import com.sun.auth.core.setPrivateProperty
import io.mockk.*
import io.mockk.impl.annotations.MockK
import org.junit.Before
import org.junit.Test

class FacebookStandardAuthTest {
    @MockK
    private lateinit var authClient: AuthClient

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        mockkObject(FacebookStandardAuth)
        FacebookStandardAuth.setPrivateProperty("authClient", authClient)
    }

    @Test
    fun signIn() {
        FacebookStandardAuth.signIn()
        verify { authClient.signIn() }
    }

    @Test
    fun setLoginButton() {
        val loginButton = mockk<LoginButton>()
        FacebookStandardAuth.setLoginButton(loginButton)
        verify { authClient.setLoginButton(loginButton) }
    }

    @Test
    fun isSignedIn() {
        every { authClient.isSignedIn() } returns false
        assert(!FacebookStandardAuth.isSignedIn())

        every { authClient.isSignedIn() } returns true
        assert(FacebookStandardAuth.isSignedIn())
    }

    @Test
    fun signOut() {
        FacebookStandardAuth.signOut()
        verify { authClient.signOut(false) }
    }

    @Test
    fun getProfile() {
        FacebookStandardAuth.getProfile()
        verify { authClient.getProfile() }
    }

    @Test
    fun getAccessToken() {
        FacebookStandardAuth.getAccessToken()
        verify { authClient.getAccessToken() }
    }
}
