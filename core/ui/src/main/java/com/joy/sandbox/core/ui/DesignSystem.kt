package com.joy.sandbox.core.ui

import androidx.compose.ui.graphics.Color

object DesignSystem {

    object Colors {
        val testBg = Color(0xFFD8F0FA)
        val testBlockBg = Color(0xFFFFFFFF)
        val testBlockText = Color(0xFF252525)
        val testText = Color(0xFFFFFFFF)
        val testWidgetBg = Color(0x80FFC0BF)
        val defaultWidgetBg = Color(0x80DFFFBF)
        val batteryLevelBg = Color(0x80919191)
        val testSuccessText = Color(0xFF91FF25)
        val batteryLevelActiveWarning = Color(0xFFFFE225)
        val testErrorText = Color(0xFFFF2525)
        val testBrandColor = Color(0xFF42A1EA)
        val buttonIgnoreBg = Color(0xFFB0B0B4)

        /** Палитра экрана настроек «Буквы» — день/ночь по выбранному режиму. */
        object Letters {

            // Фон экрана настроек
            val SettingsBackgroundDay = Color(0xFFFCF4DC)
            val SettingsBackgroundNight = Color(0xFF161412)

            // Тулбар: днём — насыщенный крем (мягко отделяется от фона),
            // ночью — чуть светлее фона экрана
            val ToolbarBackgroundDay = Color(0xFFF7ECC9)
            val ToolbarContentDay = Color(0xFF1A1A1A)

            val ToolbarBackgroundNight = Color(0xFF221F1C)
            val ToolbarContentNight = Color(0xFFF2EFE9)

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

            // Плитки опций: не выбранная — серая, выбранная — белая
            val OptionTileUnselectedDay = Color(0xFFECEAE6)
            val OptionTileSelectedDay = Color(0xFFFFFFFF)

            val OptionTileUnselectedNight = Color(0xFF2A2724)
            val OptionTileSelectedNight = Color(0xFFFFFFFF)

            // Текст на выбранной (белой) плитке — тёмный в обоих режимах
            val OptionTileSelectedText = Color(0xFF1A1A1A)
        }
    }
}
