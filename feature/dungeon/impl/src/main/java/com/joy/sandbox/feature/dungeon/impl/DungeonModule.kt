package com.joy.sandbox.feature.dungeon.impl

import androidx.lifecycle.ViewModel
import com.joy.sandbox.core.navigation.ScreenContentProvider
import com.joy.sandbox.core.ui.ScreenKey
import com.joy.sandbox.core.ui.ViewModelKey
import com.joy.sandbox.feature.dungeon.api.DungeonGameJoyScreen
import com.joy.sandbox.feature.dungeon.api.DungeonIntroJoyScreen
import com.joy.sandbox.feature.dungeon.api.DungeonJoyScreen
import com.joy.sandbox.feature.dungeon.impl.data.DungeonProgressRepositoryImpl
import com.joy.sandbox.feature.dungeon.impl.data.DungeonSoundPlayerImpl
import com.joy.sandbox.feature.dungeon.impl.data.DungeonVibrationControllerImpl
import com.joy.sandbox.feature.dungeon.impl.domain.DungeonGameInteractor
import com.joy.sandbox.feature.dungeon.impl.domain.DungeonGameInteractorImpl
import com.joy.sandbox.feature.dungeon.impl.domain.DungeonProgressRepository
import com.joy.sandbox.feature.dungeon.impl.domain.DungeonSoundPlayer
import com.joy.sandbox.feature.dungeon.impl.domain.DungeonVibrationController
import com.joy.sandbox.feature.dungeon.impl.presentation.game.DungeonGameScreenProvider
import com.joy.sandbox.feature.dungeon.impl.presentation.game.DungeonGameViewModel
import com.joy.sandbox.feature.dungeon.impl.presentation.intro.DungeonIntroScreenProvider
import com.joy.sandbox.feature.dungeon.impl.presentation.intro.DungeonIntroViewModel
import com.joy.sandbox.feature.dungeon.impl.presentation.title.DungeonTitleScreenProvider
import com.joy.sandbox.feature.dungeon.impl.presentation.title.DungeonTitleViewModel
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap

@Module
class DungeonModule {

    @Provides
    fun provideDungeonGameInteractor(impl: DungeonGameInteractorImpl): DungeonGameInteractor = impl

    @Provides
    fun provideDungeonProgressRepository(impl: DungeonProgressRepositoryImpl): DungeonProgressRepository = impl

    @Provides
    fun provideDungeonSoundPlayer(impl: DungeonSoundPlayerImpl): DungeonSoundPlayer = impl

    @Provides
    fun provideDungeonVibrationController(impl: DungeonVibrationControllerImpl): DungeonVibrationController = impl

    @Provides
    @IntoMap
    @ViewModelKey(DungeonTitleViewModel::class)
    fun provideDungeonTitleViewModel(viewModel: DungeonTitleViewModel): ViewModel = viewModel

    @Provides
    @IntoMap
    @ViewModelKey(DungeonIntroViewModel::class)
    fun provideDungeonIntroViewModel(viewModel: DungeonIntroViewModel): ViewModel = viewModel

    @Provides
    @IntoMap
    @ViewModelKey(DungeonGameViewModel::class)
    fun provideDungeonGameViewModel(viewModel: DungeonGameViewModel): ViewModel = viewModel

    @Provides
    @IntoMap
    @ScreenKey(DungeonJoyScreen.ROUTE)
    fun provideDungeonTitleContent(): ScreenContentProvider = DungeonTitleScreenProvider()

    @Provides
    @IntoMap
    @ScreenKey(DungeonIntroJoyScreen.ROUTE_PATTERN)
    fun provideDungeonIntroContent(): ScreenContentProvider = DungeonIntroScreenProvider()

    @Provides
    @IntoMap
    @ScreenKey(DungeonGameJoyScreen.ROUTE_PATTERN)
    fun provideDungeonGameContent(): ScreenContentProvider = DungeonGameScreenProvider()
}
