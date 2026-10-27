package com.joy.sandbox.di

import android.content.Context
import com.joy.sandbox.MainActivity
import com.joy.sandbox.application.SandBoxApplication
import com.joy.sandbox.di.module.CommonModule
import com.joy.sandbox.di.module.DateTimeModule
import com.joy.sandbox.di.module.SnackBarModule
import com.joy.sandbox.di.module.network.CommonNetworkModule
import com.joy.sandbox.di.module.navigation.NavigationModule
import com.joy.sandbox.feature.home.impl.HomeModule
import com.joy.sandbox.feature.start.impl.StartModule
import com.joy.sandbox.feature.websocket.impl.WebSocketModule
import com.joy.sandbox.feature.websocket.impl.data.db.ProductsDatabaseModule
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        CommonModule::class,
        DateTimeModule::class,
        NavigationModule::class,
        CommonNetworkModule::class,
        StartModule::class,
        HomeModule::class,
        WebSocketModule::class,
        ProductsDatabaseModule::class,
        SnackBarModule::class,
    ],
)
interface AppComponent {
    fun inject(app: SandBoxApplication)
    fun inject(activity: MainActivity)

    @Component.Builder
    interface Builder {
        fun applicationContext(@BindsInstance context: Context): Builder
        fun build(): AppComponent
    }
}
