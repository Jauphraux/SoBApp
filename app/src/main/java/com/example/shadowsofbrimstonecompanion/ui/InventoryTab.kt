package com.example.shadowsofbrimstonecompanion.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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

/**
 * Represents a group of items with the same item definition
 */
data class GroupedItem(
    val definition: ItemDefinition,
    val item: Item,
    val individualItemIds: List<Long>
)

@Composable
fun InventoryTab(
    itemsWithDefinitions: List<ItemWithDefinition>,
    allItemDefinitions: List<ItemDefinition>,
    onToggleEquipped: (Item) -> Unit,
    onDeleteItem: (Item) -> Unit,
    onAddItem: (Long, Int, String) -> Unit,
    onSellItem: (Item, Int) -> Unit,
    onUseAsContainer: ((Item) -> Unit)? = null,  // Added parameter for container functionality
    currentEncumbrance: Int = 0,
    maxEncumbrance: Int = 0,
    errorMessage: String? = null,
    onErrorMessageShown: () -> Unit = {}
) {
    var showAddDialog by remember { mutableStateOf(false) }

    // Show error message as a snackbar if present
    var showSnackbar by remember { mutableStateOf(false) }

    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            showSnackbar = true
        }
    }

    if (showSnackbar && errorMessage != null) {
        Snackbar(
            modifier = Modifier.padding(16.dp),
            action = {
                TextButton(onClick = {
                    showSnackbar = false
                    onErrorMessageShown()
                }) {
                    Text("Dismiss")
                }
            },
            dismissAction = {
                IconButton(onClick = {
                    showSnackbar = false
                    onErrorMessageShown()
                }) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Dismiss"
                    )
                }
            }
        ) {
            Text(errorMessage)
        }
    }

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
            // Group items by definition ID
            val groupedItems = itemsWithDefinitions
                .groupBy { it.item.itemDefinitionId }
                .map { (defId, items) ->
                    // Find the first item and its definition
                    val firstItem = items.first()
                    // Calculate total quantity across all items of this type
                    val totalQuantity = items.sumOf { it.item.quantity }
                    // Create a grouped representation
                    GroupedItem(
                        definition = firstItem.definition,
                        // Use the first item as representative, with updated quantity
                        item = firstItem.item.copy(quantity = totalQuantity),
                        // Keep track of all individual item IDs
                        individualItemIds = items.map { it.item.id }
                    )
                }
                .sortedBy { it.definition.name } // Sort by item name for consistency

            LazyColumn {
                items(groupedItems) { groupedItem ->
                    ItemCard(
                        itemWithDefinition = ItemWithDefinition(
                            item = groupedItem.item,
                            definition = groupedItem.definition
                        ),
                        onToggleEquipped = { onToggleEquipped(groupedItem.item) },
                        onDelete = { onDeleteItem(groupedItem.item) },
                        onSell = { percentage -> onSellItem(groupedItem.item, percentage) },
                        onUseAsContainer = if (onUseAsContainer != null && groupedItem.definition.isContainer) {
                            { onUseAsContainer(groupedItem.item) }
                        } else null,
                        isStackedItem = groupedItem.individualItemIds.size > 1
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
            },
            currentEncumbrance = currentEncumbrance,
            maxEncumbrance = maxEncumbrance
        )
    }
}

