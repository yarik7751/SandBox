package com.joy.sandbox.feature.dungeon.impl.presentation.game

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
import com.joy.sandbox.feature.dungeon.impl.presentation.theme.DungeonColors

/** Двери в «клеточных» координатах: горизонтальная на границе строк, вертикальная — столбцов. */
internal fun doorCenter(door: DoorId, cellWidth: Float, cellHeight: Float): Offset =
    if (door.horizontal) {
        Offset((door.x + 1) * cellWidth, (door.y + 1 + HALF_CELL) * cellHeight)
    } else {
        Offset((door.x + HALF_CELL) * cellWidth, (door.y + 2) * cellHeight)
    }

/** Все id дверей сетки: горизонтальные 'h:x,y' по строкам, затем вертикальные 'v:x,y'. */
internal fun allDoorIds(width: Int, height: Int): List<String> {
    val ids = mutableListOf<String>()
    for (y in 0 until height) {
        for (x in 0 until width - 1) ids += "h:$x,$y"
    }
    for (y in 0 until height - 1) {
        for (x in 0 until width) ids += "v:$x,$y"
    }
    return ids
}

/** Пленник: старший (id == 0) крупнее и серее, остальные — сиреневые. */
internal fun DrawScope.drawHostage(
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
internal fun DrawScope.drawPlayer(
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
internal fun DrawScope.drawEnemy(
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
