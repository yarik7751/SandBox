package com.joy.sandbox.feature.dungeon.impl.presentation.game

import androidx.compose.runtime.Composable
import com.joy.sandbox.core.navigation.JoyScreen
import com.joy.sandbox.core.navigation.ScreenContentProvider
import com.joy.sandbox.core.navigation.router.JoyRouter
import com.joy.sandbox.core.ui.ViewModelFactory
import com.joy.sandbox.feature.dungeon.api.DungeonGameJoyScreen

class DungeonGameScreenProvider : ScreenContentProvider {

    override val screen: JoyScreen<*> = DungeonGameJoyScreen.DEFAULT

    @Composable
    override fun Content(viewModelFactory: ViewModelFactory, router: JoyRouter) {
        DungeonGameScreen(viewModelFactory = viewModelFactory, router = router)
    }
}
