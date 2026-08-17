package com.sipos.kebabsk.feature.menu.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        MenuCatalogEntity::class,
        MenuCategoryCacheEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class SiposDatabase : RoomDatabase() {
    abstract fun menuCatalogDao(): MenuCatalogDao
}
