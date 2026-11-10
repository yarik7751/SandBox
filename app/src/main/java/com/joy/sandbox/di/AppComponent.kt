package com.joy.sandbox.di

import android.content.Context
import com.joy.sandbox.MainActivity
import com.joy.sandbox.application.SandBoxApplication
import com.joy.sandbox.di.module.CommonModule
import com.joy.sandbox.di.module.DateTimeModule
import com.joy.sandbox.di.module.SnackBarModule
import com.joy.sandbox.di.module.navigation.NavigationModule
import com.joy.sandbox.feature.dungeon.impl.DungeonModule
import com.joy.sandbox.feature.home.impl.HomeModule
import com.joy.sandbox.feature.letters.impl.LettersModule
import com.joy.sandbox.feature.start.impl.StartModule
import com.joy.sandbox.feature.websocket.impl.WebSocketModule
import com.joy.sandbox.feature.websocket.impl.di.RestOkHttpClientModule
import com.joy.sandbox.feature.websocket.impl.di.WebSocketOkHttpClientModule
import com.joy.sandbox.feature.websocket.impl.localdatasource.db.ProductsDatabaseModule
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        CommonModule::class,
        DateTimeModule::class,
        NavigationModule::class,
        StartModule::class,
        HomeModule::class,
        DungeonModule::class,
        WebSocketModule::class,
        LettersModule::class,
        ProductsDatabaseModule::class,
        WebSocketOkHttpClientModule::class,
        RestOkHttpClientModule::class,
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
