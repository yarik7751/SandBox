package com.joy.sandbox.feature.letters.impl.presentation.letters

import android.os.SystemClock
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
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
    val outlineWidth = with(LocalDensity.current) { OUTLINE_WIDTH_DP.dp.toPx() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged { canvasSize = it }
            .crossGesture(
                minStrokeLength = {
                    minOf(canvasSize.width, canvasSize.height) * MIN_STROKE_FRACTION
                },
                onCross = viewModel::onCrossDrawn,
            ),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val letter = state.letter ?: return@Canvas
            val style = TextStyle(
                fontSize = (size.minDimension * LETTER_SIZE_FRACTION).sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
            )
            val layout = textMeasurer.measure(
                text = letter.char.toString(),
                style = style,
                constraints = Constraints(maxWidth = size.width.toInt()),
            )
            val topLeft = Offset(
                x = (size.width - layout.size.width) / 2f,
                y = (size.height - layout.size.height) / 2f,
            )

            // Контур буквы — всегда.
            drawText(
                textLayoutResult = layout,
                topLeft = topLeft,
                color = OUTLINE_COLOR,
                drawStyle = Stroke(width = outlineWidth),
            )
            // Заливка — через волнообразный clip, растущий снизу вверх.
            clipPath(
                path = revealPath(
                    size = size,
                    progress = state.fillProgress,
                    seed = letter.char.code,
                ),
            ) {
                drawText(
                    textLayoutResult = layout,
                    topLeft = topLeft,
                    color = FILL_COLOR,
                )
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

/**
 * Clip-область закраски: снизу от низа экрана до волнистой линии,
 * высота которой растёт с [progress]. Форма волн детерминирована
 * [seed] — у каждой буквы своя, но стабильная между кадрами.
 */
private fun revealPath(size: Size, progress: Float, seed: Int): Path {
    val fillTop = size.height * (1f - progress)
    val amplitude = size.height * WAVE_AMPLITUDE_FRACTION
    val random = Random(seed)
    val offsets = List(WAVE_SEGMENTS + 1) { random.nextFloat() * amplitude }

    val path = Path()
    path.moveTo(0f, fillTop + offsets.first())
    for (i in 1..WAVE_SEGMENTS) {
        val x = size.width * i / WAVE_SEGMENTS
        path.lineTo(x, fillTop + offsets[i])
    }
    path.lineTo(size.width, size.height)
    path.lineTo(0f, size.height)
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

private const val LETTER_SIZE_FRACTION = 0.55f
private const val OUTLINE_WIDTH_DP = 3
private const val WAVE_AMPLITUDE_FRACTION = 0.02f
private const val WAVE_SEGMENTS = 24

private val OUTLINE_COLOR = Color(0xFF333333)
private val FILL_COLOR = Color(0xFFE53935)
