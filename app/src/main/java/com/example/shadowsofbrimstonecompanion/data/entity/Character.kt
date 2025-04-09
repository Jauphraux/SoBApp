package com.example.shadowsofbrimstonecompanion.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "characters")
data class Character(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val characterClass: String,
    val level: Int = 1,
    val health: Int,
    val maxHealth: Int,
    val sanity: Int,
    val maxSanity: Int,
    val xp: Int = 0,
    val gold: Int = 0,
    val darkstone: Int = 0,
    val initiative: Int = 0,  // Add these new fields
    val combat: Int = 0,
    val defense: Int = 0
)