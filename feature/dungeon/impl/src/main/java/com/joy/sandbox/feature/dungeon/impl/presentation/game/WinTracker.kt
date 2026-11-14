package com.joy.sandbox.feature.dungeon.impl.presentation.game

import com.joy.sandbox.core.dungeon.model.DungeonLevels
import com.joy.sandbox.core.dungeon.model.DungeonState
import com.joy.sandbox.core.dungeon.model.GameOver
import com.joy.sandbox.feature.dungeon.impl.domain.DungeonProgressRepository
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Запись победного прогресса: рекорд ходов и разблокировка следующего
 * уровня — ровно один раз за партию. [newRecord] читается UI после
 * сохранения; [reset] вызывается при рестарте уровня.
 */
class WinTracker @Inject constructor(
    private val progressRepository: DungeonProgressRepository,
) {

    private var recorded = false

    var newRecord = false
        private set

    fun reset() {
        recorded = false
        newRecord = false
    }

    /** Если партия выиграна и ещё не записана — сохраняет прогресс в фоне,
     *  по завершении вызывает [onSaved] (UI перечитывает [newRecord]). */
    fun recordWinIfNeeded(scope: CoroutineScope, state: DungeonState, onSaved: () -> Unit) {
        val over = state.over
        if (recorded || over?.result != GameOver.Result.WIN) return
        recorded = true
        val levelId = state.level.id
        scope.launch {
            val progress = progressRepository.load()
            val best = progress.bestTurns[levelId]
            newRecord = best == null || state.turns < best
            if (newRecord) progressRepository.saveBest(levelId, state.turns)
            if (levelId < DungeonLevels.ALL.size) {
                progressRepository.saveUnlocked(maxOf(progress.unlockedLevel, levelId + 1))
            }
            onSaved()
        }
    }
}
