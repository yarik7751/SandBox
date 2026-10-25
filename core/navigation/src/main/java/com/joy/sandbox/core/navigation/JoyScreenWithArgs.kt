package com.joy.sandbox.core.navigation

/**
 * Базовый контракт экрана с аргументами в route.
 *
 * Аргументы передаются query-параметрами: конкретный URL для навигации
 * строится фабричным методом (например, `create(id)`), а destination
 * регистрируется по [routePattern] — по ней NavHost поднимает navArgument
 * для каждого `{placeholder}`. Dagger-модуль фичи регистрирует
 * ScreenContentProvider в мапе с ключом `@ScreenKey(ROUTE_PATTERN)`.
 *
 * Чтение аргумента на экране — хелперами [currentNavLongArgument] /
 * [currentNavStringArgument] из этого же пакета.
 *
 * Пример:
 * ```
 * class ClientCardJoyScreen private constructor(val clientId: Long) : JoyScreenWithArgs() {
 *
 *     override val route: String get() = "$BASE?$ARG_CLIENT_ID=$clientId"
 *     override val routePattern: String get() = ROUTE_PATTERN
 *
 *     companion object {
 *         const val BASE = "client_card"
 *         const val ROUTE_PATTERN = "client_card?clientId={clientId}"
 *         const val ARG_CLIENT_ID = "clientId"
 *
 *         fun create(clientId: Long) = ClientCardJoyScreen(clientId)
 *     }
 * }
 * ```
 */
abstract class JoyScreenWithArgs : JoyScreen<NoArgs> {

    override val args: NoArgs
        get() = NoArgs

    /** Паттерн route с `{placeholder}` для регистрации destination в графе. */
    abstract val routePattern: String
}
