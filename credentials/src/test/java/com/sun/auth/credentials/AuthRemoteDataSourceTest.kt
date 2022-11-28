package com.sun.auth.credentials

import com.sun.auth.base.BaseUnitTest
import com.sun.auth.credentials.repositories.remote.NonAuthApi
import com.sun.auth.credentials.repositories.remote.AuthRemoteDataSource
import com.sun.auth.credentials.utils.call
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import okhttp3.Call
import org.junit.Test

class AuthRemoteDataSourceTest : BaseUnitTest() {
    @MockK(relaxed = true)
    private lateinit var nonAuthApi: NonAuthApi

    @InjectMockKs
    private lateinit var remoteDataSource: AuthRemoteDataSource

    override fun setup() {
        super.setup()
        MockKAnnotations.init(this)
    }

    @Test
    fun login() {
        every { nonAuthApi.login(any(), any()).call() } answers { "" }
        remoteDataSource.login("", "")
        verify { nonAuthApi.login("", "") }
        confirmVerified(nonAuthApi)
    }

    @Test
    fun refreshToken() {
        val call = mockk<Call>()
        every { call.call() } answers { "" }
        remoteDataSource.refreshToken(call)
        verify { call.call() }
        confirmVerified(call)
    }
}
