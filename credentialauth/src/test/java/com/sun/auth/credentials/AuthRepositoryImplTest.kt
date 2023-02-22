package com.sun.auth.credentials

import com.google.gson.Gson
import com.sun.auth.base.BaseUnitTest
import com.sun.auth.credentials.repositories.AuthRepositoryImpl
import com.sun.auth.credentials.repositories.local.AuthLocalDataSource
import com.sun.auth.credentials.repositories.model.AuthToken
import com.sun.auth.credentials.repositories.remote.AuthRemoteDataSource
import io.mockk.* // ktlint-disable no-wildcard-imports
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.runBlocking
import org.junit.Test

class AuthRepositoryImplTest : BaseUnitTest() {
    @MockK
    private lateinit var remote: AuthRemoteDataSource

    @MockK
    private lateinit var local: AuthLocalDataSource

    @MockK
    private lateinit var gson: Gson

    @InjectMockKs
    private lateinit var authRepositoryImpl: AuthRepositoryImpl

    override fun setup() {
        super.setup()
        MockKAnnotations.init(this)
    }

    @Test
    fun signIn() {
        every { remote.signIn(any(), any()) } returns ""
        every { gson.fromJson("", AuthToken::class.java) } returns mockk()
        every { local.saveToken(any()) } returns Unit

        val token = runBlocking {
            authRepositoryImpl.signIn(
                url = "",
                requestBody = "",
                responseClazz = AuthToken::class.java,
            )
        }
        verifyOrder {
            remote.signIn(url = "", requestBody = "")
            gson.fromJson("", AuthToken::class.java)
            local.saveToken(token)
        }
    }

    @Test
    fun refreshToken() {
        val call = mockk<okhttp3.Call>()
        every { remote.refreshToken(any()) } returns ""
        every { local.saveToken(any()) } returns Unit
        every { gson.fromJson("", AuthToken::class.java) } returns mockk()
        runBlocking {
            authRepositoryImpl.refreshToken(
                request = call,
                responseClazz = AuthToken::class.java,
            )
        }
        verifyOrder {
            remote.refreshToken(call)
            gson.fromJson("", AuthToken::class.java)
            local.saveToken(any())
        }
    }

    @Test
    fun saveToken() {
        val token = mockk<AuthToken>()
        every { local.saveToken(any()) } returns Unit
        authRepositoryImpl.saveToken(token)
        verify { local.saveToken(token) }
    }

    @Test
    fun getToken() {
        every { local.getToken<AuthToken>(any()) } returns mockk()
        authRepositoryImpl.getToken(AuthToken::class.java)
        verify { local.getToken(AuthToken::class.java) }
    }

    @Test
    fun removeToken() {
        every { local.removeToken() } returns Unit
        authRepositoryImpl.removeToken()
        verify { local.removeToken() }
    }
}
