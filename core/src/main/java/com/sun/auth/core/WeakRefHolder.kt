package com.sun.auth.core

import java.lang.ref.WeakReference
import kotlin.reflect.KProperty

/**
 * Weak reference to an object.
 * @param value The value of the referenced object.
 */
class WeakRefHolder<T>(private var value: WeakReference<T?>) {

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T? {
        return value.get()
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T?) {
        this.value = WeakReference(value)
    }
}

fun <T> weak(value: T) = WeakRefHolder(WeakReference(value))
