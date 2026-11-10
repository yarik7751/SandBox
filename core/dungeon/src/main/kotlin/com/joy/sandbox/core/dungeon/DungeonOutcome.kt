package com.joy.sandbox.core.dungeon

import com.joy.sandbox.core.dungeon.model.DungeonState
import com.joy.sandbox.core.dungeon.model.GameOver
import com.joy.sandbox.core.dungeon.model.Pos

/** Проверки исхода хода: гибель игрока/пленников и победа. Порядок — смерти до победы. */
internal object DungeonOutcome {

    /** Проверка гибели. Возвращает состояние с [GameOver], либо null если все живы. */
    fun checkDeaths(state: DungeonState, events: MutableList<DungeonEvent>): DungeonState? {
        if (state.over != null) return state
        return if (state.enemy == state.player) {
            events += DungeonEvent.Death(who = Mover.PLAYER)
            state.copy(
                over = GameOver(
                    result = GameOver.Result.LOSE,
                    reason = "Повелитель Тьмы настиг вас во тьме.",
                ),
            )
        } else {
            hostageDeath(state, events)
        }
    }

    private fun hostageDeath(state: DungeonState, events: MutableList<DungeonEvent>): DungeonState? {
        val victim = state.hostages.firstOrNull { !it.exited && it.room == state.enemy } ?: return null
        events += DungeonEvent.Death(who = Mover.HOSTAGE, hostageId = victim.id)
        return state.copy(
            hostages = state.hostages.map {
                if (it.id == victim.id) it.copy(dead = true) else it
            },
            over = GameOver(
                result = GameOver.Result.LOSE,
                reason = "Повелитель Тьмы добрался до пленников.",
            ),
        )
    }

    fun checkWin(state: DungeonState, events: MutableList<DungeonEvent>): DungeonState =
        if (state.over != null || !state.hostages.all { it.exited }) {
            state
        } else {
            events += DungeonEvent.Win
            state.copy(
                over = GameOver(
                    result = GameOver.Result.WIN,
                    reason = "Все пленники выведены из подземелья.",
                ),
            )
        }
}
