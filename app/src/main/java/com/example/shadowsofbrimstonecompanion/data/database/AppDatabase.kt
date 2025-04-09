package com.example.shadowsofbrimstonecompanion.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.shadowsofbrimstonecompanion.data.converters.AppTypeConverters
import com.example.shadowsofbrimstonecompanion.data.dao.AttributesDao
import com.example.shadowsofbrimstonecompanion.data.dao.CharacterDao
import com.example.shadowsofbrimstonecompanion.data.dao.ItemDao
import com.example.shadowsofbrimstonecompanion.data.dao.SkillDao
import com.example.shadowsofbrimstonecompanion.data.dao.CharacterClassDefinitionDao
import com.example.shadowsofbrimstonecompanion.data.dao.ItemDefinitionDao
import com.example.shadowsofbrimstonecompanion.data.entity.Attributes
import com.example.shadowsofbrimstonecompanion.data.entity.Character
import com.example.shadowsofbrimstonecompanion.data.entity.Item
import com.example.shadowsofbrimstonecompanion.data.entity.Skill
import com.example.shadowsofbrimstonecompanion.data.entity.CharacterClassDefinition
import com.example.shadowsofbrimstonecompanion.data.entity.ItemDefinition

@Database(
    entities = [
        Character::class,
        Attributes::class,
        Item::class,
        Skill::class,
        CharacterClassDefinition::class,
        ItemDefinition::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(AppTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun characterDao(): CharacterDao
    abstract fun attributesDao(): AttributesDao
    abstract fun itemDao(): ItemDao
    abstract fun skillDao(): SkillDao
    abstract fun characterClassDefinitionDao(): CharacterClassDefinitionDao
    abstract fun itemDefinitionDao(): ItemDefinitionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "brimstone_database"
                )
                    .fallbackToDestructiveMigration()  // This will recreate the database if version changes
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}