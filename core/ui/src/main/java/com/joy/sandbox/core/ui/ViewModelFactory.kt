package com.joy.sandbox.core.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import javax.inject.Provider

/**
 * Создаётся в :app через Dagger (NavigationModule): собирает мапу
 * multibinding'ов ViewModel, накопленную Dagger-модулями фич.
 */
class ViewModelFactory(
    private val creators: Map<Class<out ViewModel>, @JvmSuppressWildcards Provider<ViewModel>>
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val creator = creators[modelClass] ?: throw IllegalArgumentException("Unknown model class $modelClass")
        return creator.get() as T
    }
}
