package com.joy.sandbox.feature.letters.api

import com.joy.sandbox.core.navigation.JoyScreenWithArgs
import com.joy.sandbox.feature.letters.api.model.Alphabet
import com.joy.sandbox.feature.letters.api.model.LettersOrder

/**
 * Ночной режим показа букв: тот же показ, что и [LettersJoyScreen],
 * но без звука, в тёплой тусклой палитре, с замедляющимся темпом
 * и затуханием экрана по таймеру.
 *
 * Алфавит, порядок и длительность таймера передаются string-аргументами
 * route (см. [com.joy.sandbox.core.navigation.ScreenContentProvider.stringArgs]).
 * Длительность — в минутах, [INFINITE_MINUTES] означает бесконечный сеанс.
 */
class NightLettersJoyScreen private constructor(
    val alphabet: Alphabet,
    val order: LettersOrder,
    val minutes: Int,
) : JoyScreenWithArgs() {

    override val route: String
        get() = "$BASE?$ARG_ALPHABET=${alphabet.name}&$ARG_ORDER=${order.name}&$ARG_MINUTES=$minutes"

    override val routePattern: String
        get() = ROUTE_PATTERN

    companion object {

        /** Значение [minutes] для бесконечного сеанса. */
        const val INFINITE_MINUTES = -1

        const val BASE = "letters_night"
        const val ROUTE_PATTERN = "letters_night?alphabet={alphabet}&order={order}&minutes={minutes}"
        const val ARG_ALPHABET = "alphabet"
        const val ARG_ORDER = "order"
        const val ARG_MINUTES = "minutes"

        /** Значение для регистрации провайдера в Dagger-мапе (аргументы не важны). */
        val DEFAULT: NightLettersJoyScreen =
            create(Alphabet.CYRILLIC, LettersOrder.ALPHABETICAL, DEFAULT_MINUTES)

        /** Длительность по умолчанию, если в route нет аргумента. */
        const val DEFAULT_MINUTES = 20

        fun create(alphabet: Alphabet, order: LettersOrder, minutes: Int) =
            NightLettersJoyScreen(alphabet, order, minutes)
    }
}
