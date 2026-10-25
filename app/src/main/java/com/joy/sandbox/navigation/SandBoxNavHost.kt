package com.joy.sandbox.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.joy.sandbox.core.navigation.ScreenContentProvider
import com.joy.sandbox.core.navigation.router.JoyRouter
import com.joy.sandbox.core.ui.ViewModelFactory
import com.joy.sandbox.feature.start.api.StartJoyScreen

/**
 * Граф навигации приложения, собираемый из Dagger-мапы провайдеров контента
 * (route → ScreenContentProvider), которую наполняют Dagger-модули фич.
 *
 * :app не знает о конкретных экранах — добавление фичи не требует правок здесь.
 * Start — start destination; навигация идёт только через [JoyRouter],
 * подключаемый к NavController здесь же.
 *
 * Route вида "screen?arg={arg}" регистрируется как destination с navArgument.
 * Поддерживаются только Long-аргументы — этого достаточно для id сущностей;
 * другие типы при необходимости добавим явно.
 */
@Composable
fun SandBoxNavHost(
    router: JoyRouter,
    viewModelFactory: ViewModelFactory,
    contentProviders: Map<String, ScreenContentProvider>,
) {
    val navController = rememberNavController()

    DisposableEffect(navController) {
        router.attach(navController, contentProviders.mapValues { it.value.screen })
        onDispose { router.detach() }
    }

    NavHost(
        navController = navController,
        startDestination = StartJoyScreen.route,
    ) {
        contentProviders.forEach { (route, provider) ->
            val argNames = "\\{(\\w+)\\}".toRegex().findAll(route).map { it.groupValues[1] }.toList()
            composable(
                route = route,
                arguments = argNames.map { name ->
                    navArgument(name) { type = NavType.LongType }
                },
            ) {
                provider.Content(
                    viewModelFactory = viewModelFactory,
                    router = router,
                )
            }
        }
    }
}
