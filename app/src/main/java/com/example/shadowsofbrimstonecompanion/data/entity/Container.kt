package com.example.shadowsofbrimstonecompanion.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.shadowsofbrimstonecompanion.data.converters.AppTypeConverters

@Entity(
    tableName = "containers",
    foreignKeys = [
        ForeignKey(
            entity = Item::class,
            parentColumns = ["id"],
            childColumns = ["itemId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [Index("itemId")]
)
@TypeConverters(AppTypeConverters::class)
data class Container(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val itemId: Long? = null,
    val maxCapacity: Int,
    val acceptedItemTypes: List<String> = emptyList(),
    val isStash: Boolean = false,
    val name: String? = null,
    val isSystemContainer: Boolean = false
)