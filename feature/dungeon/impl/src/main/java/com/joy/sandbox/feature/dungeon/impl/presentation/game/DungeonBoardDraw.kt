package com.joy.sandbox.feature.dungeon.impl.presentation.game

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector2D
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import com.joy.sandbox.core.dungeon.model.DoorId
import com.joy.sandbox.feature.dungeon.impl.presentation.model.DungeonGameUiModel
import com.joy.sandbox.feature.dungeon.impl.presentation.model.PosUi

internal fun DrawScope.drawThreats(
    model: DungeonGameUiModel,
    cellWidth: Float,
    cellHeight: Float,
) {
    model.threatRooms.forEach { room ->
        drawRect(
            color = ThreatColor,
            topLeft = Offset(room.x * cellWidth, room.y * cellHeight),
            size = Size(cellWidth, cellHeight),
        )
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
    drawRect(
        color = ExitColor,
        topLeft = topLeft,
        size = Size(cellWidth, cellHeight),
        style = Stroke(width = EXIT_STROKE),
    )
    val labelLayout = textMeasurer.measure(
        text = exitLabel,
        style = TextStyle(color = ExitColor, fontSize = EXIT_LABEL_SP, fontWeight = FontWeight.Bold),
    )
    drawText(
        textLayoutResult = labelLayout,
        topLeft = topLeft + Offset(EXIT_LABEL_PADDING, EXIT_LABEL_PADDING),
    )
}

internal fun DrawScope.drawGrid(
    model: DungeonGameUiModel,
    cellWidth: Float,
    cellHeight: Float,
) {
    for (x in 0..model.width) {
        drawLine(
            color = WallColor,
            start = Offset(x * cellWidth, 0f),
            end = Offset(x * cellWidth, size.height),
            strokeWidth = WALL_STROKE,
        )
    }
    for (y in 0..model.height) {
        drawLine(
            color = WallColor,
            start = Offset(0f, y * cellHeight),
            end = Offset(size.width, y * cellHeight),
            strokeWidth = WALL_STROKE,
        )
    }
}

internal fun DrawScope.drawDoors(
    model: DungeonGameUiModel,
    cellWidth: Float,
    cellHeight: Float,
) {
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
        drawLine(
            color = if (open) DoorOpenColor else DoorClosedColor,
            start = start,
            end = end,
            strokeWidth = if (open) DOOR_OPEN_STROKE else DOOR_CLOSED_STROKE,
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
    val radius = minOf(cellWidth, cellHeight) * ENTITY_RADIUS_FACTOR
    model.hostages.filter { !it.exited }.forEach { hostage ->
        val center = positions[hostageKey(hostage.id)]?.value?.toPixels(cellWidth, cellHeight)
            ?: hostage.room.toCenter().toPixels(cellWidth, cellHeight)
        drawCircle(color = HostageColor, radius = radius * HOSTAGE_RADIUS_FACTOR, center = center)
        val label = textMeasurer.measure(
            text = (hostage.id + 1).toString(),
            style = TextStyle(color = Color.Black, fontSize = ENTITY_LABEL_SP, fontWeight = FontWeight.Bold),
        )
        drawText(
            textLayoutResult = label,
            topLeft = center - Offset(label.size.width / 2f, label.size.height / 2f),
        )
    }
    drawEntity(positions[KEY_PLAYER], model.player, cellWidth, cellHeight, radius, PlayerColor, textMeasurer, null)
    drawEntity(
        positions[KEY_ENEMY],
        model.enemy,
        cellWidth,
        cellHeight,
        radius,
        if (model.enemyTrapped) EnemyTrappedColor else EnemyColor,
        textMeasurer,
        enemyLabel,
    )
}

private fun DrawScope.drawEntity(
    animatable: Animatable<Offset, AnimationVector2D>?,
    fallback: PosUi,
    cellWidth: Float,
    cellHeight: Float,
    radius: Float,
    color: Color,
    textMeasurer: TextMeasurer,
    label: String?,
) {
    val center = (animatable?.value ?: fallback.toCenter()).toPixels(cellWidth, cellHeight)
    drawCircle(color = color, radius = radius, center = center)
    if (label != null) {
        val layout = textMeasurer.measure(
            text = label,
            style = TextStyle(color = EntityTextColor, fontSize = ENTITY_LABEL_SP, fontWeight = FontWeight.Bold),
        )
        drawText(
            textLayoutResult = layout,
            topLeft = center - Offset(layout.size.width / 2f, layout.size.height / 2f),
        )
    }
}

private fun Offset.toPixels(cellWidth: Float, cellHeight: Float): Offset =
    Offset(x * cellWidth, y * cellHeight)
