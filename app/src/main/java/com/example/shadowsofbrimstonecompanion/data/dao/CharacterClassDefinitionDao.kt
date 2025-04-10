package com.example.shadowsofbrimstonecompanion.data.dao

import androidx.room.*
import com.example.shadowsofbrimstonecompanion.data.entity.CharacterClassDefinition
import kotlinx.coroutines.flow.Flow

@Dao
interface CharacterClassDefinitionDao {
    @Insert
    suspend fun insert(characterClass: CharacterClassDefinition): Long

    @Insert
    suspend fun insertAll(classes: List<CharacterClassDefinition>)

    @Update
    suspend fun update(characterClass: CharacterClassDefinition)

    @Delete
    suspend fun delete(characterClass: CharacterClassDefinition)

    @Query("DELETE FROM character_class_definitions")
    suspend fun deleteAll()

    @Query("SELECT * FROM character_class_definitions ORDER BY name ASC")
    fun getAllClassDefinitions(): Flow<List<CharacterClassDefinition>>

    @Query("SELECT COUNT(*) FROM character_class_definitions")
    suspend fun getClassCount(): Int
}