package com.sun.auth.credentials.repositories.remote

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Url

internal interface NonAuthApi {

    @POST
    fun signIn(@Url url: String, @Body requestBody: Any?): Call<ResponseBody>
}
