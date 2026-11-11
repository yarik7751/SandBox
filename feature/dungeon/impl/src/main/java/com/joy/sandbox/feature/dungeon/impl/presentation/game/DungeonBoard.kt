package com.joy.sandbox.feature.dungeon.impl.presentation.game

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector2D
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.sp
import com.joy.sandbox.core.dungeon.model.DoorId
import com.joy.sandbox.feature.dungeon.impl.presentation.model.DungeonGameUiModel
import com.joy.sandbox.feature.dungeon.impl.presentation.model.PosUi
import com.joy.sandbox.feature.dungeon.impl.presentation.theme.DungeonColors

/**
 * Игровая доска «Подземелья»: сетка комнат, двери, сущности.
 * Тап по комнате — ход, тап по двери — переключение (ядро отвергает
 * недопустимые действия). Позиции сущностей анимируются между клетками.
 */
@Composable
fun DungeonBoard(
    model: DungeonGameUiModel,
    exitLabel: String,
    enemyLabel: String,
    onRoomClick: (PosUi) -> Unit,
    onDoorClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    val positions = remember { mutableStateMapOf<String, Animatable<Offset, AnimationVector2D>>() }
    val textMeasurer = rememberTextMeasurer()

    LaunchedEffect(model.moveSeq) {
        animateEntities(model, positions)
    }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { canvasSize = it }
            .pointerInput(model.width, model.height) {
                detectTapGestures { offset ->
                    handleTap(
                        offset = offset,
                        size = canvasSize,
                        model = model,
                        onRoomClick = onRoomClick,
                        onDoorClick = onDoorClick,
                    )
                }
            },
    ) {
        val cellWidth = size.width / model.width
        val cellHeight = size.height / model.height

        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(DungeonColors.BoardBgTop, DungeonColors.BoardBgBottom),
            ),
        )
        drawRooms(model, cellWidth, cellHeight)
        drawThreats(model, cellWidth, cellHeight)
        drawDoors(model, cellWidth, cellHeight)
        drawExit(model, cellWidth, cellHeight, textMeasurer, exitLabel)
        drawChainsToHostages(model, cellWidth, cellHeight)
        drawEntities(model, positions, cellWidth, cellHeight, textMeasurer, enemyLabel)
    }
}

private suspend fun animateEntities(
    model: DungeonGameUiModel,
    positions: MutableMap<String, Animatable<Offset, AnimationVector2D>>,
) {
    val targets = buildMap {
        put(KEY_PLAYER, model.player.toCenter())
        put(KEY_ENEMY, model.enemy.toCenter())
        model.hostages.filter { !it.exited }.forEach { put(hostageKey(it.id), it.room.toCenter()) }
    }
    targets.forEach { (key, target) ->
        val animatable = positions.getOrPut(key) { Animatable(target, Offset.VectorConverter) }
        if (animatable.value != target) {
            animatable.animateTo(target, tween(DURATION_MS, easing = FastOutSlowInEasing))
        }
    }
}

internal fun hostageKey(id: Int): String = "hostage:$id"

internal fun PosUi.toCenter(): Offset = Offset(x + HALF_CELL, y + HALF_CELL)

private fun handleTap(
    offset: Offset,
    size: IntSize,
    model: DungeonGameUiModel,
    onRoomClick: (PosUi) -> Unit,
    onDoorClick: (String) -> Unit,
) {
    if (size.width == 0 || size.height == 0) return
    val cellWidth = size.width.toFloat() / model.width
    val cellHeight = size.height.toFloat() / model.height
    val threshold = minOf(cellWidth, cellHeight) * DOOR_TAP_FACTOR

    allDoorIds(model.width, model.height).forEach { id ->
        val door = DoorId.parse(id) ?: return@forEach
        val center = doorCenter(door, cellWidth, cellHeight)
        if ((offset - center).getDistance() <= threshold) {
            onDoorClick(id)
            return
        }
    }
    val room = PosUi((offset.x / cellWidth).toInt(), (offset.y / cellHeight).toInt())
    if (room.x in 0 until model.width && room.y in 0 until model.height) {
        onRoomClick(room)
    }
}

