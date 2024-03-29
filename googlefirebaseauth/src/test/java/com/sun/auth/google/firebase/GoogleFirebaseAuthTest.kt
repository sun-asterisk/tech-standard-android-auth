package com.sun.auth.google.firebase

import android.content.Context
import com.sun.auth.core.PROVIDER_GOOGLE
import com.sun.auth.core.setPrivateProperty
import io.mockk.*
import io.mockk.impl.annotations.MockK
import org.junit.Before
import org.junit.Test

class GoogleFirebaseAuthTest {
    @MockK
    private lateinit var authClient: AuthClient

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        mockkObject(GoogleFirebaseAuth)
        GoogleFirebaseAuth.setPrivateProperty("authClient", authClient)
    }

    @Test
    fun initialize() {
        val context = mockk<Context>().apply {
            every { applicationContext } returns mockk()
        }
        val config = mockk<GoogleConfig>()
        GoogleFirebaseAuth.initialize(context, config)
        verify {
            context.applicationContext
        }
    }

    @Test
    fun signIn() {
        GoogleFirebaseAuth.signIn()
        verify { authClient.signIn() }
    }

    @Test
    fun isSignedIn() {
        every { authClient.isSignedIn() } returns false
        assert(!GoogleFirebaseAuth.isSignedIn())

        every { authClient.isSignedIn() } returns true
        assert(GoogleFirebaseAuth.isSignedIn())
    }

    @Test
    fun signOut() {
        GoogleFirebaseAuth.signOut()
        verify { authClient.signOut(false, null) }
    }

    @Test
    fun getUser() {
        GoogleFirebaseAuth.getUser()
        verify { authClient.getUser() }
    }

    @Test
    fun getLinkedAccounts() {
        GoogleFirebaseAuth.getLinkedAccounts(PROVIDER_GOOGLE)
        verify { authClient.getLinkedAccounts(PROVIDER_GOOGLE) }
    }

    @Test
    fun showOneTapSignIn() {
        GoogleFirebaseAuth.showOneTapSignIn()
        verify { authClient.showOneTapSignIn() }
    }
}
