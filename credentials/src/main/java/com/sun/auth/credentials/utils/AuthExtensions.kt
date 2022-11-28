package com.sun.auth.credentials.utils

import okhttp3.ResponseBody
import retrofit2.Call

internal fun Call<ResponseBody>.call(): String {
    return execute().body()?.string().orEmpty()
}

internal fun okhttp3.Call.call(): String {
    return execute().body?.string().orEmpty()
}
