package com.joy.sandbox.feature.letters.impl.presentation.letters

import android.os.SystemClock
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
import com.joy.sandbox.feature.letters.api.model.Alphabet
import com.joy.sandbox.feature.letters.api.model.LettersOrder
import com.joy.sandbox.feature.letters.impl.presentation.letters.LettersViewModel.Commands
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
                seed = letter.char.code,
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LETTER_HOLLOW_COLOR)
            .onSizeChanged { canvasSize = it }
            .crossGesture(
                minStrokeLength = {
                    minOf(canvasSize.width, canvasSize.height) * MIN_STROKE_FRACTION
                },
                onCross = viewModel::onCrossDrawn,
            ),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val render = letterRender ?: return@Canvas
            val layout = render.layout
            val topLeft = Offset(
                x = (size.width - layout.size.width) / 2f,
                y = (size.height - layout.size.height) / 2f,
            )

            // 1. Чёрный фон и белая буква текстом: раскладка сама корректно
            //    обрабатывает внутренние и пересекающиеся контуры глифа
            //    (у Б, В штанга и чаша — отдельные контуры с наложением,
            //    поэтому вырезать букву из plate-пути через glyphPath
            //    нельзя — EvenOdd ломается на пересечениях).
            //    Граница чёрного и есть контур, обводка Stroke не нужна.
            drawRect(color = PLATE_COLOR)
            drawText(
                textLayoutResult = layout,
                topLeft = topLeft,
                color = LETTER_HOLLOW_COLOR,
            )
            // 2. Цветная заливка — волнообразный clip, растущий от нижней
            //    точки буквы к верхней, дополнительно обрезанный контуром
            //    самого глифа (NonZero: пересекающиеся контуры глифа
            //    суммируются и остаются частью буквы). Верх области заливки
            //    поднят на максимальную высоту зазубрины (амплитуду волны),
            //    чтобы при полной заливке не оставалось белых пиков.
            val fillBounds = render.glyphBounds.translate(topLeft).let { bounds ->
                bounds.copy(
                    top = bounds.top - bounds.height * WAVE_AMPLITUDE_FRACTION,
                )
            }
            clipPath(
                path = revealPath(
                    bounds = fillBounds,
                    progress = state.fillProgress,
                    seed = render.seed,
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
            Commands.ExitToHome -> router.exit()
        }
    }
}

private class LetterRender(
    val layout: TextLayoutResult,
    val glyphPath: Path,
    val glyphBounds: Rect,
    val fillColor: Color,
    val seed: Int,
)

/**
 * Paint платформенной раскладки текста для построения контура глифа:
 * жирный sans-serif того же размера, что у измеренного layout.
 */
private fun glyphPaint(textSizePx: Float): android.graphics.Paint =
    android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
        typeface = android.graphics.Typeface.DEFAULT_BOLD
        textSize = textSizePx
    }

/**
 * Clip-область закраски: от низа [bounds] до волнистой линии, высота
 * которой растёт с [progress] — заливка поднимается с нижней точки
 * буквы до верхней. Форма волн детерминирована [seed] — у каждой буквы
 * своя, но стабильная между кадрами.
 */
private fun revealPath(bounds: Rect, progress: Float, seed: Int): Path {
    val fillTop = bounds.bottom - bounds.height * progress
    val amplitude = bounds.height * WAVE_AMPLITUDE_FRACTION
    val random = Random(seed)
    val offsets = List(WAVE_SEGMENTS + 1) { random.nextFloat() * amplitude }

    val path = Path()
    path.moveTo(bounds.left, fillTop + offsets.first())
    for (i in 1..WAVE_SEGMENTS) {
        val x = bounds.left + bounds.width * i / WAVE_SEGMENTS
        path.lineTo(x, fillTop + offsets[i])
    }
    path.lineTo(bounds.right, bounds.bottom)
    path.lineTo(bounds.left, bounds.bottom)
    path.close()
    return path
}

/**
 * Детектор жеста-креста: первый штрих сверху вниз, второй (после
 * отпускания, в пределах таймаута) справа налево → [onCross].
 */
private fun Modifier.crossGesture(
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

private class Stroke(points: List<Offset>) {

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
private fun KeepScreenOnEffect() {
    val view = LocalView.current
    DisposableEffect(Unit) {
        view.keepScreenOn = true
        onDispose { view.keepScreenOn = false }
    }
}

private const val STAGE_NONE = 0
private const val STAGE_VERTICAL_DONE = 1

private const val MIN_STROKE_FRACTION = 0.3f
private const val DOMINANT_AXIS_RATIO = 2f
private const val STROKES_TIMEOUT_MS = 2_000L

private const val LETTER_WIDTH_FRACTION = 0.8f
private const val LETTER_MAX_HEIGHT_FRACTION = 0.8f
private const val REFERENCE_FONT_SIZE_PX = 100f
private const val WAVE_AMPLITUDE_FRACTION = 0.02f
private const val WAVE_SEGMENTS = 24

private val PLATE_COLOR = Color.Black
private val LETTER_HOLLOW_COLOR = Color.White
