package com.joy.sandbox.feature.letters.impl.presentation

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

/**
 * Immersive-режим на время жизни экрана: скрывает status и navigation bar,
 * свайп от края временно показывает системные панели. При уходе с экрана
 * системные бары восстанавливаются.
 */
@Composable
internal fun ImmersiveModeEffect() {
    val activity = LocalContext.current as ComponentActivity
    val insetsController = remember { WindowInsetsControllerCompat(activity.window, activity.window.decorView) }
    DisposableEffect(Unit) {
        insetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        insetsController.hide(WindowInsetsCompat.Type.systemBars())
        onDispose { insetsController.show(WindowInsetsCompat.Type.systemBars()) }
    }
}
