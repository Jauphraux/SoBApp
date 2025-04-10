package com.example.shadowsofbrimstonecompanion.data.dao

import androidx.room.*
import com.example.shadowsofbrimstonecompanion.data.entity.Container
import com.example.shadowsofbrimstonecompanion.data.entity.ContainerWithItems
import kotlinx.coroutines.flow.Flow

@Dao
interface ContainerDao {
    @Insert
    suspend fun insert(container: Container)

    @Update
    suspend fun update(container: Container)

    @Delete
    suspend fun delete(container: Container)

    @Transaction
    @Query("SELECT * FROM containers WHERE itemId = :containerId")
    fun getContainerWithItems(containerId: Long): Flow<ContainerWithItems?>

    @Transaction
    @Query("SELECT * FROM containers WHERE isStash = 1")
    fun getStashes(): Flow<List<ContainerWithItems>>

    @Transaction
    @Query("SELECT * FROM containers WHERE isStash = 0 AND itemId IN (SELECT id FROM items WHERE characterId = :characterId)")
    fun getContainersForCharacter(characterId: Long): Flow<List<ContainerWithItems>>
}