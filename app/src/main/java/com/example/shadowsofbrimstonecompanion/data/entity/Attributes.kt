package com.example.shadowsofbrimstonecompanion.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "attributes",
    foreignKeys = [ForeignKey(
        entity = Character::class,
        parentColumns = ["id"],
        childColumns = ["characterId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class Attributes(
    @PrimaryKey val characterId: Long,
    val agility: Int,
    val strength: Int,
    val lore: Int,
    val luck: Int,
    val cunning: Int,
    val spirit: Int
)
