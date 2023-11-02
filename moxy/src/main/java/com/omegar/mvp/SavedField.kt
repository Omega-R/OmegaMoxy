package com.omegar.mvp

import kotlin.reflect.KProperty

class SavedField<T>(initValue: T, private val nullValue: T) {

    internal var value: T? = initValue

    constructor(initValue: T): this(initValue, initValue)

    @Suppress("UNCHECKED_CAST")
    operator fun getValue(thisRef: Any, property: KProperty<*>): T = value ?: nullValue

    operator fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
        this.value = value
    }
}