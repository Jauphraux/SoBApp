// app/src/main/java/com/example/shadowsofbrimstonecompanion/data/dao/ItemDao.kt

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
}