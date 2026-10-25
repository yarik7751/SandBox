package com.joy.sandbox.utils

import android.view.ViewTreeObserver
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.flowWithLifecycle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@Composable
inline fun <reified T> Flow<T>.observe(
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
    key: Any = Unit,
    noinline action: suspend (T) -> Unit,
) {
    LaunchedEffect(key1 = key) {
        onEach(action)
            .flowWithLifecycle(lifecycleOwner.lifecycle, minActiveState)
            .launchIn(this)
    }
}

@Composable
fun keyboardAsState(): State<Boolean> {
    val view = LocalView.current
    val viewTreeObserver = view.viewTreeObserver
    var isImeVisible by remember { mutableStateOf(false) }

    DisposableEffect(viewTreeObserver) {
        val listener = ViewTreeObserver.OnPreDrawListener {
            isImeVisible = ViewCompat.getRootWindowInsets(view)
                ?.isVisible(WindowInsetsCompat.Type.ime()) == true
            true
        }
        viewTreeObserver.addOnPreDrawListener(listener)
        onDispose {
            viewTreeObserver.removeOnPreDrawListener(listener)
        }
    }
    return rememberUpdatedState(isImeVisible)
}
