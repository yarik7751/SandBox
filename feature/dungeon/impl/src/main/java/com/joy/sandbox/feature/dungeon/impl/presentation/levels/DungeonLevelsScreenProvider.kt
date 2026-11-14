package com.joy.sandbox.feature.dungeon.impl.presentation.levels

import androidx.compose.runtime.Composable
import com.joy.sandbox.core.navigation.JoyScreen
import com.joy.sandbox.core.navigation.ScreenContentProvider
import com.joy.sandbox.core.navigation.router.JoyRouter
import com.joy.sandbox.core.ui.ViewModelFactory
import com.joy.sandbox.feature.dungeon.api.DungeonLevelsJoyScreen

class DungeonLevelsScreenProvider : ScreenContentProvider {

    override val screen: JoyScreen<*> = DungeonLevelsJoyScreen

    @Composable
    override fun Content(viewModelFactory: ViewModelFactory, router: JoyRouter) {
        DungeonLevelsScreen(viewModelFactory = viewModelFactory, router = router)
    }
}
