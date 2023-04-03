package com.sun.auth.google.standard

import com.sun.auth.core.setPrivateProperty
import io.mockk.*
import io.mockk.impl.annotations.MockK
import org.junit.Before
import org.junit.Test

class GoogleStandardAuthTest {
    @MockK
    private lateinit var authClient: AuthClient

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        mockkObject(GoogleStandardAuth)
        GoogleStandardAuth.setPrivateProperty("authClient", authClient)
    }

    @Test
    fun signIn() {
        GoogleStandardAuth.signIn()
        verify { authClient.signIn() }
    }

    @Test
    fun isSignedIn() {
        every { authClient.isSignedIn() } returns false
        assert(!GoogleStandardAuth.isSignedIn())

        every { authClient.isSignedIn() } returns true
        assert(GoogleStandardAuth.isSignedIn())
    }

    @Test
    fun signOut() {
        GoogleStandardAuth.signOut(false)
        verify { authClient.signOut(false, null) }
    }

    @Test
    fun getUser() {
        GoogleStandardAuth.getUser()
        verify { authClient.getUser() }
    }

    @Test
    fun showOneTapSignIn() {
        val oneTapSignInCallback = mockk<OneTapSignInCallback>()
        GoogleStandardAuth.showOneTapSignIn(oneTapSignInCallback)
        verify { authClient.showOneTapSignIn(oneTapSignInCallback) }
    }
}
