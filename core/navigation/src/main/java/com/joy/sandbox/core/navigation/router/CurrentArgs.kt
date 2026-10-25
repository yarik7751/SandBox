package com.joy.sandbox.core.navigation.router

import android.os.Parcelable

interface CurrentArgs {

    fun <A : Parcelable> get(): A
}
