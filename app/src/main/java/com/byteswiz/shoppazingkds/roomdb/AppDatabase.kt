package com.byteswiz.shoppazingkds.roomdb

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.byteswiz.parentmodel.KDSCartItem
import com.byteswiz.parentmodel.ParentModel
import com.byteswiz.shoppazingkds.dao.OrdersDao

@Database(entities = [
    ParentModel::class,
    KDSCartItem::class
                     ]
    ,version = 1,
    exportSchema = true)
@TypeConverters(DataConverters:: class)
abstract class AppDatabase: RoomDatabase() {
    abstract fun ordersDao(): OrdersDao

    companion object{
        var INSTANCE: AppDatabase? = null

        fun getAppDataBase(context: Context): AppDatabase? {
            synchronized(AppDatabase::class){
                INSTANCE = Room.databaseBuilder(context.applicationContext,
                    AppDatabase::class.java, "myDB")
                    .build()
            }
            return INSTANCE
        }
        fun destroyDataBase(){
            INSTANCE = null
        }

    }
}