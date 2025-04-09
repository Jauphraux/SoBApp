package com.example.shadowsofbrimstonecompanion.data.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class AppTypeConverters {
    private val gson = Gson()

    // For converting Map<String, Int> (used in both attributes and stat modifiers)
    @TypeConverter
    fun fromStringIntMap(value: Map<String, Int>?): String {
        return if (value == null) {
            "{}"
        } else {
            gson.toJson(value)
        }
    }

    @TypeConverter
    fun toStringIntMap(value: String?): Map<String, Int> {
        if (value == null || value.isEmpty()) {
            return emptyMap()
        }
        val mapType = object : TypeToken<Map<String, Int>>() {}.type
        return try {
            gson.fromJson(value, mapType) ?: emptyMap()
        } catch (e: Exception) {
            emptyMap()
        }
    }

    // For converting List<String> (used in keywords)
    @TypeConverter
    fun fromStringList(value: List<String>?): String {
        return if (value == null) {
            "[]"
        } else {
            gson.toJson(value)
        }
    }

    @TypeConverter
    fun toStringList(value: String?): List<String> {
        if (value == null || value.isEmpty()) {
            return emptyList()
        }
        val listType = object : TypeToken<List<String>>() {}.type
        return try {
            gson.fromJson(value, listType) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}