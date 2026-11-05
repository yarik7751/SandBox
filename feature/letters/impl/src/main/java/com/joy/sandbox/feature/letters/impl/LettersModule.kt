package com.joy.sandbox.feature.letters.impl

import androidx.lifecycle.ViewModel
import com.joy.sandbox.core.navigation.ScreenContentProvider
import com.joy.sandbox.core.ui.ScreenKey
import com.joy.sandbox.core.ui.ViewModelKey
import com.joy.sandbox.feature.letters.api.LettersJoyScreen
import com.joy.sandbox.feature.letters.api.LettersSettingsJoyScreen
import com.joy.sandbox.feature.letters.impl.data.LettersSettingsRepositoryImpl
import com.joy.sandbox.feature.letters.impl.data.LettersSpeakerImpl
import com.joy.sandbox.feature.letters.impl.domain.LettersInteractor
import com.joy.sandbox.feature.letters.impl.domain.LettersInteractorImpl
import com.joy.sandbox.feature.letters.impl.domain.LettersSettingsRepository
import com.joy.sandbox.feature.letters.impl.domain.LettersSpeaker
import com.joy.sandbox.feature.letters.impl.presentation.letters.LettersScreenProvider
import com.joy.sandbox.feature.letters.impl.presentation.letters.LettersViewModel
import com.joy.sandbox.feature.letters.impl.presentation.settings.LettersSettingsScreenProvider
import com.joy.sandbox.feature.letters.impl.presentation.settings.LettersSettingsViewModel
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap

@Module
class LettersModule {

    @Provides
    fun provideLettersInteractor(impl: LettersInteractorImpl): LettersInteractor = impl

    @Provides
    fun provideLettersSettingsRepository(
        impl: LettersSettingsRepositoryImpl,
    ): LettersSettingsRepository = impl

    @Provides
    fun provideLettersSpeaker(impl: LettersSpeakerImpl): LettersSpeaker = impl

    @Provides
    @IntoMap
    @ViewModelKey(LettersSettingsViewModel::class)
    fun provideLettersSettingsViewModel(viewModel: LettersSettingsViewModel): ViewModel = viewModel

    @Provides
    @IntoMap
    @ViewModelKey(LettersViewModel::class)
    fun provideLettersViewModel(viewModel: LettersViewModel): ViewModel = viewModel

    @Provides
    @IntoMap
    @ScreenKey(LettersSettingsJoyScreen.ROUTE)
    fun provideLettersSettingsContent(): ScreenContentProvider = LettersSettingsScreenProvider()

    @Provides
    @IntoMap
    @ScreenKey(LettersJoyScreen.ROUTE_PATTERN)
    fun provideLettersContent(): ScreenContentProvider = LettersScreenProvider()
}
