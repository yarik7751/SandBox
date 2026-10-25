package com.joy.sandbox.core.navigation

import android.os.Parcel
import android.os.Parcelable

/**
 * Заглушка аргументов для экранов без аргументов.
 *
 * Реализовано без kotlin-parcelize (встроенный Kotlin AGP не подключает
 * его compiler plugin): объект-синглтон маршаллится в пустой Parcel
 * и никогда не восстанавливается из него.
 */
object NoArgs : Parcelable {

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = Unit

    @Suppress("unused")
    @JvmField
    val CREATOR: Parcelable.Creator<NoArgs> = object : Parcelable.Creator<NoArgs> {

        override fun createFromParcel(source: Parcel): NoArgs = NoArgs

        override fun newArray(size: Int): Array<NoArgs?> = arrayOfNulls(size)
    }
}