@Composable
fun ItemCard(
    itemWithDefinition: ItemWithDefinition,
    onToggleEquipped: () -> Unit,
    onDelete: () -> Unit,
    onSell: (Int) -> Unit,
    onUseAsContainer: (() -> Unit)? = null,  // Added parameter for container functionality
    isStackedItem: Boolean = false
) {
    var expanded by remember { mutableStateOf(false) }
    var showSellDialog by remember { mutableStateOf(false) }
    val item = itemWithDefinition.item
    val definition = itemWithDefinition.definition

    // Check if this item can be a container
    val isContainerCapable = definition.isContainer
    val isDarkstone = definition.type == "Dark Stone"

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when {
                item.equipped -> MaterialTheme.colorScheme.primaryContainer
                isDarkstone -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Add dark stone icon if applicable
                        if (isDarkstone) {
                            Text(
                                "🌑 ", // Dark stone emoji
                                style = MaterialTheme.typography.titleMedium
                            )
                        }

                        Text(
                            text = definition.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        // Add quantity badge for stacked items
                        if (item.quantity > 1 || isStackedItem) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                modifier = Modifier.padding(4.dp)
                            ) {
                                Text(
                                    text = "×${item.quantity}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Text(
                        text = "Type: ${definition.type}",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    // Show container indicator if applicable
                    if (isContainerCapable) {
                        Text(
                            text = "Container",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Show if dark stone is unprotected
                    if (isDarkstone) {
                        Text(
                            text = "Unprotected - may cause corruption",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
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

                // Add this code to show anvil weight
                if (definition.anvilWeight > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Weight: ")
                        // Display anvil symbols based on weight
                        repeat(definition.anvilWeight) {
                            Text("⚒️", // Anvil emoji
                                modifier = Modifier.padding(horizontal = 2.dp))
                        }
                        // Show total weight if quantity > 1
                        if (item.quantity > 1) {
                            Text(" × ${item.quantity} = ${definition.anvilWeight * item.quantity} total",
                                style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                if (definition.goldValue > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Value: ${definition.goldValue} gold" +
                            if (item.quantity > 1) " (${definition.goldValue * item.quantity} total)" else "")
                }

                if (item.notes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Notes: ${item.notes}")
                }

                // Add Container section if applicable
                if (isContainerCapable && onUseAsContainer != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Button(
                            onClick = onUseAsContainer,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        ) {
                            Icon(
                                Icons.Default.Inventory2,
                                contentDescription = "Use as Container"
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Use as Container")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Only show buttons if it's not a personal item
                if (!definition.isPersonalItem) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = { showSellDialog = true },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text("Sell")
                        }

                        Spacer(modifier = Modifier.width(8.dp))

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

    if (showSellDialog) {
        SellItemDialog(
            item = item,
            definition = definition,
            onDismiss = { showSellDialog = false },
            onSell = { percentage ->
                onSell(percentage)
                showSellDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddItemDialog(
    itemDefinitions: List<ItemDefinition>,
    onDismiss: () -> Unit,
    onAddItem: (Long, Int, String) -> Unit,
    currentEncumbrance: Int = 0,
    maxEncumbrance: Int = 0
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

                    // Add equipment slot information
                    if (selectedItem.equipSlot != null) {
                        Text(
                            text = "Equip Slot: ${selectedItem.equipSlot}" +
                                    if (selectedItem.equipSlot == "Two-Handed") " (requires both hands)" else "",
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Show container info if applicable
                    if (selectedItem.isContainer) {
                        Text(
                            text = "Container: Capacity ${selectedItem.containerCapacity} items",
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (selectedItem.containerAcceptedTypes.isNotEmpty()) {
                            Text(
                                text = "Accepts: ${selectedItem.containerAcceptedTypes.joinToString(", ")}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

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

                        // Add this code to show encumbrance warning
                        if (selectedItem.anvilWeight > 0 && maxEncumbrance > 0) {
                            val quantityNum = quantity.toIntOrNull() ?: 0
                            val additionalWeight = selectedItem.anvilWeight * quantityNum
                            val newTotalWeight = currentEncumbrance + additionalWeight

                            if (newTotalWeight > maxEncumbrance) {
                                Text(
                                    "Warning: Adding this will make you overencumbered! " +
                                            "($newTotalWeight/$maxEncumbrance anvils)",
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            } else if (additionalWeight > 0) {
                                Text(
                                    "Encumbrance after adding: $newTotalWeight/$maxEncumbrance anvils",
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
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

@Composable
fun SellItemDialog(
    item: Item,
    definition: ItemDefinition,
    onDismiss: () -> Unit,
    onSell: (Int) -> Unit
) {
    val itemValue = definition.goldValue * item.quantity

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Sell Item") },
        text = {
            Column {
                Text(
                    "Item: ${definition.name}" +
                            if (item.quantity > 1) " (${item.quantity})" else ""
                )

                Text("Base Value: ${definition.goldValue} gold per item")

                if (item.quantity > 1) {
                    Text("Total Value: $itemValue gold")
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Select how much to sell for:")

                // Move buttons here inside the content
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(onClick = { onSell(0) }) {
                        Text("0%\n(0 gold)")
                    }

                    Button(onClick = { onSell(50) }) {
                        Text("50%\n(${itemValue / 2} gold)")
                    }

                    Button(onClick = { onSell(100) }) {
                        Text("100%\n($itemValue gold)")
                    }
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        confirmButton = { }
    )
}