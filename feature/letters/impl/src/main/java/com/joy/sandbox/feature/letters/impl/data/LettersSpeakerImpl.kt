package com.joy.sandbox.feature.letters.impl.data

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import com.joy.sandbox.feature.letters.api.model.Alphabet
import com.joy.sandbox.feature.letters.impl.R
import com.joy.sandbox.feature.letters.impl.domain.LettersSpeaker
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume

/**
 * Проигрывает записанные названия букв (m4a из raw/) через MediaPlayer:
 * колбэк завершения есть из коробки, поэтому await надёжный.
 * Для буквы без записи проигрывания нет — вызов возвращается сразу.
 */
class LettersSpeakerImpl @Inject constructor(
    private val context: Context,
) : LettersSpeaker {

    private var player: MediaPlayer? = null

    override suspend fun awaitLetterSpoken(letter: Char, alphabet: Alphabet) {
        val resId = letterAudioRes(letter, alphabet) ?: return
        suspendCancellableCoroutine { continuation ->
            val mediaPlayer = MediaPlayer.create(context, resId) ?: run {
                Log.w(TAG, "Cannot create MediaPlayer for res $resId")
                continuation.resume(Unit)
                return@suspendCancellableCoroutine
            }
            player = mediaPlayer
            mediaPlayer.setOnCompletionListener {
                mediaPlayer.release()
                if (player === mediaPlayer) player = null
                continuation.resume(Unit)
            }
            continuation.invokeOnCancellation {
                mediaPlayer.release()
                if (player === mediaPlayer) player = null
            }
            mediaPlayer.start()
        }
    }

    override fun release() {
        player?.release()
        player = null
    }

    private fun letterAudioRes(letter: Char, alphabet: Alphabet): Int? = when (alphabet) {
        Alphabet.CYRILLIC -> when (letter) {
            'А' -> R.raw.ru_a
            'Б' -> R.raw.ru_b
            'В' -> R.raw.ru_v
            'Г' -> R.raw.ru_g
            'Д' -> R.raw.ru_d
            'Е' -> R.raw.ru_e
            'Ё' -> R.raw.ru_yo
            'Ж' -> R.raw.ru_zh
            'З' -> R.raw.ru_z
            'И' -> R.raw.ru_i
            'Й' -> R.raw.ru_j
            'К' -> R.raw.ru_k
            'Л' -> R.raw.ru_l
            'М' -> R.raw.ru_m
            'Н' -> R.raw.ru_n
            'О' -> R.raw.ru_o
            'П' -> R.raw.ru_p
            'Р' -> R.raw.ru_r
            'С' -> R.raw.ru_s
            'Т' -> R.raw.ru_t
            'У' -> R.raw.ru_u
            'Ф' -> R.raw.ru_f
            'Х' -> R.raw.ru_h
            'Ц' -> R.raw.ru_c
            'Ч' -> R.raw.ru_ch
            'Ш' -> R.raw.ru_sh
            'Щ' -> R.raw.ru_sch
            'Ъ' -> R.raw.ru_tvyordy_znak
            'Ы' -> R.raw.ru_y
            'Ь' -> R.raw.ru_myagky_znak
            'Э' -> R.raw.ru_ee
            'Ю' -> R.raw.ru_yu
            'Я' -> R.raw.ru_ya
            else -> null
        }

        Alphabet.LATIN -> when (letter) {
            'A' -> R.raw.en_a
            'B' -> R.raw.en_b
            'C' -> R.raw.en_c
            'D' -> R.raw.en_d
            'E' -> R.raw.en_e
            'F' -> R.raw.en_f
            'G' -> R.raw.en_g
            'H' -> R.raw.en_h
            'I' -> R.raw.en_i
            'J' -> R.raw.en_j
            'K' -> R.raw.en_k
            'L' -> R.raw.en_l
            'M' -> R.raw.en_m
            'N' -> R.raw.en_n
            'O' -> R.raw.en_o
            'P' -> R.raw.en_p
            'Q' -> R.raw.en_q
            'R' -> R.raw.en_r
            'S' -> R.raw.en_s
            'T' -> R.raw.en_t
            'U' -> R.raw.en_u
            'V' -> R.raw.en_v
            'W' -> R.raw.en_w
            'X' -> R.raw.en_x
            'Y' -> R.raw.en_y
            'Z' -> R.raw.en_z
            else -> null
        }
    }

    private companion object {

        const val TAG = "LettersSpeaker"
    }
}
