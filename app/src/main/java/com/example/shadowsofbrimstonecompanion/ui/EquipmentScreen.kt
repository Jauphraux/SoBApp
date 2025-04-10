package com.example.shadowsofbrimstonecompanion.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.shadowsofbrimstonecompanion.data.entity.Item
import com.example.shadowsofbrimstonecompanion.data.entity.ItemWithDefinition

@Composable
fun EquipmentScreen(
    itemsWithDefinitions: List<ItemWithDefinition>,
    allItems: List<ItemWithDefinition>,
    onToggleEquipped: (Item) -> Unit,
    errorMessage: String? = null,
    onErrorMessageShown: () -> Unit = {}
) {
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
            }
        ) {
            Text(errorMessage)
        }
    }

    // Get equipped items for each slot
    val headSlot = itemsWithDefinitions.find { it.definition.equipSlot == "Head" && it.item.equipped }
    val bodySlot = itemsWithDefinitions.find { it.definition.equipSlot == "Body" && it.item.equipped }
    val handsSlot = itemsWithDefinitions.find { it.definition.equipSlot == "Hands" && it.item.equipped }
    val feetSlot = itemsWithDefinitions.find { it.definition.equipSlot == "Feet" && it.item.equipped }
    val accessorySlot = itemsWithDefinitions.find { it.definition.equipSlot == "Accessory" && it.item.equipped }

    // Get hand slots (either one 2-handed or up to 2 1-handed)
    val twoHandedItem = itemsWithDefinitions.find { it.definition.equipSlot == "Two-Handed" && it.item.equipped }
    val handItems = if (twoHandedItem == null) {
        itemsWithDefinitions.filter { it.definition.equipSlot == "Hand" && it.item.equipped }
    } else {
        listOf(twoHandedItem)
    }

    val leftHandItem = if (twoHandedItem != null) twoHandedItem else handItems.getOrNull(0)
    val rightHandItem = if (twoHandedItem != null) twoHandedItem else handItems.getOrNull(1)

    // State for dialogs
    var showSlotDialog by remember { mutableStateOf(false) }
    var selectedSlot by remember { mutableStateOf("") }
    var currentEquippedItem by remember<MutableState<ItemWithDefinition?>> { mutableStateOf(null) }
    var availableItems by remember { mutableStateOf(listOf<ItemWithDefinition>()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            "Equipment",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Character silhouette with equipment slots
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.75f)
                .border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                .padding(16.dp)
        ) {
            // Head slot
            EquipmentSlot(
                name = "Head",
                item = headSlot,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .size(80.dp, 60.dp)
                    .offset(y = 5.dp),
                onClick = {
                    selectedSlot = "Head"
                    currentEquippedItem = headSlot
                    availableItems = allItems.filter { it.definition.equipSlot == "Head" }
                    showSlotDialog = true
                }
            )

            // Body slot
            EquipmentSlot(
                name = "Body",
                item = bodySlot,
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(120.dp, 140.dp)
                    .offset(y = 20.dp),
                onClick = {
                    selectedSlot = "Body"
                    currentEquippedItem = bodySlot
                    availableItems = allItems.filter { it.definition.equipSlot == "Body" }
                    showSlotDialog = true
                }
            )

            // Left hand slot
            EquipmentSlot(
                name = if (twoHandedItem != null) "Two-Handed" else "Left Hand",
                item = leftHandItem,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .size(60.dp, 80.dp)
                    .offset(x = 5.dp, y = 20.dp),
                onClick = {
                    selectedSlot = if (twoHandedItem != null) "Two-Handed" else "Hand"
                    currentEquippedItem = leftHandItem
                    availableItems = if (twoHandedItem != null) {
                        allItems.filter { it.definition.equipSlot == "Two-Handed" }
                    } else if (handItems.size >= 2) {
                        allItems.filter { it.definition.equipSlot == "Hand" && it.item.id == leftHandItem?.item?.id }
                    } else {
                        allItems.filter { it.definition.equipSlot == "Hand" || it.definition.equipSlot == "Two-Handed" }
                    }
                    showSlotDialog = true
                }
            )

            // Right hand slot
            EquipmentSlot(
                name = if (twoHandedItem != null) "Two-Handed" else "Right Hand",
                item = rightHandItem,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .size(60.dp, 80.dp)
                    .offset(x = (-5).dp, y = 20.dp),
                onClick = {
                    selectedSlot = if (twoHandedItem != null) "Two-Handed" else "Hand"
                    currentEquippedItem = rightHandItem
                    availableItems = if (twoHandedItem != null) {
                        allItems.filter { it.definition.equipSlot == "Two-Handed" }
                    } else if (handItems.size >= 2) {
                        allItems.filter { it.definition.equipSlot == "Hand" && it.item.id == rightHandItem?.item?.id }
                    } else {
                        allItems.filter { it.definition.equipSlot == "Hand" || it.definition.equipSlot == "Two-Handed" }
                    }
                    showSlotDialog = true
                }
            )

            // Hands slot (gloves)
            EquipmentSlot(
                name = "Hands",
                item = handsSlot,
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(100.dp, 30.dp)
                    .offset(y = 100.dp),
                onClick = {
                    selectedSlot = "Hands"
                    currentEquippedItem = handsSlot
                    availableItems = allItems.filter { it.definition.equipSlot == "Hands" }
                    showSlotDialog = true
                }
            )

            // Feet slot
            EquipmentSlot(
                name = "Feet",
                item = feetSlot,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .size(100.dp, 50.dp)
                    .offset(y = (-5).dp),
                onClick = {
                    selectedSlot = "Feet"
                    currentEquippedItem = feetSlot
                    availableItems = allItems.filter { it.definition.equipSlot == "Feet" }
                    showSlotDialog = true
                }
            )

            // Accessory slot
            EquipmentSlot(
                name = "Accessory",
                item = accessorySlot,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(60.dp)
                    .offset(x = (-10).dp, y = 10.dp),
                circular = true,
                onClick = {
                    selectedSlot = "Accessory"
                    currentEquippedItem = accessorySlot
                    availableItems = allItems.filter { it.definition.equipSlot == "Accessory" }
                    showSlotDialog = true
                }
            )

            // Character icon in center
            Icon(
                Icons.Default.Person,
                contentDescription = "Character",
                modifier = Modifier
                    .size(150.dp)
                    .align(Alignment.Center),
                tint = MaterialTheme.colorScheme.surfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Equipment stats summary
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Equipment Summary",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Count equipped items
                val equippedCount = itemsWithDefinitions.count { it.item.equipped }
                val totalSlots = 7 // head, body, hands, feet, accessory, and 2 hand slots

                Text("Equipped: $equippedCount / $totalSlots slots filled")

                Spacer(modifier = Modifier.height(8.dp))

                // Calculate and display total modifiers from all equipped items
                val statModifiers = mutableMapOf<String, Int>()
                itemsWithDefinitions
                    .filter { it.item.equipped }
                    .forEach { item ->
                        item.definition.statModifiers.forEach { (stat, value) ->
                            statModifiers[stat] = (statModifiers[stat] ?: 0) + value
                        }
                    }

                if (statModifiers.isNotEmpty()) {
                    Text("Total Bonuses:", fontWeight = FontWeight.Bold)
                    statModifiers.forEach { (stat, value) ->
                        Text("• $stat: ${if (value > 0) "+$value" else value}")
                    }
                } else {
                    Text("No stat bonuses from equipment")
                }
            }
        }

        // Equipment slot dialog
        if (showSlotDialog) {
            EquipmentSlotDialog(
                slotName = selectedSlot,
                currentItem = currentEquippedItem,
                availableItems = availableItems,
                onDismiss = { showSlotDialog = false },
                onEquipItem = { item ->
                    onToggleEquipped(item)
                    showSlotDialog = false
                }
            )
        }
    }
}

