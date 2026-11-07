package com.joy.sandbox.feature.letters.impl

import androidx.lifecycle.ViewModel
import com.joy.sandbox.core.navigation.ScreenContentProvider
import com.joy.sandbox.core.ui.ScreenKey
import com.joy.sandbox.core.ui.ViewModelKey
import com.joy.sandbox.feature.letters.api.LettersJoyScreen
import com.joy.sandbox.feature.letters.api.LettersSettingsJoyScreen
import com.joy.sandbox.feature.letters.api.NightLettersJoyScreen
import com.joy.sandbox.feature.letters.impl.data.LettersDaySettingsRepositoryImpl
import com.joy.sandbox.feature.letters.impl.data.LettersModeRepositoryImpl
import com.joy.sandbox.feature.letters.impl.data.LettersNightSettingsRepositoryImpl
import com.joy.sandbox.feature.letters.impl.data.LettersSpeakerImpl
import com.joy.sandbox.feature.letters.impl.domain.LettersDaySettingsInteractor
import com.joy.sandbox.feature.letters.impl.domain.LettersDaySettingsInteractorImpl
import com.joy.sandbox.feature.letters.impl.domain.LettersDaySettingsRepository
import com.joy.sandbox.feature.letters.impl.domain.LettersModeInteractor
import com.joy.sandbox.feature.letters.impl.domain.LettersModeInteractorImpl
import com.joy.sandbox.feature.letters.impl.domain.LettersModeRepository
import com.joy.sandbox.feature.letters.impl.domain.LettersNightSettingsInteractor
import com.joy.sandbox.feature.letters.impl.domain.LettersNightSettingsInteractorImpl
import com.joy.sandbox.feature.letters.impl.domain.LettersNightSettingsRepository
import com.joy.sandbox.feature.letters.impl.domain.LettersSpeaker
import com.joy.sandbox.feature.letters.impl.presentation.letters.LettersScreenProvider
import com.joy.sandbox.feature.letters.impl.presentation.letters.LettersViewModel
import com.joy.sandbox.feature.letters.impl.presentation.letters.NightLettersScreenProvider
import com.joy.sandbox.feature.letters.impl.presentation.letters.NightLettersViewModel
import com.joy.sandbox.feature.letters.impl.presentation.settings.LettersSettingsScreenProvider
import com.joy.sandbox.feature.letters.impl.presentation.settings.LettersSettingsViewModel
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap

@Module
class LettersModule {

    @Provides
    fun provideLettersDaySettingsInteractor(
        impl: LettersDaySettingsInteractorImpl,
    ): LettersDaySettingsInteractor = impl

    @Provides
    fun provideLettersNightSettingsInteractor(
        impl: LettersNightSettingsInteractorImpl,
    ): LettersNightSettingsInteractor = impl

    @Provides
    fun provideLettersModeInteractor(
        impl: LettersModeInteractorImpl,
    ): LettersModeInteractor = impl

    @Provides
    fun provideLettersModeRepository(
        impl: LettersModeRepositoryImpl,
    ): LettersModeRepository = impl

    @Provides
    fun provideLettersDaySettingsRepository(
        impl: LettersDaySettingsRepositoryImpl,
    ): LettersDaySettingsRepository = impl

    @Provides
    fun provideLettersNightSettingsRepository(
        impl: LettersNightSettingsRepositoryImpl,
    ): LettersNightSettingsRepository = impl

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
    @ViewModelKey(NightLettersViewModel::class)
    fun provideNightLettersViewModel(viewModel: NightLettersViewModel): ViewModel = viewModel

    @Provides
    @IntoMap
    @ScreenKey(LettersSettingsJoyScreen.ROUTE)
    fun provideLettersSettingsContent(): ScreenContentProvider = LettersSettingsScreenProvider()

    @Provides
    @IntoMap
    @ScreenKey(LettersJoyScreen.ROUTE_PATTERN)
    fun provideLettersContent(): ScreenContentProvider = LettersScreenProvider()

    @Provides
    @IntoMap
    @ScreenKey(NightLettersJoyScreen.ROUTE_PATTERN)
    fun provideNightLettersContent(): ScreenContentProvider = NightLettersScreenProvider()
}
