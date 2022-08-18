/*
 * Copyright (c) 2022, Ben Jilks <benjyjilks@gmail.com>
 *
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.example.burnercontroller.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Product::class, OpenFoodFactsEntry::class], version = 6, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun products(): ProductDao
    abstract fun foodData(): OpenFoodFactsEntryDao
}
