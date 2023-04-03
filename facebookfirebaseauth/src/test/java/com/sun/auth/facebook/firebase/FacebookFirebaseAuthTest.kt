package com.sun.auth.facebook.firebase

import com.facebook.login.widget.LoginButton
import com.sun.auth.core.PROVIDER_FACEBOOK
import com.sun.auth.core.setPrivateProperty
import io.mockk.*
import io.mockk.impl.annotations.MockK
import org.junit.Before
import org.junit.Test

class FacebookFirebaseAuthTest {
    @MockK
    private lateinit var authClient: AuthClient

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        mockkObject(FacebookFirebaseAuth)
        FacebookFirebaseAuth.setPrivateProperty("authClient", authClient)
    }

    @Test
    fun signIn() {
        FacebookFirebaseAuth.signIn()
        verify { authClient.signIn() }
    }

    @Test
    fun setLoginButton() {
        val loginButton = mockk<LoginButton>()
        FacebookFirebaseAuth.setLoginButton(loginButton)
        verify { authClient.setLoginButton(loginButton) }
    }

    @Test
    fun isSignedIn() {
        every { authClient.isSignedIn() } returns false
        assert(!FacebookFirebaseAuth.isSignedIn())

        every { authClient.isSignedIn() } returns true
        assert(FacebookFirebaseAuth.isSignedIn())
    }

    @Test
    fun signOut() {
        FacebookFirebaseAuth.signOut()
        verify { authClient.signOut(false, null) }
    }

    @Test
    fun getProfile() {
        FacebookFirebaseAuth.getProfile()
        verify { authClient.getProfile() }
    }

    @Test
    fun getAccessToken() {
        FacebookFirebaseAuth.getAccessToken()
        verify { authClient.getAccessToken() }
    }

    @Test
    fun getFirebaseUser() {
        FacebookFirebaseAuth.getFirebaseUser()
        verify { authClient.getUser() }
    }

    @Test
    fun getLinkedAccounts() {
        FacebookFirebaseAuth.getLinkedAccounts(PROVIDER_FACEBOOK)
        verify { authClient.getLinkedAccounts(PROVIDER_FACEBOOK) }
    }
}
