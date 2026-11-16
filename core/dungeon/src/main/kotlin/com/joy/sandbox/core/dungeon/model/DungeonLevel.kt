package com.joy.sandbox.core.dungeon.model

/**
 * Описание уровня: сетка, стартовые позиции, запертые двери, пар.
 *
 * Механические модификаторы (документ docs/dungeon-generation.md):
 * - [blocked] — завалы: клетки, куда не может войти никто; должны не
 *   пересекаться с выходом и стартовыми позициями (валидация в
 *   `DungeonEngine.create`);
 * - [concealed] — укрытия: тёмные клетки, проходимы для героя и пленников,
 *   но враг не видит там персонажей (не целится) и не входит туда;
 * - [jammed] — заклинившие двери: отсутствуют на доске, переключение
 *   отклоняется;
 * - [togglesBudget] — лимит переключений дверей героем ([Int.MAX_VALUE] —
 *   без лимита); при 0 переключение отклоняется без траты хода;
 * - [enemyDoorCharge] — сколько раз враг, оказавшись в ловушке, может
 *   открыть соседнюю закрытую дверь, дающую путь к цели;
 * - [extraMoves] — запас дополнительных ходов: перемещение активного
 *   персонажа без ответа врага.
 */
data class DungeonLevel(
    val id: Int,
    val par: Int,
    val name: String,
    val w: Int,
    val h: Int,
    val player: Pos,
    val enemy: Pos,
    val hostages: List<Pos>,
    val closed: Set<DoorId>,
    val intro: String,
    val blocked: Set<Pos> = emptySet(),
    val concealed: Set<Pos> = emptySet(),
    val jammed: Set<DoorId> = emptySet(),
    val togglesBudget: Int = Int.MAX_VALUE,
    val enemyDoorCharge: Int = 0,
    val extraMoves: Int = 0,
)

/**
 * Механики уровня: базовые правила — на первом уровне игры, остальные —
 * по фактически заданным модификаторам (см. docs/dungeon-generation.md).
 */
val DungeonLevel.mechanics: List<DungeonMechanic>
    get() = buildList {
        if (id == DungeonLevels.FIRST_LEVEL_ID) add(DungeonMechanic.BASE)
        if (jammed.isNotEmpty()) add(DungeonMechanic.JAMMED)
        if (concealed.isNotEmpty()) add(DungeonMechanic.CONCEALED)
        if (blocked.isNotEmpty()) add(DungeonMechanic.BLOCKED)
        if (togglesBudget != Int.MAX_VALUE) add(DungeonMechanic.TOGGLES_BUDGET)
        if (enemyDoorCharge > 0) add(DungeonMechanic.ENEMY_DOOR_CHARGE)
        if (extraMoves > 0) add(DungeonMechanic.EXTRA_MOVES)
    }
