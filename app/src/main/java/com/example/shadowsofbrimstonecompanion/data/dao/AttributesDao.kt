package com.example.shadowsofbrimstonecompanion.data.dao

import androidx.room.*
import com.example.shadowsofbrimstonecompanion.data.entity.Attributes
import kotlinx.coroutines.flow.Flow

@Dao
interface AttributesDao {
    @Insert
    suspend fun insert(attributes: Attributes)

    @Update
    suspend fun update(attributes: Attributes)

    @Query("SELECT * FROM attributes WHERE characterId = :characterId")
    fun getAttributesForCharacter(characterId: Long): Flow<Attributes?>
}
