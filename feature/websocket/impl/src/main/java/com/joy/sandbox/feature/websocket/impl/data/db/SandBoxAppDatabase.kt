package com.joy.sandbox.feature.websocket.impl.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [ProductEntity::class], version = 1, exportSchema = false)
abstract class SandBoxAppDatabase : RoomDatabase() {

    abstract fun productsDao(): ProductsDao
}
