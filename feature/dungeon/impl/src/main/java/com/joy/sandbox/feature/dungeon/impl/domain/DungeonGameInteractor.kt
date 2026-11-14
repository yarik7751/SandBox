package com.joy.sandbox.feature.dungeon.impl.domain

import com.joy.sandbox.core.dungeon.ActResult
import com.joy.sandbox.core.dungeon.DungeonAction
import com.joy.sandbox.core.dungeon.DungeonEngine
import com.joy.sandbox.core.dungeon.model.DungeonLevel
import com.joy.sandbox.core.dungeon.model.DungeonState
import com.joy.sandbox.core.dungeon.model.Pos
import javax.inject.Inject

/** Доступ к игровому ядру «Подземелья» со стороны presentation. */
interface DungeonGameInteractor {

    fun createState(level: DungeonLevel): DungeonState

    fun act(state: DungeonState, action: DungeonAction): ActResult

    fun threatRooms(state: DungeonState): Set<Pos>
}

class DungeonGameInteractorImpl @Inject constructor() : DungeonGameInteractor {

    override fun createState(level: DungeonLevel): DungeonState = DungeonEngine.create(level)

    override fun act(state: DungeonState, action: DungeonAction): ActResult = DungeonEngine.act(state, action)

    override fun threatRooms(state: DungeonState): Set<Pos> = DungeonEngine.threatRooms(state)
}
