package com.joy.sandbox

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.joy.sandbox.application.SandBoxApplication
import com.joy.sandbox.core.navigation.ScreenContentProvider
import com.joy.sandbox.core.navigation.router.JoyRouter
import com.joy.sandbox.core.ui.ViewModelFactory
import com.joy.sandbox.core.ui.snackbar.flow.SnackBarManagerFlow
import javax.inject.Inject

class MainActivity : ComponentActivity() {

    @Inject
    lateinit var router: JoyRouter

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    @Inject
    lateinit var screenContentProviders: Map<String, @JvmSuppressWildcards ScreenContentProvider>

    @Inject
    lateinit var snackBarManagerFlow: SnackBarManagerFlow

    override fun onCreate(savedInstanceState: Bundle?) {
        SandBoxApplication.INSTANCE.appComponent.inject(this)
        super.onCreate(savedInstanceState)

        setContent {
            SandBoxAppContent(
                router = router,
                viewModelFactory = viewModelFactory,
                contentProviders = screenContentProviders,
                snackBarManagerFlow = snackBarManagerFlow,
            )
        }
    }
}
