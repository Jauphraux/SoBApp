// app/src/main/java/com/example/shadowsofbrimstonecompanion/data/entity/ItemWithDefinition.kt

package com.example.shadowsofbrimstonecompanion.data.entity

import androidx.room.Embedded
import androidx.room.Relation

data class ItemWithDefinition(
    @Embedded val item: Item,
    @Relation(
        parentColumn = "itemDefinitionId",
        entityColumn = "id"
    )
    val definition: ItemDefinition
)