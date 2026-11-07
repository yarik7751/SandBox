package com.joy.sandbox.feature.letters.impl.domain

/**
 * Доступ к последнему выбранному режиму настроек («День»/«Ночь»).
 * Пока пользователь ни разу не выбирал режим, [loadMode] возвращает
 * [LettersMode.NIGHT] — режим по умолчанию.
 */
interface LettersModeRepository {

    fun loadMode(): LettersMode

    fun saveMode(mode: LettersMode)
}
