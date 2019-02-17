package com.youknow.everydayphoto.data.source.local

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.content.Context
import com.youknow.everydayphoto.data.model.Model
import com.youknow.everydayphoto.data.model.Photo

@Database(entities = [Model::class, Photo::class], version = 1)
abstract class AppDatabase : RoomDatabase() {

    abstract fun modelDao(): ModelDao

    abstract fun photoDao(): PhotoDao

    companion object {
        private var INSTANCE: AppDatabase? = null

        fun getAppDataBase(context: Context): AppDatabase? {
            if (INSTANCE == null) {
                synchronized(AppDatabase::class) {
                    INSTANCE = Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "myDB").build()
                }
            }

            return INSTANCE
        }
    }
}
