package com.joy.sandbox.feature.dungeon.impl.presentation.game

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector2D
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import com.joy.sandbox.core.dungeon.model.DoorId
import com.joy.sandbox.feature.dungeon.impl.presentation.model.DungeonGameUiModel
import com.joy.sandbox.feature.dungeon.impl.presentation.theme.DungeonColors

/** Улица над выходом: светлая клетка-строка, куда выходят герой и пленники. */
internal fun DrawScope.drawStreet(
    model: DungeonGameUiModel,
    cellWidth: Float,
    cellHeight: Float,
    textMeasurer: TextMeasurer,
    streetLabel: String,
) {
    val topLeft = Offset(model.exitRoom.x * cellWidth, 0f)
    drawRect(
        color = DungeonColors.StreetBg,
        topLeft = topLeft,
        size = Size(cellWidth, cellHeight),
    )
    drawRect(
        color = DungeonColors.StreetEdge,
        topLeft = topLeft,
        size = Size(cellWidth, cellHeight),
        style = Stroke(width = WALL_STROKE),
    )
    // лучи света над «дырой» выхода
    drawRect(
        color = DungeonColors.ExitLight,
        topLeft = topLeft + Offset(cellWidth * EXIT_LIGHT_INSET_X, cellHeight * STREET_LIGHT_TOP),
        size = Size(cellWidth * (1f - EXIT_LIGHT_INSET_X * 2), cellHeight * STREET_LIGHT_HEIGHT),
    )
    val labelLayout = textMeasurer.measure(
        text = streetLabel,
        style = TextStyle(
            color = DungeonColors.StreetText,
            fontSize = STREET_LABEL_SP,
            fontWeight = FontWeight.Bold,
        ),
    )
    drawText(
        textLayoutResult = labelLayout,
        topLeft = topLeft +
            Offset(
                (cellWidth - labelLayout.size.width) / 2f,
                cellHeight * EXIT_LABEL_TOP,
            ),
    )
}

internal fun DrawScope.drawThreats(
    model: DungeonGameUiModel,
    cellWidth: Float,
    cellHeight: Float,
) {
    model.threatRooms.forEach { room ->
        val topLeft = Offset(room.x * cellWidth, (room.y + 1) * cellHeight)
        drawRect(
            color = DungeonColors.Threat,
            topLeft = topLeft,
            size = Size(cellWidth, cellHeight),
        )
        if (!model.heroOnStreet && room == model.player) {
            drawRect(
                color = DungeonColors.ThreatFrame,
                topLeft = topLeft,
                size = Size(cellWidth, cellHeight),
                style = Stroke(width = THREAT_FRAME_STROKE),
            )
        }
    }
}

internal fun DrawScope.drawRooms(
    model: DungeonGameUiModel,
    cellWidth: Float,
    cellHeight: Float,
) {
    val masonryInset = minOf(cellWidth, cellHeight) * MASONRY_INSET_FACTOR
    val wallStroke = maxOf(WALL_STROKE, minOf(cellWidth, cellHeight) * WALL_STROKE_FACTOR)
    for (y in 0 until model.height) {
        for (x in 0 until model.width) {
            val topLeft = Offset(x * cellWidth, (y + 1) * cellHeight)
            val cellSize = Size(cellWidth, cellHeight)
            drawRect(
                color = if ((x + y) % 2 == 0) DungeonColors.CellEven else DungeonColors.CellOdd,
                topLeft = topLeft,
                size = cellSize,
            )
            // каменная кладка
            drawRect(
                color = DungeonColors.Masonry,
                topLeft = topLeft + Offset(masonryInset, masonryInset),
                size = Size(
                    cellWidth - masonryInset * 2,
                    cellHeight - masonryInset * 2,
                ),
                style = Stroke(width = MASONRY_STROKE),
            )
            // стены
            drawRect(
                color = DungeonColors.Wall,
                topLeft = topLeft + Offset(wallStroke / 2f, wallStroke / 2f),
                size = Size(cellWidth - wallStroke, cellHeight - wallStroke),
                style = Stroke(width = wallStroke),
            )
        }
    }
}

