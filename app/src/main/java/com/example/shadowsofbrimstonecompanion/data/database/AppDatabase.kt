package com.example.shadowsofbrimstonecompanion.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.shadowsofbrimstonecompanion.data.converters.AppTypeConverters
import com.example.shadowsofbrimstonecompanion.data.dao.AttributesDao
import com.example.shadowsofbrimstonecompanion.data.dao.CharacterDao
import com.example.shadowsofbrimstonecompanion.data.dao.ItemDao
import com.example.shadowsofbrimstonecompanion.data.dao.SkillDao
import com.example.shadowsofbrimstonecompanion.data.dao.CharacterClassDefinitionDao
import com.example.shadowsofbrimstonecompanion.data.dao.ItemDefinitionDao
import com.example.shadowsofbrimstonecompanion.data.dao.ContainerDao
import com.example.shadowsofbrimstonecompanion.data.entity.Attributes
import com.example.shadowsofbrimstonecompanion.data.entity.Character
import com.example.shadowsofbrimstonecompanion.data.entity.Item
import com.example.shadowsofbrimstonecompanion.data.entity.Skill
import com.example.shadowsofbrimstonecompanion.data.entity.CharacterClassDefinition
import com.example.shadowsofbrimstonecompanion.data.entity.ItemDefinition
import com.example.shadowsofbrimstonecompanion.data.entity.Container

@Database(
    entities = [
        Character::class,
        Attributes::class,
        Item::class,
        Skill::class,
        CharacterClassDefinition::class,
        ItemDefinition::class,
        Container::class  // Add Container entity
    ],
    version = 5,  // Increment version number
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
    abstract fun containerDao(): ContainerDao

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
                    .addMigrations(MIGRATION_4_5)
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Drop and recreate the containers table with the new schema
                database.execSQL("DROP TABLE IF EXISTS containers")
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS containers (
                        isSystemContainer INTEGER NOT NULL,
                        itemId INTEGER,
                        acceptedItemTypes TEXT NOT NULL,
                        isStash INTEGER NOT NULL,
                        name TEXT,
                        maxCapacity INTEGER NOT NULL,
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        FOREIGN KEY(itemId) REFERENCES items(id) ON DELETE CASCADE ON UPDATE CASCADE
                    )
                """)
                // Create the index on itemId
                database.execSQL("CREATE INDEX index_containers_itemId ON containers(itemId)")
            }
        }
    }
}