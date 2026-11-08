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

    // Позиция указателя как доля дорожки: 0 = «День», 1 = «Ночь».
    // Инициализируется выбранным режимом сразу — стартового «Дня» нет,
    // первичная постановка не зависит ни от эффектов, ни от измерения.
    val thumbPos = remember { Animatable(selectedMode.fraction) }
    // Во время drag долю держим вне аниматора — без запуска
    // корутин на каждое событие; NaN = drag не идёт.
    var dragFraction by remember { mutableFloatStateOf(Float.NaN) }

    // Синхронизация с выбранным режимом извне (тап, drag-end, cancel).
    // При входе цель совпадает со стартовым значением — движения нет.
    LaunchedEffect(selectedMode) {
        if (dragFraction.isNaN()) {
            thumbPos.animateTo(
                targetValue = selectedMode.fraction,
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
                    onDragStart = {
                        dragFraction = thumbPos.value
                    },
                    onDragEnd = {
                        val releasedFraction = dragFraction
                        dragFraction = Float.NaN
                        // Ближайший раздел — по доле позиции: порог —
                        // середина между разделами (индекс 0 достижим
                        // при тяге влево).
                        val targetIndex = (releasedFraction * (count - 1))
                            .roundToInt()
                            .coerceIn(0, count - 1)
                        scope.launch {
                            thumbPos.snapTo(releasedFraction)
                            thumbPos.animateTo(
                                targetValue = modes[targetIndex].fraction,
                                animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                            )
                        }
                        onModeSelect(modes[targetIndex])
                    },
                    onDragCancel = {
                        dragFraction = Float.NaN
                        scope.launch {
                            thumbPos.animateTo(
                                targetValue = selectedMode.fraction,
                                animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                            )
                        }
                    },
                ) { change, dragAmount ->
                    change.consume()
                    val maxX = trackWidthPx - slotWidthPx
                    if (maxX > 0) {
                        dragFraction = (dragFraction + dragAmount.x / maxX)
                            .coerceIn(0f, 1f)
                    }
                }
            }
            .pointerInput(count) {
                detectTapGestures { offset ->
                    if (trackWidthPx == 0) return@detectTapGestures
                    val index = (offset.x / slotWidthPx).toInt().coerceIn(0, count - 1)
                    scope.launch {
                        thumbPos.animateTo(
                            targetValue = modes[index].fraction,
                            animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                        )
                    }
                    onModeSelect(modes[index])
                }
            },
    ) {
        // Доля позиции указателя: в покое — анимированная, во время
        // drag — drag-доля. Валидна с первого кадра (инициализирована
        // выбранным режимом) — стартового «Дня» нет.
        val positionFraction = if (dragFraction.isNaN()) thumbPos.value else dragFraction
        val maxX = (trackWidthPx - slotWidthPx).toFloat().coerceAtLeast(0f)
        if (trackWidthPx > 0) {
            // Доля позиции указателя между «Днем» (0) и «Ночью» (1) —
            // внешние цвета (фон, тулбар) лерпятся по ней.
            if (thumbFraction.floatValue != positionFraction) {
                thumbFraction.floatValue = positionFraction
            }
            val thumbPosition = positionFraction * maxX
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
                val thumbCenter = positionFraction * maxX + slotWidthPx / 2f
                val coverage = if (slotWidthPx > 0f) {
                    (1f - abs(slotCenter - thumbCenter) / slotWidthPx).coerceIn(0f, 1f)
                } else {
                    0f
                }
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

private val TAB_HEIGHT = 40.dp
private val THUMB_PADDING = 4.dp
private val THUMB_ELEVATION = 2.dp

private val TRACK_SHAPE = RoundedCornerShape(percent = 50)
private val THUMB_SHAPE = RoundedCornerShape(percent = 50)

private const val TAB_ANIM_MS = 250
