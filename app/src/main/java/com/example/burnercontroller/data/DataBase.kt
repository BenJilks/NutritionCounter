package com.example.burnercontroller.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Product::class, OpenFoodFactsEntry::class], version = 6, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun products(): ProductDao
    abstract fun foodData(): OpenFoodFactsEntryDao
}
