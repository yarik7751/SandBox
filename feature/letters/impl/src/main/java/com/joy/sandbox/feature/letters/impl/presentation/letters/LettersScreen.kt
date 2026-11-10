package com.joy.sandbox.feature.letters.impl.presentation.letters

import android.os.SystemClock
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.joy.sandbox.core.navigation.currentNavStringArgument
import com.joy.sandbox.core.navigation.router.JoyRouter
import com.joy.sandbox.core.ui.ViewModelFactory
import com.joy.sandbox.feature.letters.api.LettersJoyScreen
import com.joy.sandbox.feature.letters.api.LettersDaySettingsJoyScreen
import com.joy.sandbox.feature.letters.api.model.Alphabet
import com.joy.sandbox.feature.letters.api.model.LettersOrder
import com.joy.sandbox.feature.letters.impl.presentation.ImmersiveModeEffect
import com.joy.sandbox.feature.letters.impl.presentation.letters.LettersViewModel.Commands
import com.joy.sandbox.feature.letters.impl.presentation.letters.model.FillDirection
import com.joy.sandbox.utils.observe
import kotlin.math.abs
import kotlin.random.Random

/**
 * Экран показа букв: по очереди выводит буквы, контур уже на месте,
 * заливка медленно растёт — как будто букву закрашивают вручную.
 *
 * Экран не обрабатывает нажатия; единственное действие — жест-крест:
 * штрих сверху вниз, отпустить, затем штрих справа налево → возврат
 * на Главный.
 */
@Composable
fun LettersScreen(
    viewModelFactory: ViewModelFactory,
    router: JoyRouter,
) {
    val viewModel: LettersViewModel = viewModel(factory = viewModelFactory)
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()

    val alphabet = currentNavStringArgument(LettersJoyScreen.ARG_ALPHABET)
        ?.let { name -> runCatching { Alphabet.valueOf(name) }.getOrNull() }
        ?: Alphabet.CYRILLIC
    val order = currentNavStringArgument(LettersJoyScreen.ARG_ORDER)
        ?.let { name -> runCatching { LettersOrder.valueOf(name) }.getOrNull() }
        ?: LettersOrder.ALPHABETICAL

    LaunchedEffect(Unit) {
        viewModel.init(alphabet = alphabet, order = order)
    }

    KeepScreenOnEffect()
    ImmersiveModeEffect()
    // Back заблокирован: единственный выход — жест-крест.
    BackHandler(enabled = true) { }

    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current

    // Размер буквы подгоняется под canvas один раз на смену буквы:
    // измеряем на опорном размере и линейно масштабируем до 80 % ширины.
    // Заодно строим Path-контур глифа — им будет обрезаться цветная заливка.
    val letterRender = remember(state.letter, canvasSize, density) {
        val letter = state.letter
        if (letter == null || canvasSize.width == 0 || canvasSize.height == 0) {
            null
        } else {
            val baseStyle = TextStyle(
                fontSize = with(density) { REFERENCE_FONT_SIZE_PX.toSp() },
                fontWeight = FontWeight.Bold,
            )
            val reference = textMeasurer.measure(
                text = letter.char.toString(),
                style = baseStyle,
            )
            val scale = minOf(
                canvasSize.width * LETTER_WIDTH_FRACTION / reference.size.width,
                canvasSize.height * LETTER_MAX_HEIGHT_FRACTION / reference.size.height,
            )
            val layout = textMeasurer.measure(
                text = letter.char.toString(),
                style = baseStyle.copy(
                    fontSize = with(density) { (REFERENCE_FONT_SIZE_PX * scale).toSp() },
                ),
            )
            // Контур глифа берём из платформенной раскладки текста
            // (baseline совпадает с firstBaseline измеренного layout).
            val androidPath = android.graphics.Path()
            glyphPaint(textSizePx = REFERENCE_FONT_SIZE_PX * scale).getTextPath(
                letter.char.toString(),
                0,
                1,
                0f,
                layout.firstBaseline,
                androidPath,
            )
            val bounds = android.graphics.RectF()
            androidPath.computeBounds(bounds, true)
            LetterRender(
                layout = layout,
                glyphPath = androidPath.asComposePath(),
                glyphBounds = Rect(bounds.left, bounds.top, bounds.right, bounds.bottom),
                fillColor = letter.fillColor,
                fillDirection = letter.fillDirection,
                seed = letter.char.code,
            )
        }
    }

    // Альфа буквы: появление и исчезновение через fade.
    val letterAlpha by animateFloatAsState(
        targetValue = if (state.letterVisible) 1f else 0f,
        animationSpec = tween(durationMillis = FADE_DURATION_MS.toInt()),
    )

    // Прогресс заливки кадрируется по кадрам отрисовки (Compose-анимация,
    // LinearEasing) — без дискретных тиков, плавно на 60/120 fps.
    // Ключ — буква: на каждый кадр показа заливка стартует с нуля.
    val fillProgress = remember(state.letter) { Animatable(0f) }
    LaunchedEffect(state.letter) {
        if (state.letter != null) {
            fillProgress.snapTo(0f)
            fillProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = LettersViewModel.FILL_DURATION_MS.toInt(),
                    easing = LinearEasing,
                ),
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PLATE_COLOR)
            .onSizeChanged { canvasSize = it }
            .crossGesture(
                minStrokeLength = {
                    minOf(canvasSize.width, canvasSize.height) * MIN_STROKE_FRACTION
                },
                onCross = viewModel::onCrossDrawn,
            ),
    ) {
        // Альфа применяется только к этому Canvas с буквой — чёрный фон
        // Box остаётся без анимации.
        Canvas(modifier = Modifier
            .fillMaxSize()
            .graphicsLayer(alpha = letterAlpha),
        ) {
            val render = letterRender ?: return@Canvas
            val layout = render.layout
            val topLeft = Offset(
                x = (size.width - layout.size.width) / 2f,
                y = (size.height - layout.size.height) / 2f,
            )

            // 1. Белая буква текстом: раскладка сама корректно обрабатывает
            //    внутренние и пересекающиеся контуры глифа (у Б, В штанга
            //    и чаша — отдельные контуры с наложением, поэтому вырезать
            //    букву из plate-пути через glyphPath нельзя — EvenOdd
            //    ломается на пересечениях).
            //    Граница чёрного и есть контур, обводка Stroke не нужна.
            drawText(
                textLayoutResult = layout,
                topLeft = topLeft,
                color = LETTER_HOLLOW_COLOR,
            )
            // 2. Цветная заливка — волнистая линия, движущаяся по оси
            //    направления [FillDirection.axis] от угла-источника к целевому,
            //    дополнительно обрезанная контуром самого глифа (NonZero:
            //    пересекающиеся контуры глифа суммируются и остаются частью
            //    буквы). Область заливки расширена на амплитуду волны со
            //    всех сторон — при полной заливке не остаётся белых пиков
            //    ни в одном направлении.
            val glyphBounds = render.glyphBounds.translate(topLeft)
            val waveAmplitude = minOf(glyphBounds.width, glyphBounds.height) *
                WAVE_AMPLITUDE_FRACTION
            val fillBounds = Rect(
                left = glyphBounds.left - waveAmplitude,
                top = glyphBounds.top - waveAmplitude,
                right = glyphBounds.right + waveAmplitude,
                bottom = glyphBounds.bottom + waveAmplitude,
            )
            clipPath(
                path = revealPath(
                    bounds = fillBounds,
                    amplitude = waveAmplitude,
                    progress = fillProgress.value,
                    seed = render.seed,
                    direction = render.fillDirection,
                ),
            ) {
                withTransform({ translate(topLeft.x, topLeft.y) }) {
                    clipPath(path = render.glyphPath) {
                        drawText(
                            textLayoutResult = layout,
                            topLeft = Offset.Zero,
                            color = render.fillColor,
                        )
                    }
                }
            }
        }
    }

    HandleCommands(
        viewModel = viewModel,
        router = router,
    )
}

