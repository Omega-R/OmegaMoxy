package com.omegar.mvp

import android.os.Parcel
import android.os.Parcelable
import com.omegar.mvp.viewstate.SerializeType.*
import com.omegar.mvp.viewstate.ViewCommand
import java.io.Serializable

internal class SavedState(val list: List<ViewCommand<*>>) : Parcelable {

    constructor(parcel: Parcel) : this(parcel.readList())

    override fun describeContents(): Int = 0


    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(list.count { it is Serializable || it is Parcelable })
        list.forEach {
            when (it) {
                is Parcelable -> {
                    dest.writeByte(PARCELABLE.ordinal.toByte())
                    dest.writeParcelable(it, 0)
                }
                is Serializable -> {
                    dest.writeByte(SERIALIZABLE.ordinal.toByte())
                    dest.writeSerializable(it)
                }
            }
        }
    }

    companion object CREATOR : Parcelable.Creator<SavedState> {

        override fun createFromParcel(parcel: Parcel): SavedState {
            return SavedState(parcel.readList())
        }

       private fun Parcel.readList(): List<ViewCommand<out MvpView>> {
            val size = readInt()
            return (0 until size).map {
                val typeIndex = readByte().toInt()
                when (values()[typeIndex]) {
                    PARCELABLE -> readParcelable(ViewCommand::class.java.classLoader) as ViewCommand<*>
                    SERIALIZABLE -> readSerializable() as ViewCommand<*>
                    NONE -> throw IllegalArgumentException("none")
                }
            }
        }

        override fun newArray(size: Int): Array<SavedState?> {
            return arrayOfNulls(size)
        }
    }
}