package com.joy.sandbox.core.dungeon

import com.joy.sandbox.core.dungeon.model.DoorId
import com.joy.sandbox.core.dungeon.model.DungeonLevel
import com.joy.sandbox.core.dungeon.model.DungeonLevels
import com.joy.sandbox.core.dungeon.model.DungeonMechanic
import com.joy.sandbox.core.dungeon.model.Pos
import com.joy.sandbox.core.dungeon.model.mechanics
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Какие механики включаются на уровне по его модификаторам. */
class DungeonMechanicTest {

    @Test
    fun `first level has only base mechanic`() {
        assertEquals(listOf(DungeonMechanic.BASE), DungeonLevels.byId(1).mechanics)
    }

    @Test
    fun `handmade levels 2-5 have no new mechanics`() {
        // Рукотворная пятёрка использует только изначально закрытые двери —
        // отдельной механики это не вводит.
        (2..5).forEach { id ->
            assertEquals(emptyList<DungeonMechanic>(), DungeonLevels.byId(id).mechanics)
        }
    }

    @Test
    fun `level without modifiers has empty mechanics`() {
        val level = DungeonLevels.byId(1).copy(id = 101)
        assertEquals(emptyList<DungeonMechanic>(), level.mechanics)
    }

    @Test
    fun `jammed doors enable jammed mechanic`() {
        val level = base().copy(jammed = setOf(DoorId.horizontal(0, 0)))
        assertEquals(listOf(DungeonMechanic.JAMMED), level.mechanics)
    }

    @Test
    fun `concealed cells enable concealed mechanic`() {
        val level = base().copy(concealed = setOf(Pos(1, 1)))
        assertEquals(listOf(DungeonMechanic.CONCEALED), level.mechanics)
    }

    @Test
    fun `blocked cells enable blocked mechanic`() {
        val level = base().copy(blocked = setOf(Pos(1, 1)))
        assertEquals(listOf(DungeonMechanic.BLOCKED), level.mechanics)
    }

    @Test
    fun `limited toggles enable toggles budget mechanic`() {
        val level = base().copy(togglesBudget = 5)
        assertEquals(listOf(DungeonMechanic.TOGGLES_BUDGET), level.mechanics)
    }

    @Test
    fun `unlimited toggles do not enable toggles budget mechanic`() {
        assertTrue(base().mechanics.isEmpty())
    }

    @Test
    fun `enemy door charge enables its mechanic`() {
        val level = base().copy(enemyDoorCharge = 1)
        assertEquals(listOf(DungeonMechanic.ENEMY_DOOR_CHARGE), level.mechanics)
    }

    @Test
    fun `extra moves enable their mechanic`() {
        val level = base().copy(extraMoves = 2)
        assertEquals(listOf(DungeonMechanic.EXTRA_MOVES), level.mechanics)
    }

    @Test
    fun `mechanics follow declaration order regardless of modifiers order`() {
        val level = base().copy(
            extraMoves = 2,
            blocked = setOf(Pos(1, 1)),
            jammed = setOf(DoorId.horizontal(0, 0)),
        )
        assertEquals(
            listOf(
                DungeonMechanic.JAMMED,
                DungeonMechanic.BLOCKED,
                DungeonMechanic.EXTRA_MOVES,
            ),
            level.mechanics,
        )
    }

    private fun base(): DungeonLevel = DungeonLevels.byId(1).copy(id = 101)
}
