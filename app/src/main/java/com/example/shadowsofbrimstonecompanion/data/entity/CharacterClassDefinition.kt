package com.example.shadowsofbrimstonecompanion.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

@Entity(tableName = "character_class_definitions")
data class CharacterClassDefinition(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String,
    val startingHealth: Int,
    val startingSanity: Int,
    val startingAttributes: Map<String, Int>,
    val imageResourceName: String? = null
)