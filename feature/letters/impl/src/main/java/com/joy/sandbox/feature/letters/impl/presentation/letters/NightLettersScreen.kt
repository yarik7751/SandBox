package com.joy.sandbox.feature.letters.impl.presentation.letters

import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.IntSize
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.joy.sandbox.core.navigation.currentNavStringArgument
import com.joy.sandbox.core.navigation.router.JoyRouter
import com.joy.sandbox.core.ui.ViewModelFactory
import com.joy.sandbox.feature.letters.api.LettersSettingsJoyScreen
import com.joy.sandbox.feature.letters.api.NightLettersJoyScreen
import com.joy.sandbox.feature.letters.api.model.Alphabet
import com.joy.sandbox.feature.letters.api.model.LettersOrder
import com.joy.sandbox.feature.letters.impl.presentation.letters.NightLettersViewModel.Commands
import com.joy.sandbox.utils.observe

/**
 * Ночной режим показа букв для засыпания: тот же полноэкранный показ,
 * что и [LettersScreen], но без звука, без волнистой заливки (буква
 * появляется сразу залитой через мягкий fade-in), в тёплой тусклой
 * палитре и с замедляющимся темпом; по таймеру яркость сползает
 * к минимуму и экран затухает к чёрному.
 *
 * Блокировки — как в дневном режиме (2.6): immersive, заблокированный
 * back, единственный выход — жест-крест родителя. При сворачивании
 * приложения сеанс паузится и продолжается с того же кадра.
 */
@Composable
fun NightLettersScreen(
    viewModelFactory: ViewModelFactory,
    router: JoyRouter,
) {
    val viewModel: NightLettersViewModel = viewModel(factory = viewModelFactory)
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()

    val alphabet = currentNavStringArgument(NightLettersJoyScreen.ARG_ALPHABET)
        ?.let { name -> runCatching { Alphabet.valueOf(name) }.getOrNull() }
        ?: Alphabet.CYRILLIC
    val order = currentNavStringArgument(NightLettersJoyScreen.ARG_ORDER)
        ?.let { name -> runCatching { LettersOrder.valueOf(name) }.getOrNull() }
        ?: LettersOrder.ALPHABETICAL
    val minutes = currentNavStringArgument(NightLettersJoyScreen.ARG_MINUTES)
        ?.toIntOrNull()
        ?: NightLettersJoyScreen.DEFAULT_MINUTES

    LaunchedEffect(Unit) {
        viewModel.init(alphabet = alphabet, order = order, minutes = minutes)
    }

    KeepScreenOnEffect()
    ImmersiveModeEffect()
    // 2.6: back заблокирован — единственный выход — жест-крест.
    BackHandler(enabled = true) { }
    NightBrightnessEffect(
        brightness = state.brightness,
        finished = state.finished,
    )
    PauseOnStopEffect(viewModel = viewModel)

    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current

    // Размер буквы и контур глифа — той же логикой, что в дневном режиме.
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

    // 2.5→8 с: волнистая заливка кадрируется по кадрам отрисовки
    // (Compose-анимация, LinearEasing) — без дискретных тиков.
    // Ключ — буква: на каждый кадр показа заливка стартует с нуля.
    val fillProgress = remember(state.letter) { Animatable(0f) }
    LaunchedEffect(state.letter) {
        if (state.letter != null) {
            fillProgress.snapTo(0f)
            fillProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = NightModeConfig.FILL_DURATION_MS.toInt(),
                    easing = LinearEasing,
                ),
            )
        }
    }

    // 2.5: появление через мягкий fade-in 1 с; исчезновение — длиннее:
    // чёрный промежуток между кадрами (2.2).
    val letterAlpha by animateFloatAsState(
        targetValue = if (state.letterVisible) 1f else 0f,
        animationSpec = tween(
            durationMillis = if (state.letterVisible) {
                NightModeConfig.LETTER_FADE_IN_MS.toInt()
            } else {
                NightModeConfig.FRAME_FADE_MS.toInt()
            },
        ),
        label = "nightLetterAlpha",
    )

    // 2.4: финальный fade к чисто чёрному экрану.
    val blackOverlayAlpha by animateFloatAsState(
        targetValue = if (state.finished) 1f else 0f,
        animationSpec = tween(durationMillis = NightModeConfig.FINAL_BLACK_MS.toInt()),
        label = "nightBlackOverlay",
    )

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
        // Альфа применяется только к Canvas с буквой — чёрный фон
        // Box остаётся без анимации (между кадрами — чёрный промежуток).
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

            // 1. Тускло-серый контур текстом — граница с чёрным фоном
            //    и есть контур, обводка Stroke не нужна.
            drawText(
                textLayoutResult = layout,
                topLeft = topLeft,
                color = NightModeConfig.CONTOUR_COLOR,
            )
            // 2. Цветная заливка — волнистая линия, как в дневном режиме,
            //    но в 2 раза медленнее (8 с): медленная заливка не будит,
            //    а мягко усыпляет.
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

        if (blackOverlayAlpha > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(alpha = blackOverlayAlpha)
                    .background(Color.Black),
            )
        }
    }

    HandleCommands(
        viewModel = viewModel,
        router = router,
    )
}

@Composable
private fun HandleCommands(
    viewModel: NightLettersViewModel,
    router: JoyRouter,
) {
    viewModel.commandsFlow.observe { command ->
        when (command) {
            // Выход — на настройки: показ заменяется ими, в стеке
            // не остаётся (back из настроек попадает на Главный).
            Commands.ExitToSettings -> router.replaceScreen(LettersSettingsJoyScreen)
        }
    }
}

/**
 * Яркость окна на время сеанса (2.7): старт — 0.15, дальше плавно
 * следует за значением из ViewModel (в последнюю минуту таймера —
 * к минимуму, при завершении — к нулю). При уходе с экрана системная
 * яркость восстанавливается (BRIGHTNESS_OVERRIDE_NONE).
 */
@Composable
private fun NightBrightnessEffect(
    brightness: Float,
    finished: Boolean,
) {
    val activity = LocalContext.current as ComponentActivity
    val animatedBrightness = remember { Animatable(NightModeConfig.START_BRIGHTNESS) }

    DisposableEffect(Unit) {
        onDispose {
            val attrs = activity.window.attributes
            attrs.screenBrightness =
                android.view.WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
            activity.window.attributes = attrs
        }
    }

    LaunchedEffect(brightness, finished) {
        animatedBrightness.animateTo(
            targetValue = if (finished) 0f else brightness,
            animationSpec = tween(durationMillis = NightModeConfig.TICK_MS.toInt() * 3),
        )
    }

    val appliedBrightness = animatedBrightness.value
    LaunchedEffect(appliedBrightness) {
        val attrs = activity.window.attributes
        if (attrs.screenBrightness != appliedBrightness) {
            attrs.screenBrightness = appliedBrightness
            activity.window.attributes = attrs
        }
    }
}

/**
 * Пауза сеанса при сворачивании приложения, продолжение при возврате
 * (2.6). ON_START при первом появлении экрана игнорируется: ViewModel
 * ещё не на паузе.
 */
@Composable
private fun PauseOnStopEffect(viewModel: NightLettersViewModel) {
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_STOP -> viewModel.pause()
                Lifecycle.Event.ON_START -> viewModel.resume()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
}
