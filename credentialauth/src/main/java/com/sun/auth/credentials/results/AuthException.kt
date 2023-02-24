package com.sun.auth.credentials.results

/**
 * Wrapper of the error when authenticate with given info.
 * @param originalThrowable The origin error.
 */
class AuthException internal constructor(originalThrowable: Throwable?) :
    RuntimeException(originalThrowable)
