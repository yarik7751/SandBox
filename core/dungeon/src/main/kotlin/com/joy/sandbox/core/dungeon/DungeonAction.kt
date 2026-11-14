package com.joy.sandbox.core.dungeon

import com.joy.sandbox.core.dungeon.model.CharacterRef
import com.joy.sandbox.core.dungeon.model.DoorId
import com.joy.sandbox.core.dungeon.model.Pos

/** Действие игрока. Ходом считается [Move] (за 1 очко) и [Toggle] героем;
 *  [Switch] — бесплатное переключение активного персонажа, не ход. */
sealed interface DungeonAction {

    /** Переместить активного персонажа в соседнюю комнату [to] через открытую дверь. */
    data class Move(val to: Pos) : DungeonAction

    /** Переключить состояние двери [door], примыкающей к комнате героя (двери — только герой). */
    data class Toggle(val door: DoorId) : DungeonAction

    /** Сделать [to] активным персонажем: не ход, враг не отвечает. */
    data class Switch(val to: CharacterRef) : DungeonAction
}
