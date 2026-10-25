package com.joy.sandbox.utils

import kotlinx.coroutines.flow.MutableStateFlow

fun <T> MutableStateFlow<T>.update(action: (T) -> T) {
    this.value = action(this.value)
}
