// app/src/main/java/com/example/shadowsofbrimstonecompanion/data/entity/ItemDefinition.kt

package com.example.shadowsofbrimstonecompanion.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

@Entity(tableName = "item_definitions")
data class ItemDefinition(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String,
    val type: String,
    val keywords: List<String> = emptyList(),
    val statModifiers: Map<String, Int> = emptyMap(),
    val weight: Int = 0,
    val darkStoneCount: Int = 0,
    val equipSlot: String? = null,
    val usageEffect: String? = null,
    val upgradeSlots: Int = 0,
    val goldValue: Int = 0,
    val sideBagType: String? = null,
    val isPersonalItem: Boolean = false,
    val imageResourceName: String? = null
)