internal fun DrawScope.drawExit(
    model: DungeonGameUiModel,
    cellWidth: Float,
    cellHeight: Float,
    textMeasurer: TextMeasurer,
    exitLabel: String,
) {
    val topLeft = Offset(model.exitRoom.x * cellWidth, (model.exitRoom.y + 1) * cellHeight)
    // свет сверху
    drawRect(
        color = DungeonColors.ExitLight,
        topLeft = topLeft + Offset(cellWidth * EXIT_LIGHT_INSET_X, 0f),
        size = Size(cellWidth * (1f - EXIT_LIGHT_INSET_X * 2), cellHeight),
    )
    // ступени
    val stepsLeft = topLeft.x + cellWidth * EXIT_STEPS_INSET_X
    val stepsWidth = cellWidth * (1f - EXIT_STEPS_INSET_X * 2)
    for (i in 1..EXIT_STEPS_COUNT) {
        val yy = topLeft.y + cellHeight * (EXIT_STEPS_START + i * EXIT_STEPS_GAP)
        drawLine(
            color = DungeonColors.ExitSteps,
            start = Offset(stepsLeft, yy),
            end = Offset(stepsLeft + stepsWidth, yy),
            strokeWidth = EXIT_STEPS_STROKE,
        )
    }
    val labelLayout = textMeasurer.measure(
        text = exitLabel,
        style = TextStyle(
            color = DungeonColors.ExitSteps,
            fontSize = EXIT_LABEL_SP,
            fontWeight = FontWeight.Bold,
        ),
    )
    drawText(
        textLayoutResult = labelLayout,
        topLeft = topLeft +
            Offset(
                (cellWidth - labelLayout.size.width) / 2f,
                cellHeight * EXIT_LABEL_TOP,
            ),
    )
}

internal fun DrawScope.drawDoors(
    model: DungeonGameUiModel,
    cellWidth: Float,
    cellHeight: Float,
) {
    val gapStroke = maxOf(DOOR_GAP_STROKE, minOf(cellWidth, cellHeight) * DOOR_GAP_FACTOR)
    val doorStroke = maxOf(DOOR_CLOSED_STROKE, minOf(cellWidth, cellHeight) * DOOR_CLOSED_FACTOR)
    val barStroke = maxOf(DOOR_BAR_STROKE, minOf(cellWidth, cellHeight) * DOOR_BAR_FACTOR)
    allDoorIds(model.width, model.height).forEach { id ->
        val door = DoorId.parse(id) ?: return@forEach
        val open = id in model.openDoors
        val center = doorCenter(door, cellWidth, cellHeight)
        val halfPad = minOf(cellWidth, cellHeight) * DOOR_VISUAL_PADDING
        val start: Offset
        val end: Offset
        if (door.horizontal) {
            start = center.copy(y = center.y - halfPad)
            end = center.copy(y = center.y + halfPad)
        } else {
            start = center.copy(x = center.x - halfPad)
            end = center.copy(x = center.x + halfPad)
        }
        // проём: стираем кусок стены
        drawLine(
            color = DungeonColors.DoorGap,
            start = start,
            end = end,
            strokeWidth = gapStroke,
        )
        if (open) {
            drawLine(
                color = DungeonColors.DoorOpen,
                start = start,
                end = end,
                strokeWidth = DOOR_OPEN_STROKE,
            )
        } else {
            // деревянные створки
            drawLine(
                color = DungeonColors.DoorClosed,
                start = start,
                end = end,
                strokeWidth = doorStroke,
            )
            // перекладина
            val barHalf = minOf(cellWidth, cellHeight) * DOOR_BAR_HALF_FACTOR
            val barStart: Offset
            val barEnd: Offset
            if (door.horizontal) {
                barStart = center.copy(y = center.y - barHalf)
                barEnd = center.copy(y = center.y + barHalf)
            } else {
                barStart = center.copy(x = center.x - barHalf)
                barEnd = center.copy(x = center.x + barHalf)
            }
            drawLine(
                color = DungeonColors.DoorBar,
                start = barStart,
                end = barEnd,
                strokeWidth = barStroke,
            )
        }
    }
}

