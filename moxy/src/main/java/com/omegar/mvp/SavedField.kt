package com.omegar.mvp

import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import java.io.Serializable
import kotlin.reflect.KProperty

sealed class SavedField<T>(initValue: T) {

    protected var value = initValue

    abstract fun save(bundle: Bundle)

    abstract fun load(bundle: Bundle)

    @Suppress("UNCHECKED_CAST")
    operator fun getValue(thisRef: Any, property: KProperty<*>): T = value

    operator fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
        this.value = value
    }

    class SerializableSavedField<T: Serializable>(initValue: T, private val key: String): SavedField<T>(initValue) {

        override fun save(bundle: Bundle) = bundle.putSerializable(key, value)

        @Suppress("UNCHECKED_CAST")
        override fun load(bundle: Bundle) {
            value = bundle.getSerializable(key) as? T ?: value
        }

    }

    class NullSerializableSavedField<T: Serializable>(initValue: T?, private val key: String): SavedField<T?>(initValue) {

        override fun save(bundle: Bundle) = bundle.putSerializable(key, value)

        @Suppress("UNCHECKED_CAST")
        override fun load(bundle: Bundle) {
            if (bundle.containsKey(key)) {
                value = bundle.getSerializable(key) as? T?
            }
        }

    }

    class ParcelableSavedField<T: Parcelable>(initValue: T, private val key: String): SavedField<T>(initValue) {

        override fun save(bundle: Bundle) = bundle.putParcelable(key, value)

        override fun load(bundle: Bundle) {
            value = bundle.getParcelable(key) ?: value
        }
    }

    class NullParcelableSavedField<T: Parcelable>(initValue: T?, private val key: String): SavedField<T?>(initValue) {

        override fun save(bundle: Bundle) = bundle.putParcelable(key, value)

        override fun load(bundle: Bundle) {
            if (bundle.containsKey(key)) {
                value = bundle.getParcelable(key)
            }
        }
    }

}