@Composable
fun EquipmentSlot(
    name: String,
    item: ItemWithDefinition?,
    modifier: Modifier = Modifier,
    circular: Boolean = false,
    onClick: () -> Unit
) {
    val shape = if (circular) CircleShape else RoundedCornerShape(8.dp)

    Box(
        modifier = modifier
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
            .border(
                width = 2.dp,
                color = if (item != null)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.outline,
                shape = shape
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (item != null) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(4.dp)
            ) {
                Text(
                    item.definition.name,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    lineHeight = 14.sp
                )
            }
        } else {
            Text(
                name,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun EquipmentSlotDialog(
    slotName: String,
    currentItem: ItemWithDefinition?,
    availableItems: List<ItemWithDefinition>,
    onDismiss: () -> Unit,
    onEquipItem: (Item) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "$slotName Slot",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (currentItem != null) {
                    Text(
                        "Currently Equipped:",
                        fontWeight = FontWeight.Bold
                    )

                    ItemCard(
                        name = currentItem.definition.name,
                        description = currentItem.definition.description,
                        modifiers = currentItem.definition.statModifiers,
                        equipped = true,
                        onClick = { onEquipItem(currentItem.item) }
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }

                Text(
                    "Available Items:",
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (availableItems.isEmpty()) {
                    Text("No items available for this slot")
                } else {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        availableItems.forEach { itemWithDef ->
                            val isEquipped = itemWithDef.item.equipped
                            if (!isEquipped || itemWithDef.item.id == currentItem?.item?.id) {
                                ItemCard(
                                    name = itemWithDef.definition.name,
                                    description = itemWithDef.definition.description,
                                    modifiers = itemWithDef.definition.statModifiers,
                                    equipped = isEquipped,
                                    onClick = { onEquipItem(itemWithDef.item) }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Close")
                }
            }
        }
    }
}

@Composable
fun ItemCard(
    name: String,
    description: String,
    modifiers: Map<String, Int>,
    equipped: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (equipped)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = name,
                    fontWeight = FontWeight.Bold,
                    color = if (equipped)
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (equipped) {
                    Text(
                        text = "Equipped",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (description.isNotEmpty()) {
                Text(
                    text = description,
                    fontSize = 12.sp,
                    maxLines = 2,
                    color = if (equipped)
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }

            if (modifiers.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    modifiers.forEach { (stat, value) ->
                        Text(
                            text = "$stat ${if (value > 0) "+$value" else value}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (value > 0)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}