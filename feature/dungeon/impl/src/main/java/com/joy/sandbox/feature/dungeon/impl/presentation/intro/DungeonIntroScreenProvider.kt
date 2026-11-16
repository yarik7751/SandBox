package com.joy.sandbox.feature.dungeon.impl.presentation.intro

import androidx.compose.runtime.Composable
import com.joy.sandbox.core.navigation.JoyScreen
import com.joy.sandbox.core.navigation.ScreenContentProvider
import com.joy.sandbox.core.navigation.router.JoyRouter
import com.joy.sandbox.core.ui.ViewModelFactory
import com.joy.sandbox.feature.dungeon.api.DungeonIntroJoyScreen

class DungeonIntroScreenProvider : ScreenContentProvider {

    override val screen: JoyScreen<*> = DungeonIntroJoyScreen.DEFAULT

    @Composable
    override fun Content(viewModelFactory: ViewModelFactory, router: JoyRouter) {
        DungeonIntroScreen(viewModelFactory = viewModelFactory, router = router)
    }
}
