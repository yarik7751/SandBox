package com.joy.sandbox.feature.letters.impl

import androidx.lifecycle.ViewModel
import com.joy.sandbox.core.navigation.ScreenContentProvider
import com.joy.sandbox.core.ui.ScreenKey
import com.joy.sandbox.core.ui.ViewModelKey
import com.joy.sandbox.feature.letters.api.LettersDaySettingsJoyScreen
import com.joy.sandbox.feature.letters.api.LettersJoyScreen
import com.joy.sandbox.feature.letters.api.LettersMainJoyScreen
import com.joy.sandbox.feature.letters.api.LettersNightSettingsJoyScreen
import com.joy.sandbox.feature.letters.api.NightLettersJoyScreen
import com.joy.sandbox.feature.letters.impl.data.LettersDaySettingsRepositoryImpl
import com.joy.sandbox.feature.letters.impl.data.LettersNightSettingsRepositoryImpl
import com.joy.sandbox.feature.letters.impl.data.LettersSpeakerImpl
import com.joy.sandbox.feature.letters.impl.domain.LettersDaySettingsInteractor
import com.joy.sandbox.feature.letters.impl.domain.LettersDaySettingsInteractorImpl
import com.joy.sandbox.feature.letters.impl.domain.LettersDaySettingsRepository
import com.joy.sandbox.feature.letters.impl.domain.LettersNightSettingsInteractor
import com.joy.sandbox.feature.letters.impl.domain.LettersNightSettingsInteractorImpl
import com.joy.sandbox.feature.letters.impl.domain.LettersNightSettingsRepository
import com.joy.sandbox.feature.letters.impl.domain.LettersSpeaker
import com.joy.sandbox.feature.letters.impl.presentation.letters.LettersScreenProvider
import com.joy.sandbox.feature.letters.impl.presentation.letters.LettersViewModel
import com.joy.sandbox.feature.letters.impl.presentation.letters.NightLettersScreenProvider
import com.joy.sandbox.feature.letters.impl.presentation.letters.NightLettersViewModel
import com.joy.sandbox.feature.letters.impl.presentation.main.LettersMainScreenProvider
import com.joy.sandbox.feature.letters.impl.presentation.main.LettersMainViewModel
import com.joy.sandbox.feature.letters.impl.presentation.settings.day.LettersDaySettingsScreenProvider
import com.joy.sandbox.feature.letters.impl.presentation.settings.day.LettersDaySettingsViewModel
import com.joy.sandbox.feature.letters.impl.presentation.settings.night.LettersNightSettingsScreenProvider
import com.joy.sandbox.feature.letters.impl.presentation.settings.night.LettersNightSettingsViewModel
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
    @ViewModelKey(LettersMainViewModel::class)
    fun provideLettersMainViewModel(viewModel: LettersMainViewModel): ViewModel = viewModel

    @Provides
    @IntoMap
    @ViewModelKey(LettersDaySettingsViewModel::class)
    fun provideLettersDaySettingsViewModel(viewModel: LettersDaySettingsViewModel): ViewModel = viewModel

    @Provides
    @IntoMap
    @ViewModelKey(LettersNightSettingsViewModel::class)
    fun provideLettersNightSettingsViewModel(viewModel: LettersNightSettingsViewModel): ViewModel =
        viewModel

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
    @ScreenKey(LettersMainJoyScreen.ROUTE)
    fun provideLettersMainContent(): ScreenContentProvider = LettersMainScreenProvider()

    @Provides
    @IntoMap
    @ScreenKey(LettersDaySettingsJoyScreen.ROUTE)
    fun provideLettersDaySettingsContent(): ScreenContentProvider = LettersDaySettingsScreenProvider()

    @Provides
    @IntoMap
    @ScreenKey(LettersNightSettingsJoyScreen.ROUTE)
    fun provideLettersNightSettingsContent(): ScreenContentProvider = LettersNightSettingsScreenProvider()

    @Provides
    @IntoMap
    @ScreenKey(LettersJoyScreen.ROUTE_PATTERN)
    fun provideLettersContent(): ScreenContentProvider = LettersScreenProvider()

    @Provides
    @IntoMap
    @ScreenKey(NightLettersJoyScreen.ROUTE_PATTERN)
    fun provideNightLettersContent(): ScreenContentProvider = NightLettersScreenProvider()
}
