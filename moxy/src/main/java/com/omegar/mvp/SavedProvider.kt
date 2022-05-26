package com.omegar.mvp

import android.os.Parcel
import android.os.Parcelable
import java.io.Serializable

abstract class SavedProvider<T> {

    abstract fun can(item: Any): Boolean

    abstract fun save(parcel: Parcel, item: T, flags: Int = 0)

    abstract fun load(parcel: Parcel, classLoader: ClassLoader): T

}

object SerializableSavedProvider : SavedProvider<Serializable>() {

    override fun save(parcel: Parcel, item: Serializable, flags: Int) = parcel.writeSerializable(item)

    override fun load(parcel: Parcel, classLoader: ClassLoader): Serializable = parcel.readSerializable()

    override fun can(item: Any): Boolean = item is Serializable

}

object ParcelableSavedProvider : SavedProvider<Parcelable>() {

    override fun save(parcel: Parcel, item: Parcelable, flags: Int) = parcel.writeParcelable(item, flags)

    override fun load(parcel: Parcel, classLoader: ClassLoader): Parcelable = parcel.readParcelable(classLoader)

    override fun can(item: Any): Boolean = item is Parcelable

}
