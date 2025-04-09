package com.example.shadowsofbrimstonecompanion.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.shadowsofbrimstonecompanion.data.entity.Item
import com.example.shadowsofbrimstonecompanion.data.entity.ItemDefinition
import com.example.shadowsofbrimstonecompanion.data.entity.ItemWithDefinition

@Composable
fun InventoryTab(
    itemsWithDefinitions: List<ItemWithDefinition>,
    allItemDefinitions: List<ItemDefinition>,
    onToggleEquipped: (Item) -> Unit,
    onDeleteItem: (Item) -> Unit,
    onAddItem: (Long, Int, String) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Inventory",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Button(onClick = { showAddDialog = true }) {
                Text("Add Item")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (itemsWithDefinitions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("No items in inventory")
            }
        } else {
            LazyColumn {
                items(itemsWithDefinitions) { itemWithDef ->
                    ItemCard(
                        itemWithDefinition = itemWithDef,
                        onToggleEquipped = { onToggleEquipped(itemWithDef.item) },
                        onDelete = { onDeleteItem(itemWithDef.item) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }

    if (showAddDialog) {
        AddItemDialog(
            itemDefinitions = allItemDefinitions,
            onDismiss = { showAddDialog = false },
            onAddItem = { itemDefId, quantity, notes ->
                onAddItem(itemDefId, quantity, notes)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun ItemCard(
    itemWithDefinition: ItemWithDefinition,
    onToggleEquipped: () -> Unit,
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val item = itemWithDefinition.item
    val definition = itemWithDefinition.definition

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (item.equipped)
                MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = definition.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Type: ${definition.type}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (item.quantity > 1) {
                        Text(
                            text = "Quantity: ${item.quantity}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Row {
                    if (definition.equipSlot != null) {
                        IconButton(onClick = onToggleEquipped) {
                            Icon(
                                if (item.equipped) Icons.Default.CheckCircle else Icons.Default.CheckCircleOutline,
                                contentDescription = if (item.equipped) "Unequip" else "Equip"
                            )
                        }
                    }

                    IconButton(onClick = { expanded = !expanded }) {
                        Icon(
                            if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = if (expanded) "Collapse" else "Expand"
                        )
                    }
                }
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = definition.description,
                    style = MaterialTheme.typography.bodyMedium
                )

                if (definition.equipSlot != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Equip Slot: ${definition.equipSlot}")
                }

                if (definition.statModifiers.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Stat Modifiers:")
                    definition.statModifiers.forEach { (stat, value) ->
                        Text("• $stat: ${if (value > 0) "+$value" else value}")
                    }
                }

                if (definition.usageEffect != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Effect: ${definition.usageEffect}")
                }

                if (definition.keywords.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Keywords: ${definition.keywords.joinToString()}")
                }

                if (definition.goldValue > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Value: ${definition.goldValue} gold")
                }

                if (item.notes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Notes: ${item.notes}")
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Only show delete button if it's not a personal item
                if (!definition.isPersonalItem) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = onDelete,
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("Delete")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddItemDialog(
    itemDefinitions: List<ItemDefinition>,
    onDismiss: () -> Unit,
    onAddItem: (Long, Int, String) -> Unit
) {
    var selectedItemIndex by remember { mutableStateOf(0) }
    var quantity by remember { mutableStateOf("1") }
    var notes by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    val selectedItem = if (itemDefinitions.isNotEmpty() && selectedItemIndex < itemDefinitions.size) {
        itemDefinitions[selectedItemIndex]
    } else null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Item to Inventory") },
        text = {
            Column {
                if (itemDefinitions.isEmpty()) {
                    Text("No item definitions available.")
                } else if (selectedItem != null) {
                    // Item Selection Dropdown
                    Text("Select Item", style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(4.dp))

                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = selectedItem.name,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            label = { Text("Item") }
                        )

                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.heightIn(max = 300.dp)
                        ) {
                            itemDefinitions.forEachIndexed { index, itemDef ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = itemDef.name,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    },
                                    onClick = {
                                        selectedItemIndex = index
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Item preview
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Item Details:", style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Type: ${selectedItem.type}")

                    if (selectedItem.description.isNotBlank()) {
                        Text(selectedItem.description,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }

                    if (selectedItem.statModifiers.isNotEmpty()) {
                        val modifierText = selectedItem.statModifiers.entries.joinToString { (stat, value) ->
                            "$stat: ${if (value > 0) "+$value" else value}"
                        }
                        Text("Modifiers: $modifierText")
                    }

                    // Quantity field (not for personal items)
                    if (!selectedItem.isPersonalItem) {
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = quantity,
                            onValueChange = {
                                if (it.isEmpty() || it.toIntOrNull() != null) {
                                    quantity = it
                                }
                            },
                            label = { Text("Quantity") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Notes field
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notes") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    selectedItem?.let {
                        onAddItem(
                            it.id,
                            quantity.toIntOrNull() ?: 1,
                            notes
                        )
                    }
                },
                enabled = selectedItem != null && (quantity.toIntOrNull() ?: 0) > 0
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}