internal fun DrawScope.drawChainsToHostages(
    model: DungeonGameUiModel,
    cellWidth: Float,
    cellHeight: Float,
) {
    val cellSize = minOf(cellWidth, cellHeight)
    model.hostages.filter { !it.freed && !it.exited }.forEach { hostage ->
        val center = hostage.room.toCenter().toPixels(cellWidth, cellHeight)
        drawLine(
            color = DungeonColors.ChainToHostage,
            start = center + Offset(-cellSize * CHAIN_OUTER_X, -cellSize * CHAIN_OUTER_Y),
            end = center + Offset(-cellSize * CHAIN_INNER, -cellSize * CHAIN_INNER),
            strokeWidth = CHAIN_STROKE,
            pathEffect = ChainPathEffect,
        )
        drawLine(
            color = DungeonColors.ChainToHostage,
            start = center + Offset(cellSize * CHAIN_OUTER_X, -cellSize * CHAIN_OUTER_Y),
            end = center + Offset(cellSize * CHAIN_INNER, -cellSize * CHAIN_INNER),
            strokeWidth = CHAIN_STROKE,
            pathEffect = ChainPathEffect,
        )
    }
}

internal fun DrawScope.drawEntities(
    model: DungeonGameUiModel,
    positions: Map<String, Animatable<Offset, AnimationVector2D>>,
    cellWidth: Float,
    cellHeight: Float,
    textMeasurer: TextMeasurer,
    enemyLabel: String,
) {
    val cellSize = minOf(cellWidth, cellHeight)
    val radius = cellSize * ENTITY_RADIUS_FACTOR
    val activeHostageId = model.characters.firstOrNull { it.active }?.id

    // вышедшие пленники — маленькие фигурки на улице
    drawStreetCrowd(model, cellWidth, cellHeight, radius)

    model.hostages.filter { !it.exited }.forEach { hostage ->
        val center = positions[hostageKey(hostage.id)]?.value?.toPixels(cellWidth, cellHeight)
            ?: hostage.room.toCenter().toPixels(cellWidth, cellHeight)
        if (hostage.id == activeHostageId) {
            drawActiveRing(center, cellSize)
        }
        drawHostage(center, hostage.id, hostage.freed, radius)
    }
    val playerCenter = (positions[KEY_PLAYER]?.value
        ?: if (model.heroOnStreet) streetCenter(model) else model.player.toCenter()
    ).toPixels(cellWidth, cellHeight)
    if (model.activeIsHero) {
        drawActiveRing(playerCenter, cellSize)
    }
    drawPlayer(
        center = playerCenter,
        radius = radius,
        cellSize = cellSize,
    )
    drawEnemy(
        center = (positions[KEY_ENEMY]?.value ?: model.enemy.toCenter()).toPixels(cellWidth, cellHeight),
        radius = radius,
        cellSize = cellSize,
        trapped = model.enemyTrapped,
        textMeasurer = textMeasurer,
        enemyLabel = enemyLabel,
    )
}

/** Вышедшие на улицу пленники — ряд маленьких голов в клетке улицы. */
private fun DrawScope.drawStreetCrowd(
    model: DungeonGameUiModel,
    cellWidth: Float,
    cellHeight: Float,
    radius: Float,
) {
    val exited = model.hostages.filter { it.exited }
    if (exited.isEmpty()) return
    val center = streetCenter(model).toPixels(cellWidth, cellHeight)
    val spread = cellWidth * STREET_CROWD_GAP
    val startX = center.x - spread * (exited.size - 1) / 2f
    exited.forEachIndexed { index, hostage ->
        val headRadius = radius * HEAD_RADIUS_FACTOR * STREET_CROWD_SCALE
        drawCircle(
            color = if (hostage.id == 0) DungeonColors.HostageElder else DungeonColors.Hostage,
            radius = headRadius,
            center = Offset(startX + index * spread, center.y),
        )
    }
}

/** Золотое кольцо вокруг активного персонажа. */
private fun DrawScope.drawActiveRing(center: Offset, cellSize: Float) {
    drawCircle(
        color = DungeonColors.GoldBright,
        radius = cellSize * ACTIVE_RING_RADIUS,
        center = center,
        style = Stroke(width = ACTIVE_RING_STROKE),
    )
}

private const val STREET_CROWD_GAP = 0.3f
private const val STREET_CROWD_SCALE = 0.6f
private const val STREET_LIGHT_TOP = 0.55f
private const val STREET_LIGHT_HEIGHT = 0.45f
