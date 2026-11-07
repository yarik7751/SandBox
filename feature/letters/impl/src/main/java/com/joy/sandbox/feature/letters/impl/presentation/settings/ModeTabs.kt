package com.joy.sandbox.feature.letters.impl.presentation.settings

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableFloatState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.joy.sandbox.core.ui.DesignSystem
import com.joy.sandbox.feature.letters.impl.domain.LettersMode
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Вкладки-режимы «День» / «Ночь» с плавающим указателем.
 *
 * Белый указатель (thumb) свободно перемещается перетаскиванием
 * по серой дорожке; подпись, над которой он находится, плавно
 * затемняется («перетекает» на указатель). При отпускании указатель
 * пружинной анимацией встаёт на ближайший раздел и выбирает его.
 * Тап по разделу анимированно переносит указатель на него.
 */
@Composable
fun ModeTabs(
    selectedMode: LettersMode,
    onModeSelect: (LettersMode) -> Unit,
    thumbFraction: MutableFloatState,
) {
    val modes = LettersMode.entries
    val count = modes.size
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()

    // Цвета лерпятся по позиции указателя: плавно следуют за ползунком
    // и в drag, и в анимации (0 = «День», 1 = «Ночь»).
    val fraction = thumbFraction.floatValue
    val trackColor = lerp(DesignSystem.Colors.Letters.TabsTrackDay, DesignSystem.Colors.Letters.TabsTrackNight, fraction)
    val thumbColor = lerp(DesignSystem.Colors.Letters.TabsThumbDay, DesignSystem.Colors.Letters.TabsThumbNight, fraction)
    val labelIdleColor = lerp(
        DesignSystem.Colors.Letters.TabsLabelIdleDay,
        DesignSystem.Colors.Letters.TabsLabelIdleNight,
        fraction,
    )
    val labelActiveColor = lerp(
        DesignSystem.Colors.Letters.TabsLabelActiveDay,
        DesignSystem.Colors.Letters.TabsLabelActiveNight,
        fraction,
    )

    var trackWidthPx by remember { mutableIntStateOf(0) }
    val slotWidthPx = trackWidthPx.toFloat() / count

    // Позиция указателя (левая кромка) в покое — анимированная.
    val thumbX = remember { Animatable(0f) }
    // До инициализации (первого измерения дорожки) позиция не берётся
    // из аниматора — показываем выбранный режим напрямую (см. ниже),
    // стартовый кадр не зависит от эффектов.
    var isThumbInitialized by remember { mutableStateOf(false) }
    // Во время drag позицию держим вне аниматора — без запуска
    // корутин на каждое событие; NaN = drag не идёт.
    var dragX by remember { mutableFloatStateOf(Float.NaN) }

    // Инициализация аниматора при первом измерении дорожки.
    LaunchedEffect(trackWidthPx) {
        if (trackWidthPx > 0 && !isThumbInitialized) {
            thumbX.snapTo(selectedMode.ordinal * slotWidthPx)
            isThumbInitialized = true
        }
    }

    // Синхронизация с выбранным режимом извне (не во время drag) —
    // только после инициализации: до неё позицию задаёт selectedMode напрямую.
    LaunchedEffect(selectedMode) {
        if (isThumbInitialized && trackWidthPx > 0 && dragX.isNaN()) {
            thumbX.animateTo(
                targetValue = selectedMode.ordinal * slotWidthPx,
                animationSpec = tween(durationMillis = TAB_ANIM_MS),
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(TAB_HEIGHT)
            .clip(TRACK_SHAPE)
            .background(trackColor)
            .onSizeChanged {
                trackWidthPx = it.width
            }
            .pointerInput(count) {
                detectDragGestures(
                    onDragStart = { start ->
                        dragX = if (!isThumbInitialized) {
                            start.x - slotWidthPx / 2f
                        } else {
                            thumbX.value
                        }
                    },
                    onDragEnd = {
                        val releasedX = dragX
                        dragX = Float.NaN
                        // Ближайший раздел — по левой кромке указателя в
                        // делениях дорожки: порог — середина между центрами
                        // слотов (индекс 0 достижим при тяге влево).
                        val targetIndex = (releasedX / slotWidthPx)
                            .roundToInt()
                            .coerceIn(0, count - 1)
                        scope.launch {
                            thumbX.snapTo(releasedX)
                            thumbX.animateTo(
                                targetValue = targetIndex * slotWidthPx,
                                animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                            )
                        }
                        onModeSelect(modes[targetIndex])
                    },
                    onDragCancel = {
                        dragX = Float.NaN
                        scope.launch {
                            thumbX.animateTo(
                                targetValue = selectedMode.ordinal * slotWidthPx,
                                animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                            )
                        }
                    },
                ) { change, dragAmount ->
                    change.consume()
                    val maxX = trackWidthPx - slotWidthPx
                    dragX = (dragX + dragAmount.x).coerceIn(0f, maxX)
                }
            }
            .pointerInput(count) {
                detectTapGestures { offset ->
                    if (trackWidthPx == 0) return@detectTapGestures
                    val index = (offset.x / slotWidthPx).toInt().coerceIn(0, count - 1)
                    scope.launch {
                        if (!isThumbInitialized) {
                            thumbX.snapTo(index * slotWidthPx)
                            isThumbInitialized = true
                        }
                        thumbX.animateTo(
                            targetValue = index * slotWidthPx,
                            animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                        )
                    }
                    onModeSelect(modes[index])
                }
            },
    ) {
        if (trackWidthPx > 0) {
            // Позиция: до инициализации аниматора — сразу на выбранном
            // режиме (не зависит от эффектов); в покое — анимированная;
            // во время drag — drag-позиция.
            val targetX = selectedMode.ordinal * slotWidthPx
            val thumbPosition = when {
                !isThumbInitialized -> targetX
                dragX.isNaN() -> thumbX.value
                else -> dragX
            }
            // Доля позиции указателя между «Днем» (0) и «Ночью» (1) —
            // внешние цвета (фон, тулбар) лерпятся по ней.
            val maxX = trackWidthPx - slotWidthPx
            val newFraction = if (maxX > 0f) (thumbPosition / maxX).coerceIn(0f, 1f) else 0f
            if (thumbFraction.floatValue != newFraction) {
                thumbFraction.floatValue = newFraction
            }
            Box(
                modifier = Modifier
                    .offset { IntOffset(thumbPosition.roundToInt(), 0) }
                    .width(with(density) { slotWidthPx.toDp() })
                    .fillMaxHeight()
                    .padding(THUMB_PADDING)
                    .shadow(elevation = THUMB_ELEVATION, shape = THUMB_SHAPE)
                    .clip(THUMB_SHAPE)
                    .background(thumbColor),
            )
        }
        Row(modifier = Modifier.fillMaxSize()) {
            modes.forEachIndexed { index, mode ->
                // Доля перекрытия центра подписи указателем — от неё
                // плавно лерпится цвет текста (перетекание на указатель).
                val slotCenter = (index + 0.5f) * slotWidthPx
                val thumbCenter = thumbPositionForLabel(
                    thumbX = thumbX.value,
                    dragX = dragX,
                    slotWidthPx = slotWidthPx,
                )
                val coverage = (1f - abs(slotCenter - thumbCenter) / slotWidthPx)
                    .coerceIn(0f, 1f)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(mode.labelRes),
                        color = lerp(labelIdleColor, labelActiveColor, coverage),
                    )
                }
            }
        }
    }
}

/** Центр указателя: во время drag — drag-позиция, в покое — анимированная. */
private fun thumbPositionForLabel(thumbX: Float, dragX: Float, slotWidthPx: Float): Float =
    (if (dragX.isNaN()) thumbX else dragX) + slotWidthPx / 2f

private val TAB_HEIGHT = 40.dp
private val THUMB_PADDING = 4.dp
private val THUMB_ELEVATION = 2.dp

private val TRACK_SHAPE = RoundedCornerShape(percent = 50)
private val THUMB_SHAPE = RoundedCornerShape(percent = 50)

private const val TAB_ANIM_MS = 250
