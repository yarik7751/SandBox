package com.joy.sandbox.feature.letters.impl.domain

import com.joy.sandbox.feature.letters.api.model.Alphabet

/**
 * Озвучка названий букв: проигрывает аудиофайл буквы и приостанавливает
 * вызывающую корутину до конца проигрывания. Буква без аудиофайла просто
 * не озвучивается — вызов возвращается сразу.
 */
interface LettersSpeaker {

    suspend fun awaitLetterSpoken(letter: Char, alphabet: Alphabet)

    /** Освобождение ресурсов (выход с экрана). */
    fun release()
}
