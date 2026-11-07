package com.joy.sandbox.feature.letters.impl.presentation.settings

import androidx.compose.ui.graphics.Color

/**
 * Палитра экрана настроек «Буквы» — день/ночь по системной теме.
 * Цвета фичи собраны здесь, а не в теме приложения: модуль фичи
 * не зависит от app-модуля.
 */
object LettersColors {

    // Фон экрана настроек
    val SettingsBackgroundDay = Color(0xFFF8F6F3)
    val SettingsBackgroundNight = Color(0xFF161412)

    // Табы «День» / «Ночь»
    val TabsTrackDay = Color(0xFFECEAE6)
    val TabsTrackNight = Color(0xFF2A2724)

    val TabsThumbDay = Color(0xFFFFFFFF)
    val TabsThumbNight = Color(0xFF3D3A35)

    val TabsLabelIdleDay = Color(0xFF6B6B6B)
    val TabsLabelIdleNight = Color(0xFF9A958E)

    val TabsLabelActiveDay = Color(0xFF1A1A1A)
    val TabsLabelActiveNight = Color(0xFFF2EFE9)

    // Кнопки «Запустить» — тёплый акцент из палитры фичи
    val ButtonContainerDay = Color(0xFFD96C3F)
    val ButtonContentDay = Color(0xFFFFFFFF)

    val ButtonContainerNight = Color(0xFFA85C33)
    val ButtonContentNight = Color(0xFFFBEDE3)

    // Текст (заголовки групп, подписи опций)
    val TextPrimaryDay = Color(0xFF1A1A1A)
    val TextPrimaryNight = Color(0xFFF2EFE9)

    // Switch во включённом состоянии: подложка — светлый оттенок акцента
    val SwitchTrackCheckedDay = Color(0xFFF2C4A8)
    val SwitchTrackCheckedNight = Color(0xFF6E4630)
}
