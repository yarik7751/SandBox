package com.joy.sandbox.feature.notifications.impl.presentation

import android.Manifest
import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.joy.sandbox.core.navigation.router.JoyRouter
import com.joy.sandbox.core.ui.SandBoxToolbar
import com.joy.sandbox.core.ui.ViewModelFactory
import com.joy.sandbox.feature.notifications.impl.R
import com.joy.sandbox.feature.notifications.impl.presentation.NotificationsViewModel.Commands
import com.joy.sandbox.utils.observe

/**
 * Экран-тренажёр уведомлений: каждая кнопка показывает один вид.
 *
 * Разрешение POST_NOTIFICATIONS (Android 13+): запрашиваем через
 * ActivityResultContracts — системный диалог возвращает результат
 * в launcher, оттуда передаём его во ViewModel.
 */
@Composable
fun NotificationsScreen(
    viewModelFactory: ViewModelFactory,
    router: JoyRouter,
) {
    val viewModel: NotificationsViewModel = viewModel(factory = viewModelFactory)
    val state by viewModel.state.collectAsStateWithLifecycle()

    // Создаём каналы и сверяем разрешение при первом входе на экран.
    LaunchedEffect(Unit) {
        viewModel.onScreenOpened()
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        viewModel.onPermissionResult(granted)
    }

    Scaffold(
        topBar = {
            SandBoxToolbar(
                title = stringResource(R.string.notifications_title),
                onBackClick = { router.exit() },
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (!state.isPermissionGranted) {
                    PermissionBlock(viewModel)
                }
                NotificationButtons(viewModel)
            }
        }
    }

    HandleCommands(
        viewModel = viewModel,
        onRequestPermission = { permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) },
    )
}

/** Блок про разрешение: на Android 13+ без него ничего не покажется. */
@Composable
private fun PermissionBlock(viewModel: NotificationsViewModel) {
    val context = LocalContext.current

    Text(
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
        text = stringResource(R.string.notifications_permission_needed),
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.error,
    )
    Button(onClick = viewModel::onRequestPermissionClick) {
        Text(text = stringResource(R.string.notifications_button_request_permission))
    }
    // Если пользователь отклонил запрос дважды, системный диалог больше
    // не появится — только ручной переход в настройки приложения.
    Button(
        modifier = Modifier.padding(top = 4.dp),
        onClick = {
            val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            }
            context.startActivity(intent)
        },
    ) {
        Text(text = stringResource(R.string.notifications_button_app_settings))
    }
}

/** Все кнопки видов уведомлений — по одной на метод интерактора. */
@Composable
private fun NotificationButtons(viewModel: NotificationsViewModel) {
    Column(
        modifier = Modifier.padding(top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Button(onClick = viewModel::onBasicClick) {
            Text(text = stringResource(R.string.notifications_button_basic))
        }
        Button(onClick = viewModel::onHeadsUpClick) {
            Text(text = stringResource(R.string.notifications_button_heads_up))
        }
        Button(onClick = viewModel::onLowPriorityClick) {
            Text(text = stringResource(R.string.notifications_button_low))
        }
        Button(onClick = viewModel::onDynamicChannelClick) {
            Text(text = stringResource(R.string.notifications_button_dynamic_channel))
        }
        Button(onClick = viewModel::onChannelSettingsClick) {
            Text(text = stringResource(R.string.notifications_button_channel_settings))
        }
        Button(onClick = viewModel::onBigTextClick) {
            Text(text = stringResource(R.string.notifications_button_big_text))
        }
        Button(onClick = viewModel::onBigPictureClick) {
            Text(text = stringResource(R.string.notifications_button_big_picture))
        }
        Button(onClick = viewModel::onInboxClick) {
            Text(text = stringResource(R.string.notifications_button_inbox))
        }
        Button(onClick = viewModel::onMessagingClick) {
            Text(text = stringResource(R.string.notifications_button_messaging))
        }
        Button(onClick = viewModel::onProgressClick) {
            Text(text = stringResource(R.string.notifications_button_progress))
        }
        Button(onClick = viewModel::onIndeterminateClick) {
            Text(text = stringResource(R.string.notifications_button_indeterminate))
        }
        Button(onClick = viewModel::onOngoingClick) {
            Text(text = stringResource(R.string.notifications_button_ongoing))
        }
        Button(onClick = viewModel::onGroupClick) {
            Text(text = stringResource(R.string.notifications_button_group))
        }
        Button(onClick = viewModel::onActionsClick) {
            Text(text = stringResource(R.string.notifications_button_actions))
        }
        Button(onClick = viewModel::onReplyClick) {
            Text(text = stringResource(R.string.notifications_button_reply))
        }
        Button(onClick = viewModel::onCustomClick) {
            Text(text = stringResource(R.string.notifications_button_custom))
        }
        Button(onClick = viewModel::onDelayedClick) {
            Text(text = stringResource(R.string.notifications_button_delayed))
        }
    }
}

@Composable
private fun HandleCommands(
    viewModel: NotificationsViewModel,
    onRequestPermission: () -> Unit,
) {
    viewModel.commandsFlow.observe { command ->
        when (command) {
            Commands.RequestPermission -> onRequestPermission()
        }
    }
}
