package com.sun.auth.core

import kotlin.reflect.KClass

inline fun <R, T : R> Result<T>.onException(
    vararg exceptions: KClass<out Throwable>,
    transform: (exception: Throwable) -> T,
) = recoverCatching { exception ->
    exception.printStackTrace()
    if (exceptions.any { it.isInstance(exception) }) {
        transform(exception)
    } else {
        throw exception
    }
}
