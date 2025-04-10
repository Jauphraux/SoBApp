package com.example.shadowsofbrimstonecompanion.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "items",
    foreignKeys = [
        ForeignKey(
            entity = Character::class,
            parentColumns = ["id"],
            childColumns = ["characterId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("characterId"), Index("itemDefinitionId")]
)
data class Item(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val characterId: Long,
    val itemDefinitionId: Long,
    val quantity: Int = 1,
    val equipped: Boolean = false,
    val notes: String = "",
    val containerId: Long? = null // Added this field for container functionality
)