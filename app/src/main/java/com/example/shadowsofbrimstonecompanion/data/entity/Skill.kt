package com.example.shadowsofbrimstonecompanion.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "skills",
    foreignKeys = [
        ForeignKey(
            entity = Character::class,
            parentColumns = ["id"],
            childColumns = ["characterId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("characterId")]
)
data class Skill(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val characterId: Long,
    val name: String,
    val level: Int = 1,
    val description: String = ""
)