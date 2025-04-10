package com.example.shadowsofbrimstonecompanion.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.shadowsofbrimstonecompanion.data.converters.AppTypeConverters

@Entity(tableName = "item_definitions")
@TypeConverters(AppTypeConverters::class)
data class ItemDefinition(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String,
    val type: String,
    val keywords: List<String> = emptyList(),
    val statModifiers: Map<String, Int> = emptyMap(),
    val anvilWeight: Int = 0, // Number of anvil symbols (0 = no weight)
    val darkStoneCount: Int = 0,
    val equipSlot: String? = null,
    val usageEffect: String? = null,
    val upgradeSlots: Int = 0,
    val goldValue: Int = 0,
    val sideBagType: String? = null,
    val isPersonalItem: Boolean = false,
    val imageResourceName: String? = null,
    // New container-related properties
    val isContainer: Boolean = false,
    val containerCapacity: Int = 0,
    val containerAcceptedTypes: List<String> = emptyList()
)