@Composable
private fun HandleCommands(
    viewModel: LettersViewModel,
    router: JoyRouter,
) {
    viewModel.commandsFlow.observe { command ->
        when (command) {
            // Выход — на дневные настройки: показ заменяется ими, в стеке
            // не остаётся (back из настроек попадает на Главный экран фичи).
            Commands.ExitToSettings -> router.replaceScreen(LettersDaySettingsJoyScreen)
        }
    }
}

internal class LetterRender(
    val layout: TextLayoutResult,
    val glyphPath: Path,
    val glyphBounds: Rect,
    val fillColor: Color,
    val fillDirection: FillDirection,
    val seed: Int,
)

/**
 * Paint платформенной раскладки текста для построения контура глифа:
 * жирный sans-serif того же размера, что у измеренного layout.
 */
internal fun glyphPaint(textSizePx: Float): android.graphics.Paint =
    android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
        typeface = android.graphics.Typeface.DEFAULT_BOLD
        textSize = textSizePx
    }

/**
 * Clip-область закраски: сторона угла-источника волнистой линии,
 * движущейся по оси [direction] к целевому углу с ростом [progress].
 * Форма волн детерминирована [seed] — у каждой буквы своя, но стабильная
 * между кадрами. Смещения волны не превышают [amplitude] — вызывающий
 * код расширяет bounds как минимум на эту величину, тогда при
 * progress = 1 вся исходная область закрашена.
 */
