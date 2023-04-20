package com.sun.auth.core

import kotlin.reflect.KClass

inline fun <R, T : R> Result<T>.onException(
    vararg exceptions: KClass<out Throwable>,
    transform: (exception: Throwable) -> T,
) = recoverCatching { e ->
    if (e::class in exceptions) {
        e.printStackTrace()
        transform(e)
    } else {
        throw e
    }
}
