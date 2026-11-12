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
 * Игровая доска «Подземелья»: сетка комнат, двери, сущности и улица над
 * выходом (нулевая строка доски). Тап по комнате — ход активного персонажа,
 * тап по двери — переключение героем, тап по персонажу — сделать его
 * активным, тап по улице — выйти/войти через выход. Ядро отвергает
 * недопустимые действия. Позиции сущностей анимируются между клетками.
 */
@Composable
fun DungeonBoard(
    model: DungeonGameUiModel,
    exitLabel: String,
    streetLabel: String,
    enemyLabel: String,
    onRoomClick: (PosUi) -> Unit,
    onDoorClick: (String) -> Unit,
    onCharacterClick: (Int?) -> Unit,
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
                        onCharacterClick = onCharacterClick,
                    )
                }
            },
    ) {
        // нулевая строка — улица над выходом, подземелье сдвинуто на одну клетку вниз
        val cellWidth = size.width / model.width
        val cellHeight = size.height / boardRows(model)

        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(DungeonColors.BoardBgTop, DungeonColors.BoardBgBottom),
            ),
        )
        drawStreet(model, cellWidth, cellHeight, textMeasurer, streetLabel)
        drawRooms(model, cellWidth, cellHeight)
        drawThreats(model, cellWidth, cellHeight)
        drawDoors(model, cellWidth, cellHeight)
        drawExit(model, cellWidth, cellHeight, textMeasurer, exitLabel)
        drawChainsToHostages(model, cellWidth, cellHeight)
        drawEntities(model, positions, cellWidth, cellHeight, textMeasurer, enemyLabel)
    }
}

internal fun boardRows(model: DungeonGameUiModel): Int = model.height + 1

/** Центр клетки в «клеточных» координатах доски: подземелье сдвинуто на строку вниз. */
internal fun PosUi.toCenter(): Offset = Offset(x + HALF_CELL, y + 1 + HALF_CELL)

/** Центр клетки улицы над выходом. */
internal fun streetCenter(model: DungeonGameUiModel): Offset =
    Offset(model.exitRoom.x + HALF_CELL, HALF_CELL)

private suspend fun animateEntities(
    model: DungeonGameUiModel,
    positions: MutableMap<String, Animatable<Offset, AnimationVector2D>>,
) {
    val targets = buildMap {
        put(KEY_PLAYER, if (model.heroOnStreet) streetCenter(model) else model.player.toCenter())
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

private fun handleTap(
    offset: Offset,
    size: IntSize,
    model: DungeonGameUiModel,
    onRoomClick: (PosUi) -> Unit,
    onDoorClick: (String) -> Unit,
    onCharacterClick: (Int?) -> Unit,
) {
    if (size.width == 0 || size.height == 0) return
    val cellWidth = size.width.toFloat() / model.width
    val cellHeight = size.height.toFloat() / boardRows(model)
    val threshold = minOf(cellWidth, cellHeight) * DOOR_TAP_FACTOR

    val tappedDoor = allDoorIds(model.width, model.height).firstOrNull { id ->
        val door = DoorId.parse(id) ?: return@firstOrNull false
        (offset - doorCenter(door, cellWidth, cellHeight)).getDistance() <= threshold
    }
    // тап по персонажу делает его активным (герой рисуется поверх — приоритет при совпадении)
    val tappedCharacter = if (tappedDoor == null) characterAt(offset, model, cellWidth, cellHeight) else null
    when {
        tappedDoor != null -> onDoorClick(tappedDoor)
        tappedCharacter != null -> onCharacterClick(tappedCharacter)
        else -> dispatchRoomTap(offset, model, cellWidth, cellHeight, onRoomClick)
    }
}

/** Тап по клетке: строка 0 — улица над выходом, остальное — комнаты подземелья. */
private fun dispatchRoomTap(
    offset: Offset,
    model: DungeonGameUiModel,
    cellWidth: Float,
    cellHeight: Float,
    onRoomClick: (PosUi) -> Unit,
) {
    val column = (offset.x / cellWidth).toInt()
    val row = (offset.y / cellHeight).toInt()
    if (row == STREET_ROW) {
        // улица — только над выходом; тап по ней = переход героя через выход
        if (column == model.exitRoom.x) onRoomClick(PosUi(model.exitRoom.x, STREET_ROOM_Y))
    } else {
        val room = PosUi(column, row - 1)
        if (room.x in 0 until model.width && room.y in 0 until model.height) {
            onRoomClick(room)
        }
    }
}

/** Ближайший к точке касания управляемый персонаж внутри подземелья. */
private fun characterAt(
    offset: Offset,
    model: DungeonGameUiModel,
    cellWidth: Float,
    cellHeight: Float,
): Int? {
    val radius = minOf(cellWidth, cellHeight) * CHARACTER_TAP_FACTOR
    val candidates = buildList {
        if (!model.heroOnStreet) {
            add(null to model.player.toCenter().toPixels(cellWidth, cellHeight))
        }
        model.hostages
            .filter { it.freed && !it.exited }
            .forEach { add(it.id to it.room.toCenter().toPixels(cellWidth, cellHeight)) }
    }
    val nearest = candidates.minByOrNull { (_, center) -> (offset - center).getDistance() }
        ?: return null
    return if ((offset - nearest.second).getDistance() <= radius) nearest.first else null
}

internal fun Offset.toPixels(cellWidth: Float, cellHeight: Float): Offset =
    Offset(x * cellWidth, y * cellHeight)

internal const val KEY_PLAYER = "player"
internal const val KEY_ENEMY = "enemy"
internal const val HALF_CELL = 0.5f
internal const val STREET_ROW = 0
internal const val STREET_ROOM_Y = -1
internal val ChainPathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(6f, 6f))

private const val DURATION_MS = 220
private const val DOOR_TAP_FACTOR = 0.22f
private const val CHARACTER_TAP_FACTOR = 0.35f
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
internal const val LANTERN_RADIUS = 0.22f
internal const val LANTERN_OFFSET_X = 0.75f
internal const val LANTERN_OFFSET_Y = 0.25f
internal const val ENEMY_TOP = 1.5f
internal const val ENEMY_SIDE = 1.15f
internal const val ENEMY_SHOULDER = 0.7f
internal const val ENEMY_WAIST = 0.8f
internal const val ENEMY_BOTTOM = 1.1f
internal const val EYES_OFFSET = 0.75f
internal const val EYES_GAP = 0.28f
internal const val EYE_RADIUS = 0.11f
internal const val TRAPPED_LABEL_OFFSET = 1.6f
internal const val ACTIVE_RING_STROKE = 3f
internal const val ACTIVE_RING_RADIUS = 0.38f
internal val EXIT_LABEL_SP = 11.sp
internal val STREET_LABEL_SP = 10.sp
internal val TRAPPED_LABEL_SP = 12.sp
