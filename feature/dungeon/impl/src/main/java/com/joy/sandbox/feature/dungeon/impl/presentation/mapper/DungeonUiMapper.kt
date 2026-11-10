package com.joy.sandbox.feature.dungeon.impl.presentation.mapper

import com.joy.sandbox.core.dungeon.DungeonEngine
import com.joy.sandbox.core.dungeon.model.DungeonState
import com.joy.sandbox.core.dungeon.model.GameOver
import com.joy.sandbox.core.dungeon.model.Pos
import com.joy.sandbox.feature.dungeon.impl.presentation.model.DungeonGameUiModel
import com.joy.sandbox.feature.dungeon.impl.presentation.model.HostageUiModel
import com.joy.sandbox.feature.dungeon.impl.presentation.model.OverUiModel
import com.joy.sandbox.feature.dungeon.impl.presentation.model.PosUi
import javax.inject.Inject

/** Дополнительные данные экрана, не входящие в доменное состояние. */
data class GameUiParams(
    val canUndo: Boolean,
    val soundEnabled: Boolean,
    val vibrationEnabled: Boolean,
    val moveSeq: Long,
    val newRecord: Boolean,
)

/** Маппинг доменного состояния игры в [DungeonGameUiModel]. */
class DungeonUiMapper @Inject constructor() {

    fun mapGame(state: DungeonState, params: GameUiParams): DungeonGameUiModel = DungeonGameUiModel(
        levelId = state.level.id,
        levelName = state.level.name,
        width = state.level.w,
        height = state.level.h,
        turns = state.turns,
        par = state.level.par,
        player = state.player.toUi(),
        enemy = state.enemy.toUi(),
        hostages = state.hostages.map { hostage ->
            HostageUiModel(
                id = hostage.id,
                room = hostage.room.toUi(),
                freed = hostage.freed,
                exited = hostage.exited,
            )
        },
        openDoors = state.openDoors.map { it.toString() }.toSet(),
        threatRooms = DungeonEngine.threatRooms(state).map { it.toUi() }.toSet(),
        exitRoom = state.exitRoom.toUi(),
        enemyTrapped = state.enemyTrapped,
        over = state.over?.let { over -> mapOver(over, params.newRecord) },
        canUndo = params.canUndo,
        soundEnabled = params.soundEnabled,
        vibrationEnabled = params.vibrationEnabled,
        moveSeq = params.moveSeq,
    )

    private fun mapOver(over: GameOver, newRecord: Boolean): OverUiModel = OverUiModel(
        win = over.result == GameOver.Result.WIN,
        reason = over.reason,
        newRecord = newRecord,
    )

    private fun Pos.toUi(): PosUi = PosUi(x, y)
}
