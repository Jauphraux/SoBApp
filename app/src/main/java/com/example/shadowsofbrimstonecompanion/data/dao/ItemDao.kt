package com.example.shadowsofbrimstonecompanion.data.dao

import androidx.room.*
import com.example.shadowsofbrimstonecompanion.data.entity.Item
import com.example.shadowsofbrimstonecompanion.data.entity.ItemWithDefinition
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDao {
    @Insert
    suspend fun insert(item: Item): Long

    @Update
    suspend fun update(item: Item)

    @Delete
    suspend fun delete(item: Item)

    @Query("SELECT * FROM items WHERE characterId = :characterId")
    fun getItemsForCharacter(characterId: Long): Flow<List<Item>>

    @Transaction
    @Query("SELECT * FROM items WHERE characterId = :characterId")
    fun getItemsWithDefinitionsForCharacter(characterId: Long): Flow<List<ItemWithDefinition>>

    // New method to get a specific item by ID
    @Query("SELECT * FROM items WHERE id = :itemId")
    suspend fun getItemById(itemId: Long): Item?

    // New query to get items in a specific container
    @Query("SELECT * FROM items WHERE containerId = :containerId")
    fun getItemsInContainer(containerId: Long): Flow<List<Item>>

    // New query to get loose items (not in any container)
    @Query("SELECT * FROM items WHERE characterId = :characterId AND containerId IS NULL")
    fun getLooseItemsForCharacter(characterId: Long): Flow<List<Item>>
}