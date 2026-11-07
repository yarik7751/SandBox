package com.joy.sandbox.feature.letters.impl.domain

/**
 * Последний выбранный режим настроек («День»/«Ночь»): чтение при
 * входе на экран настроек и сохранение при переключении.
 */
interface LettersModeInteractor {

    fun loadLastMode(): LettersMode

    fun saveMode(mode: LettersMode)
}
