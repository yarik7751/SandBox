package com.joy.sandbox.core.dungeon

import com.joy.sandbox.core.dungeon.model.DoorId
import com.joy.sandbox.core.dungeon.model.Pos

/** Действие игрока за ход. */
sealed interface DungeonAction {

    /** Переместиться в соседнюю комнату [to] через открытую дверь. */
    data class Move(val to: Pos) : DungeonAction

    /** Переключить состояние двери [door], примыкающей к комнате игрока. */
    data class Toggle(val door: DoorId) : DungeonAction
}
