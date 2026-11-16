package com.joy.sandbox.feature.dungeon.impl.presentation.title

import androidx.compose.runtime.Composable
import com.joy.sandbox.core.navigation.JoyScreen
import com.joy.sandbox.core.navigation.ScreenContentProvider
import com.joy.sandbox.core.navigation.router.JoyRouter
import com.joy.sandbox.core.ui.ViewModelFactory
import com.joy.sandbox.feature.dungeon.api.DungeonJoyScreen

class DungeonTitleScreenProvider : ScreenContentProvider {

    override val screen: JoyScreen<*> = DungeonJoyScreen

    @Composable
    override fun Content(viewModelFactory: ViewModelFactory, router: JoyRouter) {
        DungeonTitleScreen(viewModelFactory = viewModelFactory, router = router)
    }
}
