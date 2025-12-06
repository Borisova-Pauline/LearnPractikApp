package com.tomli.learnpractikapp.database

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Collections::class], version = 1,
    exportSchema = true, autoMigrations = [])
abstract class CollectDB : RoomDatabase() {
    abstract val daoData: DaoData
    companion object{
        fun createDB(context: Context): CollectDB{
            return Room.databaseBuilder(context, CollectDB::class.java, "colldb.db")//.fallbackToDestructiveMigration()
                .createFromAsset("colldb.db").build()
        }
    }
}