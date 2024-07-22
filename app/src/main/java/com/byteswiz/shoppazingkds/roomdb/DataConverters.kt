package com.byteswiz.shoppazingkds.roomdb

import androidx.room.TypeConverter
import com.byteswiz.parentmodel.KDSCartItem
import com.byteswiz.parentmodel.KDSModifiersModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

class DataConverters {
    @TypeConverter
    fun fromChildrenOrderList(itmList: List<KDSCartItem?>?): String? {
        if (itmList == null) {
            return null
        }
        val gson = Gson()
        val type: Type = object : TypeToken<List<KDSCartItem?>?>() {}.type
        return gson.toJson(itmList, type)
    }

    @TypeConverter
    fun toChildrenOrderList(itmList: String?): List<KDSCartItem>? {
        if (itmList == null) {
            return null
        }
        val gson = Gson()
        val type: Type = object : TypeToken<List<KDSCartItem?>?>() {}.type
        return gson.fromJson<List<KDSCartItem>>(itmList, type)
    }


    @TypeConverter
    fun fromModifierList(itmList: List<KDSModifiersModel?>?): String? {
        if (itmList == null) {
            return null
        }
        val gson = Gson()
        val type: Type = object : TypeToken<List<KDSModifiersModel?>?>() {}.type
        return gson.toJson(itmList, type)
    }

    @TypeConverter
    fun toModifierList(itmList: String?): List<KDSModifiersModel>? {
        if (itmList == null) {
            return null
        }
        val gson = Gson()
        val type: Type = object : TypeToken<List<KDSModifiersModel?>?>() {}.type
        return gson.fromJson<List<KDSModifiersModel>>(itmList, type)
    }
}