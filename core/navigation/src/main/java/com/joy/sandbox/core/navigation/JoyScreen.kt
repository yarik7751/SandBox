package com.joy.sandbox.core.navigation

import android.os.Parcelable

/**
 * Контракт экрана приложения.
 *
 * [route] — идентификатор destination в графе Compose Navigation. Для экранов
 * без аргументов это просто константа; для экранов с аргументами конкретный
 * URL строится через фабричный метод, а в граф регистрируется [routePattern]
 * вида "screen?arg={arg}" (см. [JoyScreenWithArgs]).
 */
interface JoyScreen<A : Parcelable> {

    val args: A

    val route: String
}
