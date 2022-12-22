package com.sun.auth.credentials

import com.sun.auth.base.BaseUnitTest
import com.sun.auth.credentials.repositories.model.AuthToken
import com.sun.auth.credentials.repositories.local.SharedPrefApi
import com.sun.auth.credentials.repositories.local.AuthLocalDataSource
import com.sun.auth.credentials.utils.PREF_AUTH_TOKEN
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Test

class AuthLocalDataSourceTest : BaseUnitTest() {

    @MockK(relaxed = true)
    private lateinit var sharedPrefApi: SharedPrefApi

    @InjectMockKs
    private lateinit var localDataSource: AuthLocalDataSource

    override fun setup() {
        super.setup()
        MockKAnnotations.init(this)
    }

    @Test
    fun saveToken() {
        val token = mockk<AuthToken>()
        every { sharedPrefApi.put(any(), any<AuthToken>()) } returns Unit
        localDataSource.saveToken(token)
        verify { sharedPrefApi.put(PREF_AUTH_TOKEN, token) }
    }

    @Test
    fun getToken() {
        val clazz = AuthToken::class.java
        val token = mockk<AuthToken>()
        every { sharedPrefApi.get(any(), clazz) } returns token
        val savedToken = localDataSource.getToken(clazz)
        verify { sharedPrefApi.get(PREF_AUTH_TOKEN, clazz) }
        assertEquals(token, savedToken)
    }

    @Test
    fun removeToken() {
        every { sharedPrefApi.removeKey(any()) } returns Unit
        localDataSource.removeToken()
        verify { sharedPrefApi.removeKey(PREF_AUTH_TOKEN) }
    }
}
