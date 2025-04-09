// app/src/main/java/com/example/shadowsofbrimstonecompanion/data/dao/ItemDefinitionDao.kt

package com.example.shadowsofbrimstonecompanion.data.dao

import androidx.room.*
import com.example.shadowsofbrimstonecompanion.data.entity.ItemDefinition
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDefinitionDao {
    @Insert
    suspend fun insertAll(items: List<ItemDefinition>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: ItemDefinition): Long

    @Update
    suspend fun update(item: ItemDefinition)

    @Delete
    suspend fun delete(item: ItemDefinition)

    @Query("SELECT * FROM item_definitions ORDER BY name ASC")
    fun getAllItemDefinitions(): Flow<List<ItemDefinition>>

    @Query("SELECT * FROM item_definitions WHERE type = :itemType ORDER BY name ASC")
    fun getItemDefinitionsByType(itemType: String): Flow<List<ItemDefinition>>

    @Query("SELECT COUNT(*) FROM item_definitions")
    suspend fun getItemCount(): Int
}