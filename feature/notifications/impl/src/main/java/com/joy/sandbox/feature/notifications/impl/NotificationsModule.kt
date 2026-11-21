package com.joy.sandbox.feature.notifications.impl

import androidx.lifecycle.ViewModel
import com.joy.sandbox.core.navigation.ScreenContentProvider
import com.joy.sandbox.core.ui.ScreenKey
import com.joy.sandbox.core.ui.ViewModelKey
import com.joy.sandbox.feature.notifications.api.NotificationsJoyScreen
import com.joy.sandbox.feature.notifications.impl.domain.NotificationsInteractor
import com.joy.sandbox.feature.notifications.impl.domain.NotificationsInteractorImpl
import com.joy.sandbox.feature.notifications.impl.presentation.NotificationsScreenProvider
import com.joy.sandbox.feature.notifications.impl.presentation.NotificationsViewModel
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap

/** DI фичи «Уведомления»: ViewModel, экран и привязка интерактора к реализации. */
@Module
class NotificationsModule {

    @Provides
    @IntoMap
    @ViewModelKey(NotificationsViewModel::class)
    fun provideNotificationsViewModel(viewModel: NotificationsViewModel): ViewModel = viewModel

    @Provides
    @IntoMap
    @ScreenKey(NotificationsJoyScreen.ROUTE)
    fun provideNotificationsContent(): ScreenContentProvider = NotificationsScreenProvider()

    @Provides
    fun provideNotificationsInteractor(
        impl: NotificationsInteractorImpl,
    ): NotificationsInteractor = impl
}