internal fun revealPath(
    bounds: Rect,
    amplitude: Float,
    progress: Float,
    seed: Int,
    direction: FillDirection,
): Path {
    val u = direction.axis
    val v = Offset(x = -u.y, y = u.x)
    val corners = listOf(
        Offset(bounds.left, bounds.top),
        Offset(bounds.right, bounds.top),
        Offset(bounds.right, bounds.bottom),
        Offset(bounds.left, bounds.bottom),
    )
    val projectionsU = corners.map { it.x * u.x + it.y * u.y }
    val projectionsV = corners.map { it.x * v.x + it.y * v.y }
    val minU = projectionsU.min()
    val maxU = projectionsU.max()
    val minV = projectionsV.min()
    val maxV = projectionsV.max()

    val threshold = minU + (maxU - minU) * progress
    val random = Random(seed)
    val offsets = List(WAVE_SEGMENTS + 1) { random.nextFloat() * amplitude }

    // Базовая точка линии: lineBase·u = threshold; v ортогонален u,
    // поэтому прибавление v * s растягивает линию вдоль поперечной оси.
    val lineBase = Offset(x = u.x * threshold, y = u.y * threshold)
    // Замыкаем полигон за краем у источника (сторона x·u <= threshold) —
    // клип глифа обрежет лишнее.
    val farProjection = minU - (maxU - minU)
    val farBase = Offset(x = u.x * farProjection, y = u.y * farProjection)

    val path = Path()
    for (i in 0..WAVE_SEGMENTS) {
        val alongV = minV + (maxV - minV) * i / WAVE_SEGMENTS
        val point = lineBase + v * alongV + u * offsets[i]
        if (i == 0) {
            path.moveTo(point.x, point.y)
        } else {
            path.lineTo(point.x, point.y)
        }
    }
    val farEnd = farBase + v * maxV
    val farStart = farBase + v * minV
    path.lineTo(farEnd.x, farEnd.y)
    path.lineTo(farStart.x, farStart.y)
    path.close()
    return path
}

/**
 * Детектор жеста-креста: первый штрих сверху вниз, второй (после
 * отпускания, в пределах таймаута) справа налево → [onCross].
 */
internal fun Modifier.crossGesture(
    minStrokeLength: () -> Float,
    onCross: () -> Unit,
): Modifier = this.pointerInput(Unit) {
    var stage = STAGE_NONE
    var verticalDoneAt = 0L
    val points = mutableListOf<Offset>()

    fun reset() {
        stage = STAGE_NONE
        points.clear()
    }

    detectDragGestures(
        onDragStart = { start ->
            points.clear()
            points.add(start)
        },
        onDragEnd = {
            val stroke = Stroke(points)
            val now = SystemClock.uptimeMillis()
            when {
                stage == STAGE_NONE && stroke.isTopToBottom(minStrokeLength()) -> {
                    stage = STAGE_VERTICAL_DONE
                    verticalDoneAt = now
                }

                stage == STAGE_VERTICAL_DONE &&
                    now - verticalDoneAt <= STROKES_TIMEOUT_MS &&
                    stroke.isRightToLeft(minStrokeLength()) -> {
                    reset()
                    onCross()
                }

                else -> reset()
            }
            points.clear()
        },
        onDragCancel = { reset() },
    ) { change, _ ->
        change.consume()
        points.add(change.position)
    }
}

internal class Stroke(points: List<Offset>) {

    private val first = points.first()
    private val last = points.lastOrNull() ?: first

    val dx: Float = last.x - first.x
    val dy: Float = last.y - first.y

    fun isTopToBottom(minLength: Float): Boolean =
        dy > minLength && dy > abs(dx) * DOMINANT_AXIS_RATIO

    fun isRightToLeft(minLength: Float): Boolean =
        -dx > minLength && abs(dx) > abs(dy) * DOMINANT_AXIS_RATIO
}

@Composable
internal fun KeepScreenOnEffect() {
    val view = LocalView.current
    DisposableEffect(Unit) {
        view.keepScreenOn = true
        onDispose { view.keepScreenOn = false }
    }
}

/**
 * Immersive-режим вынесен в [com.joy.sandbox.feature.letters.impl.presentation.ImmersiveModeEffect]
 * — используется всеми экранами фичи.
 */
internal const val STAGE_NONE = 0
internal const val STAGE_VERTICAL_DONE = 1

internal const val MIN_STROKE_FRACTION = 0.3f
internal const val DOMINANT_AXIS_RATIO = 2f
internal const val STROKES_TIMEOUT_MS = 2_000L

internal const val LETTER_WIDTH_FRACTION = 0.8f
internal const val LETTER_MAX_HEIGHT_FRACTION = 0.8f
internal const val REFERENCE_FONT_SIZE_PX = 100f
internal const val WAVE_AMPLITUDE_FRACTION = 0.02f
internal const val WAVE_SEGMENTS = 24
internal const val FADE_DURATION_MS = 300L

internal val PLATE_COLOR = Color.Black
internal val LETTER_HOLLOW_COLOR = Color.White
