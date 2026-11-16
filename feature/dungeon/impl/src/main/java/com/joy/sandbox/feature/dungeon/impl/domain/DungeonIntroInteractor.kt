package com.joy.sandbox.feature.dungeon.impl.domain

import com.joy.sandbox.core.dungeon.model.DungeonMechanic
import com.joy.sandbox.core.dungeon.model.mechanics
import javax.inject.Inject

/**
 * Интро уровня: история уровня + механики, которые игрок ещё не читал.
 * [newMechanics] пуст быть не может — null означает «интро не нужно».
 */
data class DungeonIntro(
    val levelId: Int,
    val levelName: String,
    val levelIntro: String,
    val newMechanics: List<DungeonMechanic>,
)

/** Интро перед уровнем: показывать ли экран и фиксация прочтения. */
interface DungeonIntroInteractor {

    /** Интро для уровня; null — непрочитанных механик нет, можно сразу в игру. */
    suspend fun introForLevel(levelId: Int): DungeonIntro?

    suspend fun markMechanicsSeen(mechanics: List<DungeonMechanic>)
}

class DungeonIntroInteractorImpl @Inject constructor(
    private val levelsRepository: DungeonLevelsRepository,
    private val progressRepository: DungeonProgressRepository,
) : DungeonIntroInteractor {

    override suspend fun introForLevel(levelId: Int): DungeonIntro? {
        val level = levelsRepository.byId(levelId)
        val seen = progressRepository.load().seenMechanics
        val newMechanics = level?.mechanics?.filter { it !in seen }.orEmpty()
        return level?.takeIf { newMechanics.isNotEmpty() }?.let {
            DungeonIntro(
                levelId = it.id,
                levelName = it.name,
                levelIntro = it.intro,
                newMechanics = newMechanics,
            )
        }
    }

    override suspend fun markMechanicsSeen(mechanics: List<DungeonMechanic>) {
        mechanics.forEach { progressRepository.markMechanicSeen(it) }
    }
}
