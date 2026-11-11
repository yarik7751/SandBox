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

internal fun DrawScope.drawThreats(
    model: DungeonGameUiModel,
    cellWidth: Float,
    cellHeight: Float,
) {
    model.threatRooms.forEach { room ->
        val topLeft = Offset(room.x * cellWidth, room.y * cellHeight)
        drawRect(
            color = DungeonColors.Threat,
            topLeft = topLeft,
            size = Size(cellWidth, cellHeight),
        )
        if (room == model.player) {
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
            val topLeft = Offset(x * cellWidth, y * cellHeight)
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
    val topLeft = Offset(model.exitRoom.x * cellWidth, model.exitRoom.y * cellHeight)
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

    drawFollowChain(model, positions, cellWidth, cellHeight)

    model.hostages.filter { !it.exited }.forEach { hostage ->
        val center = positions[hostageKey(hostage.id)]?.value?.toPixels(cellWidth, cellHeight)
            ?: hostage.room.toCenter().toPixels(cellWidth, cellHeight)
        drawHostage(center, hostage.id, hostage.freed, radius)
    }
    drawPlayer(
        center = (positions[KEY_PLAYER]?.value ?: model.player.toCenter()).toPixels(cellWidth, cellHeight),
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

/** Тонкая пунктирная цепочка между игроком и освобождёнными пленниками. */
private fun DrawScope.drawFollowChain(
    model: DungeonGameUiModel,
    positions: Map<String, Animatable<Offset, AnimationVector2D>>,
    cellWidth: Float,
    cellHeight: Float,
) {
    val chain = buildList {
        add((positions[KEY_PLAYER]?.value ?: model.player.toCenter()).toPixels(cellWidth, cellHeight))
        model.hostages
            .filter { it.freed && !it.exited }
            .sortedBy { it.id }
            .forEach { hostage ->
                add(
                    positions[hostageKey(hostage.id)]?.value?.toPixels(cellWidth, cellHeight)
                        ?: hostage.room.toCenter().toPixels(cellWidth, cellHeight),
                )
            }
    }
    if (chain.size < 2) return
    for (i in 0 until chain.size - 1) {
        drawLine(
            color = DungeonColors.FollowChain,
            start = chain[i],
            end = chain[i + 1],
            strokeWidth = FOLLOW_CHAIN_STROKE,
            pathEffect = FollowChainPathEffect,
        )
    }
}

/** Пленник: старший (id == 0) крупнее и серее, остальные — сиреневые. */
private fun DrawScope.drawHostage(
    center: Offset,
    id: Int,
    freed: Boolean,
    entityRadius: Float,
) {
    val radius = entityRadius * if (id == 0) HOSTAGE_ELDER_RADIUS_FACTOR else HOSTAGE_RADIUS_FACTOR
    val color = if (id == 0) DungeonColors.HostageElder else DungeonColors.Hostage
    drawCircle(
        color = color,
        radius = radius * HEAD_RADIUS_FACTOR,
        center = center.copy(y = center.y - radius * HEAD_OFFSET),
    )
    val body = Path().apply {
        val top = center.y - radius * BODY_TOP
        val bottom = center.y + radius * BODY_BOTTOM
        moveTo(center.x, top)
        quadraticTo(
            center.x + radius * BODY_SIDE, center.y + radius * BODY_SHOULDER,
            center.x + radius * BODY_WAIST, bottom,
        )
        lineTo(center.x - radius * BODY_WAIST, bottom)
        quadraticTo(
            center.x - radius * BODY_SIDE, center.y + radius * BODY_SHOULDER,
            center.x, top,
        )
        close()
    }
    drawPath(path = body, color = color)
    if (!freed) {
        drawArc(
            color = DungeonColors.HostageChain,
            startAngle = CHAIN_ARC_START,
            sweepAngle = CHAIN_ARC_SWEEP,
            useCenter = false,
            topLeft = center.copy(y = center.y + radius * CHAIN_ARC_TOP) - Offset(radius, radius),
            size = Size(radius * 2, radius * 2),
            style = Stroke(width = CHAIN_STROKE),
        )
    }
}

/** Игрок: фигура в капюшоне с фонарём и тёплым светом. */
private fun DrawScope.drawPlayer(
    center: Offset,
    radius: Float,
    cellSize: Float,
) {
    // свет фонаря
    drawCircle(
        color = DungeonColors.LanternLight,
        radius = cellSize * LANTERN_LIGHT_RADIUS,
        center = center,
    )
    val cloak = Path().apply {
        val top = center.y - radius * HOOD_TOP
        val bottom = center.y + radius * BODY_BOTTOM
        moveTo(center.x, top)
        quadraticTo(
            center.x + radius * HOOD_SIDE, center.y - radius * HOOD_SHOULDER,
            center.x + radius * CLOAK_WAIST, bottom,
        )
        lineTo(center.x - radius * CLOAK_WAIST, bottom)
        quadraticTo(
            center.x - radius * HOOD_SIDE, center.y - radius * HOOD_SHOULDER,
            center.x, top,
        )
        close()
    }
    drawPath(path = cloak, color = DungeonColors.PlayerCloak)
    drawCircle(
        color = DungeonColors.PlayerHood,
        radius = radius * HOOD_RADIUS,
        center = center.copy(y = center.y - radius * HOOD_OFFSET),
    )
    drawCircle(
        color = DungeonColors.GoldBright,
        radius = radius * LANTERN_RADIUS,
        center = center + Offset(radius * LANTERN_OFFSET_X, radius * LANTERN_OFFSET_Y),
    )
}

/** Враг: тёмный силуэт с красными глазами и аурой тьмы. */
private fun DrawScope.drawEnemy(
    center: Offset,
    radius: Float,
    cellSize: Float,
    trapped: Boolean,
    textMeasurer: TextMeasurer,
    enemyLabel: String,
) {
    // аура тьмы
    drawCircle(
        color = DungeonColors.EnemyAura,
        radius = cellSize * ENEMY_AURA_RADIUS,
        center = center,
    )
    val silhouette = Path().apply {
        val top = center.y - radius * ENEMY_TOP
        val bottom = center.y + radius * ENEMY_BOTTOM
        moveTo(center.x, top)
        quadraticTo(
            center.x + radius * ENEMY_SIDE, center.y - radius * ENEMY_SHOULDER,
            center.x + radius * ENEMY_WAIST, bottom,
        )
        lineTo(center.x - radius * ENEMY_WAIST, bottom)
        quadraticTo(
            center.x - radius * ENEMY_SIDE, center.y - radius * ENEMY_SHOULDER,
            center.x, top,
        )
        close()
    }
    drawPath(path = silhouette, color = DungeonColors.EnemyBody)
    val eyesY = center.y - radius * EYES_OFFSET
    drawCircle(
        color = DungeonColors.RedBright,
        radius = radius * EYE_RADIUS,
        center = Offset(center.x - radius * EYES_GAP, eyesY),
    )
    drawCircle(
        color = DungeonColors.RedBright,
        radius = radius * EYE_RADIUS,
        center = Offset(center.x + radius * EYES_GAP, eyesY),
    )
    if (trapped) {
        val layout = textMeasurer.measure(
            text = enemyLabel,
            style = TextStyle(
                color = DungeonColors.EnemyTrapped,
                fontSize = TRAPPED_LABEL_SP,
                fontWeight = FontWeight.Bold,
            ),
        )
        drawText(
            textLayoutResult = layout,
            topLeft = center - Offset(layout.size.width / 2f, radius * TRAPPED_LABEL_OFFSET),
        )
    }
}

private fun Offset.toPixels(cellWidth: Float, cellHeight: Float): Offset =
    Offset(x * cellWidth, y * cellHeight)
