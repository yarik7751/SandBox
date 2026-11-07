package com.joy.sandbox.core.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import android.os.Build
import android.view.WindowInsets
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowInsetsControllerCompat

/**
 * Закрашивает status bar в [statusBarColor], а navigation bar — в
 * [navigationBarColor]; выбирает цвет системных иконок: [lightIcons] = true —
 * светлые иконки (тёмный фон), false — тёмные иконки (светлый фон).
 *
 * Цвета применяются на каждой рекомпозиции, поэтому подаются анимированные
 * цвета — бары едут вместе с фоном экрана. Прежние значения (фон декора,
 * цвета окон и флаги иконок) восстанавливаются при выходе с экрана.
 *
 * На Android 15+ цвета окон барами игнорируются: там фон декора красится
 * в две полосы — верхняя (высота status bar) в [statusBarColor],
 * остальное в [navigationBarColor]. Фон декора подменяется один раз
 * (подмена фона сбрасывает паддинги декора, поэтому после неё запрашивается
 * повторная доставка инсетов — паддинги ставит слушатель в приложении);
 * дальше на каждом кадре меняются только цвета drawable.
 */
@Composable
fun SystemBarColors(
    statusBarColor: Color,
    navigationBarColor: Color,
    lightIcons: Boolean,
) {
    val view = LocalView.current
    val splitDrawable = remember { SplitSystemBarsDrawable() }

    DisposableEffect(view) {
        val window = view.context.findActivity().window
        val prevDecorBackground = window.decorView.background
        val prevStatusBarColor = window.statusBarColor
        val prevNavBarColor = window.navigationBarColor
        val controller = WindowInsetsControllerCompat(window, view)
        val prevLightStatusIcons = controller.isAppearanceLightStatusBars
        val prevLightNavIcons = controller.isAppearanceLightNavigationBars

        onDispose {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
                window.decorView.background = prevDecorBackground
            } else {
                window.statusBarColor = prevStatusBarColor
                window.navigationBarColor = prevNavBarColor
            }
            controller.isAppearanceLightStatusBars = prevLightStatusIcons
            controller.isAppearanceLightNavigationBars = prevLightNavIcons
        }
    }

    SideEffect {
        val window = view.context.findActivity().window
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            splitDrawable.topHeightPx = view.rootWindowInsets
                ?.getInsets(WindowInsets.Type.statusBars())
                ?.top
                ?: 0
            splitDrawable.topColor = statusBarColor.toArgb()
            splitDrawable.bottomColor = navigationBarColor.toArgb()
            if (window.decorView.background !== splitDrawable) {
                window.decorView.setBackgroundDrawable(splitDrawable)
                // Подмена фона сбрасывает паддинги декора — просим инсеты
                // перепримениться (паддинги ставит слушатель в приложении).
                view.requestApplyInsets()
            } else {
                splitDrawable.invalidateSelf()
            }
        } else {
            window.statusBarColor = statusBarColor.toArgb()
            window.navigationBarColor = navigationBarColor.toArgb()
        }
        val controller = WindowInsetsControllerCompat(window, view)
        controller.isAppearanceLightStatusBars = !lightIcons
        controller.isAppearanceLightNavigationBars = !lightIcons
    }
}

/**
 * Фон декора на Android 15+: верхняя полоса [topHeightPx] закрашена
 * в [topColor] (status bar), остальная площадь — в [bottomColor]
 * (navigation bar). Поля меняются на каждом кадре анимации цветов.
 */
private class SplitSystemBarsDrawable : Drawable() {

    var topHeightPx: Int = 0
    var topColor: Int = android.graphics.Color.TRANSPARENT
    var bottomColor: Int = android.graphics.Color.TRANSPARENT

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    override fun draw(canvas: Canvas) {
        val width = bounds.width().toFloat()
        val height = bounds.height().toFloat()
        paint.color = bottomColor
        canvas.drawRect(0f, 0f, width, height, paint)
        paint.color = topColor
        canvas.drawRect(0f, 0f, width, topHeightPx.toFloat(), paint)
    }

    override fun setAlpha(alpha: Int) = Unit

    override fun setColorFilter(colorFilter: ColorFilter?) = Unit

    @Deprecated("Deprecated in Java")
    override fun getOpacity(): Int = PixelFormat.OPAQUE
}

/** Activity из контекста view: ContextThemeWrapper → baseContext → Activity. */
private tailrec fun Context.findActivity(): Activity {
    if (this is Activity) return this
    if (this is ContextWrapper) return baseContext.findActivity()
    error("Activity not found from context $this")
}
