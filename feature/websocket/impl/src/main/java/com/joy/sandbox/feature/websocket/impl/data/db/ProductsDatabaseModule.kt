package com.joy.sandbox.feature.websocket.impl.data.db

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class ProductsDatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(context: Context): SandBoxAppDatabase {
        return Room.databaseBuilder(
            context,
            SandBoxAppDatabase::class.java,
            "sandbox_app.db",
        ).build()
    }

    @Provides
    fun provideProductsDao(database: SandBoxAppDatabase): ProductsDao {
        return database.productsDao()
    }
}
