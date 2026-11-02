package com.joy.sandbox.feature.letters.api

import com.joy.sandbox.core.navigation.JoyScreenWithArgs
import com.joy.sandbox.feature.letters.api.model.Alphabet
import com.joy.sandbox.feature.letters.api.model.LettersOrder

/**
 * Экран показа букв. Открывается из настроек через [replaceScreen], поэтому
 * возврат с него попадает сразу на Главный, минуя настройки.
 *
 * Алфавит и порядок передаются string-аргументами route
 * (см. [com.joy.sandbox.core.navigation.ScreenContentProvider.stringArgs]).
 */
class LettersJoyScreen private constructor(
    val alphabet: Alphabet,
    val order: LettersOrder,
) : JoyScreenWithArgs() {

    override val route: String
        get() = "$BASE?$ARG_ALPHABET=${alphabet.name}&$ARG_ORDER=${order.name}"

    override val routePattern: String
        get() = ROUTE_PATTERN

    companion object {

        const val BASE = "letters"
        const val ROUTE_PATTERN = "letters?alphabet={alphabet}&order={order}"
        const val ARG_ALPHABET = "alphabet"
        const val ARG_ORDER = "order"

        /** Значение для регистрации провайдера в Dagger-мапе (аргументы не важны). */
        val DEFAULT: LettersJoyScreen = create(Alphabet.CYRILLIC, LettersOrder.ALPHABETICAL)

        fun create(alphabet: Alphabet, order: LettersOrder) = LettersJoyScreen(alphabet, order)
    }
}