internal fun doorCenter(door: DoorId, cellWidth: Float, cellHeight: Float): Offset =
    if (door.horizontal) {
        Offset((door.x + 1) * cellWidth, (door.y + HALF_CELL) * cellHeight)
    } else {
        Offset((door.x + HALF_CELL) * cellWidth, (door.y + 1) * cellHeight)
    }

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

internal const val KEY_PLAYER = "player"
internal const val KEY_ENEMY = "enemy"
internal const val HALF_CELL = 0.5f
internal val ChainPathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))
internal val FollowChainPathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))

private const val DURATION_MS = 220
private const val DOOR_TAP_FACTOR = 0.22f
internal const val DOOR_VISUAL_PADDING = 0.35f
internal const val DOOR_OPEN_STROKE = 6f
internal const val DOOR_GAP_STROKE = 5f
internal const val DOOR_GAP_FACTOR = 0.09f
internal const val DOOR_CLOSED_STROKE = 5f
internal const val DOOR_CLOSED_FACTOR = 0.075f
internal const val DOOR_BAR_STROKE = 3f
internal const val DOOR_BAR_FACTOR = 0.05f
internal const val DOOR_BAR_HALF_FACTOR = 0.13f
internal const val WALL_STROKE = 4f
internal const val WALL_STROKE_FACTOR = 0.055f
internal const val MASONRY_STROKE = 1f
internal const val MASONRY_INSET_FACTOR = 0.18f
internal const val THREAT_FRAME_STROKE = 3f
internal const val EXIT_LIGHT_INSET_X = 0.1f
internal const val EXIT_STEPS_INSET_X = 0.3f
internal const val EXIT_STEPS_START = 0.15f
internal const val EXIT_STEPS_GAP = 0.18f
internal const val EXIT_STEPS_COUNT = 3
internal const val EXIT_STEPS_STROKE = 2f
internal const val EXIT_LABEL_TOP = 0.12f
internal const val CHAIN_STROKE = 2f
internal const val CHAIN_OUTER_X = 0.3f
internal const val CHAIN_OUTER_Y = 0.35f
internal const val CHAIN_INNER = 0.08f
internal const val CHAIN_ARC_START = 10f
internal const val CHAIN_ARC_SWEEP = 160f
internal const val CHAIN_ARC_TOP = 0.25f
internal const val FOLLOW_CHAIN_STROKE = 2f
internal const val ENTITY_RADIUS_FACTOR = 0.24f
internal const val HOSTAGE_ELDER_RADIUS_FACTOR = 1.0f
internal const val HOSTAGE_RADIUS_FACTOR = 0.8f
internal const val HEAD_RADIUS_FACTOR = 0.55f
internal const val HEAD_OFFSET = 0.35f
internal const val BODY_TOP = 0.1f
internal const val BODY_SHOULDER = 0.3f
internal const val BODY_SIDE = 0.85f
internal const val BODY_WAIST = 0.6f
internal const val BODY_BOTTOM = 1.0f
internal const val HOOD_TOP = 1.25f
internal const val HOOD_SIDE = 1.05f
internal const val HOOD_SHOULDER = 0.9f
internal const val HOOD_RADIUS = 0.5f
internal const val HOOD_OFFSET = 0.55f
internal const val CLOAK_WAIST = 0.85f
internal const val LANTERN_LIGHT_RADIUS = 0.9f
internal const val LANTERN_RADIUS = 0.22f
internal const val LANTERN_OFFSET_X = 0.75f
internal const val LANTERN_OFFSET_Y = 0.25f
internal const val ENEMY_AURA_RADIUS = 1.0f
internal const val ENEMY_TOP = 1.5f
internal const val ENEMY_SIDE = 1.15f
internal const val ENEMY_SHOULDER = 0.7f
internal const val ENEMY_WAIST = 0.8f
internal const val ENEMY_BOTTOM = 1.1f
internal const val EYES_OFFSET = 0.75f
internal const val EYES_GAP = 0.28f
internal const val EYE_RADIUS = 0.11f
internal const val TRAPPED_LABEL_OFFSET = 1.6f
internal val EXIT_LABEL_SP = 11.sp
internal val TRAPPED_LABEL_SP = 12.sp
