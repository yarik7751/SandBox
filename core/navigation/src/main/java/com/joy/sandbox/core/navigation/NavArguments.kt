package com.joy.sandbox.core.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.navigation.NavBackStackEntry

/**
 * Аргумент текущего destination с имени [name], приведённый к Long.
 * Экран читает свои nav-аргументы через эти хелперы в начале композиции
 * и передаёт во ViewModel (см. JoyScreenWithArgs).
 */
@Composable
fun currentNavLongArgument(name: String): Long? {
    val entry = LocalViewModelStoreOwner.current as? NavBackStackEntry
    return entry?.arguments?.getLong(name)
}

/**
 * Строковый аргумент текущего destination с имени [name].
 */
@Composable
fun currentNavStringArgument(name: String): String? {
    val entry = LocalViewModelStoreOwner.current as? NavBackStackEntry
    return entry?.arguments?.getString(name)
}
