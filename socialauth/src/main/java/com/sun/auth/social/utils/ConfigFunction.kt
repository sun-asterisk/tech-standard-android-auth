package com.sun.auth.social.utils

internal interface ConfigFunction<T> {
    fun invoke(config: T)
}

internal fun <T> invoke(callback: (T.() -> Unit)? = null) = object : ConfigFunction<T> {
    override fun invoke(config: T) {
        callback?.invoke(config)
    }
}
