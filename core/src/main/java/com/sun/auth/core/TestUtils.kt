package com.sun.auth.core

inline fun <reified T : Any, R> T.setPrivateProperty(name: String, mock: R) =
    T::class.java
        .declaredFields
        .firstOrNull { it.name == name }
        ?.apply { isAccessible = true }
        ?.set(this, mock)

inline fun <reified T> T.callPrivateFunc(name: String, vararg args: Any?): Any? =
    T::class.java
        .declaredMethods
        .firstOrNull { it.name == name }
        ?.apply { isAccessible = true }
        ?.invoke(this, *args)

inline fun <reified T : Any, R> T.getPrivateProperty(name: String): R? =
    T::class.java
        .declaredFields
        .firstOrNull { it.name == name }
        ?.apply { isAccessible = true }
        ?.get(this) as? R
