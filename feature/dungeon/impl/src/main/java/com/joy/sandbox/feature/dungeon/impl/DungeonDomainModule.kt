package com.joy.sandbox.feature.dungeon.impl

import com.joy.sandbox.feature.dungeon.impl.data.DungeonProgressRepositoryImpl
import com.joy.sandbox.feature.dungeon.impl.data.DungeonSoundPlayerImpl
import com.joy.sandbox.feature.dungeon.impl.data.DungeonVibrationControllerImpl
import com.joy.sandbox.feature.dungeon.impl.domain.DungeonGameInteractor
import com.joy.sandbox.feature.dungeon.impl.domain.DungeonGameInteractorImpl
import com.joy.sandbox.feature.dungeon.impl.domain.DungeonProgressRepository
import com.joy.sandbox.feature.dungeon.impl.domain.DungeonSoundPlayer
import com.joy.sandbox.feature.dungeon.impl.domain.DungeonVibrationController
import dagger.Module
import dagger.Provides

/** Доменные зависимости фичи «Подземелье»: репозиторий прогресса, звук, вибрация, игровой интерактор. */
@Module
class DungeonDomainModule {

    @Provides
    fun provideDungeonGameInteractor(impl: DungeonGameInteractorImpl): DungeonGameInteractor = impl

    @Provides
    fun provideDungeonProgressRepository(impl: DungeonProgressRepositoryImpl): DungeonProgressRepository = impl

    @Provides
    fun provideDungeonSoundPlayer(impl: DungeonSoundPlayerImpl): DungeonSoundPlayer = impl

    @Provides
    fun provideDungeonVibrationController(impl: DungeonVibrationControllerImpl): DungeonVibrationController = impl
}
