package com.example.shadowsofbrimstonecompanion.data.entity

import androidx.room.Embedded
import androidx.room.Relation

data class ContainerWithItems(
    @Embedded val container: Container,
    @Relation(
        parentColumn = "itemId",
        entityColumn = "containerId"
    )
    val items: List<Item>
)