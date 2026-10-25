package com.joy.sandbox.core.navigation

import androidx.compose.runtime.Composable
import com.joy.sandbox.core.navigation.router.JoyRouter
import com.joy.sandbox.core.ui.ViewModelFactory

/**
 * Поставщик контента экрана для графа навигации.
 *
 * Каждая фича-impl предоставляет реализацию и регистрирует её в Dagger-мапе
 * (route → ScreenContentProvider); NavHost в :app строит destinations по этой
 * мапе, не зная конкретных экранов.
 */
interface ScreenContentProvider {

    val screen: JoyScreen<*>

    @Composable
    fun Content(viewModelFactory: ViewModelFactory, router: JoyRouter)
}
