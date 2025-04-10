package com.example.shadowsofbrimstonecompanion.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.shadowsofbrimstonecompanion.data.entity.Container
import com.example.shadowsofbrimstonecompanion.data.entity.ContainerWithItems
import com.example.shadowsofbrimstonecompanion.data.entity.Item
import com.example.shadowsofbrimstonecompanion.data.entity.ItemWithDefinition

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContainerScreen(
    containers: List<ContainerWithItems>,
    allItems: List<ItemWithDefinition>,
    looseItems: List<ItemWithDefinition>,
    onMoveItem: (Long, Long?) -> Unit,
    onClose: () -> Unit
) {
    var selectedContainer by remember { mutableStateOf<ContainerWithItems?>(null) }
    var showAddItemDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Containers & Storage") },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Container list
            Text(
                "Containers",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (containers.isEmpty()) {
                Text("No containers available")
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(0.4f)
                        .fillMaxWidth()
                ) {
                    items(containers) { containerWithItems ->
                        ContainerListItem(
                            containerWithItems = containerWithItems,
                            isSelected = containerWithItems == selectedContainer,
                            onClick = { selectedContainer = containerWithItems }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Container contents
                Text(
                    "Contents: ${selectedContainer?.container?.name ?: "No container selected"}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (selectedContainer == null) {
                    Text("Select a container to view its contents")
                } else {
                    val containerItems = selectedContainer!!.items
                    if (containerItems.isEmpty()) {
                        Text("Container is empty")
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .weight(0.6f)
                                .fillMaxWidth()
                        ) {
                            items(containerItems) { item ->
                                val itemDef = allItems.find { it.item.id == item.id }?.definition
                                if (itemDef != null) {
                                    ContainerItemCard(
                                        itemName = itemDef.name,
                                        description = itemDef.description,
                                        onRemove = { onMoveItem(item.id, null) }
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Add item section
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = { showAddItemDialog = true },
                            modifier = Modifier.align(Alignment.Center)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add item")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Add Item to Container")
                        }
                    }
                }
            }

            // Loose items section
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Loose Items",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                if (selectedContainer != null) {
                    Text(
                        "${looseItems.size} items available",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (looseItems.isEmpty()) {
                Text("No loose items in inventory")
            } else {
                LazyColumn(
                    modifier = Modifier
                        .heightIn(max = 200.dp)
                        .fillMaxWidth()
                ) {
                    items(looseItems) { itemWithDef ->
                        LooseItemCard(
                            itemName = itemWithDef.definition.name,
                            description = itemWithDef.definition.description,
                            onMove = {
                                if (selectedContainer != null) {
                                    onMoveItem(itemWithDef.item.id, selectedContainer!!.container.itemId)
                                }
                            },
                            canMove = selectedContainer != null
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }

    // Dialog for adding items to container
    if (showAddItemDialog && selectedContainer != null) {
        AddItemToContainerDialog(
            containerWithItems = selectedContainer!!,  // Changed from container to containerWithItems
            availableItems = looseItems,
            onDismiss = { showAddItemDialog = false },
            onAddItem = { itemId ->
                onMoveItem(itemId, selectedContainer!!.container.itemId)
                showAddItemDialog = false
            }
        )
    }
}

@Composable
fun ContainerListItem(
    containerWithItems: ContainerWithItems,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val container = containerWithItems.container
    val items = containerWithItems.items

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = container.name ?: "Container",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "${items.size}/${container.maxCapacity}",
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            if (container.isStash) {
                Text(
                    text = "Stash",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (container.acceptedItemTypes.isNotEmpty()) {
                Text(
                    text = "Accepts: ${container.acceptedItemTypes.joinToString(", ")}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
fun ContainerItemCard(
    itemName: String,
    description: String,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = itemName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2
                )
            }

            IconButton(onClick = onRemove) {
                Icon(
                    Icons.Default.RemoveCircle,
                    contentDescription = "Remove from container",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun LooseItemCard(
    itemName: String,
    description: String,
    onMove: () -> Unit,
    canMove: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = itemName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2
                )
            }

            Button(
                onClick = onMove,
                enabled = canMove,
                colors = ButtonDefaults.buttonColors(
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(
                    Icons.Default.MoveDown,
                    contentDescription = "Move to container"
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Move")
            }
        }
    }
}

@Composable
fun AddItemToContainerDialog(
    containerWithItems: ContainerWithItems,  // Changed parameter type from Container to ContainerWithItems
    availableItems: List<ItemWithDefinition>,
    onDismiss: () -> Unit,
    onAddItem: (Long) -> Unit
) {
    val container = containerWithItems.container
    val items = containerWithItems.items  // Get the items from the containerWithItems

    // Filter items based on container accepted types if necessary
    val filteredItems = if (container.acceptedItemTypes.isEmpty()) {
        availableItems
    } else {
        availableItems.filter { item ->
            container.acceptedItemTypes.any { acceptedType ->
                item.definition.type == acceptedType ||
                        item.definition.keywords.contains(acceptedType)
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Add Item to ${container.name ?: "Container"}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    "Capacity: ${items.size}/${container.maxCapacity}",  // Now correctly uses items from containerWithItems
                    style = MaterialTheme.typography.bodyMedium
                )

                if (container.acceptedItemTypes.isNotEmpty()) {
                    Text(
                        "Accepts: ${container.acceptedItemTypes.joinToString(", ")}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (filteredItems.isEmpty()) {
                    Text(
                        "No compatible items available",
                        style = MaterialTheme.typography.bodyLarge
                    )
                } else {
                    Text(
                        "Select an item to add:",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .heightIn(max = 300.dp)
                    ) {
                        items(filteredItems) { itemWithDef ->
                            ItemSelectionCard(
                                itemName = itemWithDef.definition.name,
                                description = itemWithDef.definition.description,
                                onClick = { onAddItem(itemWithDef.item.id) }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                }
            }
        }
    }
}

@Composable
fun ItemSelectionCard(
    itemName: String,
    description: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = itemName,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StashScreen(
    stashes: List<ContainerWithItems>,
    itemsWithDefinitions: List<ItemWithDefinition>,
    onMoveItem: (Long, Long?) -> Unit,
    onCreateStash: (String, Int, List<String>) -> Unit,
    onClose: () -> Unit
) {
    var selectedStash by remember { mutableStateOf<ContainerWithItems?>(null) }
    var showAddStashDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Stash Management") },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showAddStashDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Create stash")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Stashes list
            Text(
                "Available Stashes",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (stashes.isEmpty()) {
                Text("No stashes created yet. Create a stash to store items permanently.")
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(0.4f)
                        .fillMaxWidth()
                ) {
                    items(stashes) { stash ->
                        ContainerListItem(
                            containerWithItems = stash,
                            isSelected = stash == selectedStash,
                            onClick = { selectedStash = stash }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                // Stash contents
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    "Stash Contents: ${selectedStash?.container?.name ?: "No stash selected"}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (selectedStash == null) {
                    Text("Select a stash to view its contents")
                } else {
                    val stashItems = selectedStash!!.items
                    if (stashItems.isEmpty()) {
                        Text("Stash is empty")
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .weight(0.6f)
                                .fillMaxWidth()
                        ) {
                            items(stashItems) { item ->
                                val itemDef = itemsWithDefinitions.find { it.item.id == item.id }?.definition
                                if (itemDef != null) {
                                    ContainerItemCard(
                                        itemName = itemDef.name,
                                        description = itemDef.description,
                                        onRemove = { onMoveItem(item.id, null) }
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialog for creating new stash
    if (showAddStashDialog) {
        CreateStashDialog(
            onDismiss = { showAddStashDialog = false },
            onCreateStash = { name, capacity, acceptedTypes ->
                onCreateStash(name, capacity, acceptedTypes)
                showAddStashDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateStashDialog(
    onDismiss: () -> Unit,
    onCreateStash: (String, Int, List<String>) -> Unit
) {
    var stashName by remember { mutableStateOf("") }
    var stashCapacity by remember { mutableStateOf("10") }
    var stashAcceptedTypes by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Create New Stash",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = stashName,
                    onValueChange = { stashName = it },
                    label = { Text("Stash Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = stashCapacity,
                    onValueChange = {
                        if (it.isEmpty() || it.all { char -> char.isDigit() }) {
                            stashCapacity = it
                        }
                    },
                    label = { Text("Capacity") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = stashAcceptedTypes,
                    onValueChange = { stashAcceptedTypes = it },
                    label = { Text("Accepted Item Types (comma separated, leave blank for all)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            val capacity = stashCapacity.toIntOrNull() ?: 10
                            val acceptedTypes = if (stashAcceptedTypes.isBlank()) {
                                emptyList()
                            } else {
                                stashAcceptedTypes.split(",").map { it.trim() }
                            }

                            onCreateStash(
                                stashName.ifBlank { "Stash" },
                                capacity,
                                acceptedTypes
                            )
                        }
                    ) {
                        Text("Create")
                    }
                }
            }
        }
    }
}