package com.joy.sandbox.core.navigation.router

import android.os.Parcelable
import androidx.navigation.NavController
import com.joy.sandbox.core.navigation.JoyScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

/**
 * Фасад над [NavController] с контрактом навигации: [navigateTo],
 * [replaceScreen], [newRootScreen], [backTo], [exit], [newChain],
 * [newRootChain], [finishChain].
 *
 * Подключается к конкретному NavController через [attach] при создании NavHost
 * и отключается через [detach]. [screensFlow] зеркалирует back stack через
 * мапу route → [JoyScreen], переданную в [attach] (в :app её собирает
 * NavHost из Dagger-мапы ScreenContentProvider).
 *
 * Создаётся в :app через Dagger (NavigationModule).
 */
class JoyRouter : ScreensFlow, CurrentArgs {

    private val attachScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private var navController: NavController? = null
    private var screensByRoute: Map<String, JoyScreen<*>> = emptyMap()
    private var backStackCollector: Job? = null

    override val screensFlow = MutableStateFlow<List<JoyScreen<*>>>(emptyList())

    fun attach(navController: NavController, screensByRoute: Map<String, JoyScreen<*>>) {
        this.navController = navController
        this.screensByRoute = screensByRoute
        backStackCollector?.cancel()
        backStackCollector = attachScope.launch {
            navController.currentBackStack.collect { entries ->
                screensFlow.value = entries.mapNotNull { entry ->
                    screensByRoute[entry.destination.route]
                }
            }
        }
    }

    fun detach() {
        backStackCollector?.cancel()
        backStackCollector = null
        navController = null
        screensByRoute = emptyMap()
    }

    fun navigateTo(screen: JoyScreen<*>) {
        navController?.navigate(screen.route)
    }

    fun newRootScreen(screen: JoyScreen<*>) {
        navController?.navigate(screen.route) {
            popUpTo(0) { inclusive = true }
        }
    }

    fun replaceScreen(screen: JoyScreen<*>) {
        val controller = navController ?: return
        val currentRoute = controller.currentDestination?.route
        if (currentRoute != null) {
            controller.navigate(screen.route) {
                popUpTo(currentRoute) { inclusive = true }
            }
        } else {
            controller.navigate(screen.route)
        }
    }

    fun backTo(screen: JoyScreen<*>?) {
        val controller = navController ?: return
        if (screen != null) {
            controller.popBackStack(screen.route, inclusive = false)
        } else {
            controller.popBackStack(controller.graph.startDestinationId, inclusive = false)
        }
    }

    fun newChain(vararg screens: JoyScreen<*>) {
        val controller = navController ?: return
        screens.forEach { screen -> controller.navigate(screen.route) }
    }

    fun newRootChain(vararg screens: JoyScreen<*>) {
        val controller = navController ?: return
        val chain = screens.toList()
        val root = chain.firstOrNull() ?: return
        newRootScreen(root)
        chain.drop(1).forEach { screen -> controller.navigate(screen.route) }
    }

    /**
     * Выход из текущего флоу: возврат к start destination графа.
     */
    fun finishChain() {
        val controller = navController ?: return
        controller.popBackStack(controller.graph.startDestinationId, inclusive = false)
    }

    fun exit() {
        navController?.popBackStack()
    }

    /**
     * Отправляет результат предыдущему в стеке экрану (в его SavedStateHandle).
     * Принимающая сторона читает значение через [resultFlow] по тому же ключу.
     */
    fun sendResult(key: String, data: Any) {
        navController?.previousBackStackEntry?.savedStateHandle?.set(key, data)
    }

    /**
     * Поток результатов для [key], приходящих в текущий экран через [sendResult].
     */
    @Suppress("UNCHECKED_CAST")
    fun <T> resultFlow(key: String): Flow<T?> {
        val handle = navController?.currentBackStackEntry?.savedStateHandle
            ?: return flowOf(null)
        return handle.getStateFlow(key, null as T?)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <A : Parcelable> get(): A {
        return requireNotNull(screensFlow.value.lastOrNull()).args as A
    }
}
