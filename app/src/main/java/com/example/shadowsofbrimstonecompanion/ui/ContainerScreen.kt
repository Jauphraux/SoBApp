package com.example.shadowsofbrimstonecompanion.ui

import android.util.Log
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
    onMoveItem: (Long, Long?) -> Unit,
    onClose: () -> Unit,
    onStoreDarkstone: ((Long) -> Unit)? = null,
    characterDarkstone: Int = 0
) {
    var selectedContainer by remember { mutableStateOf<ContainerWithItems?>(null) }
    var showAddItemDialog by remember { mutableStateOf(false) }

    // Auto-select first container if none is selected and containers exist
    LaunchedEffect(containers) {
        if (selectedContainer == null && containers.isNotEmpty()) {
            Log.d("ContainerScreen", "Auto-selecting first container: ${containers.first().container.name}")
            selectedContainer = containers.first()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Manage Containers",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Container list
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
                        onClick = {
                            Log.d("ContainerScreen", "Container selected: ${containerWithItems.container.name}")
                            selectedContainer = containerWithItems
                        },
                        onStoreDarkstone = onStoreDarkstone,
                        characterDarkstone = characterDarkstone
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Container details
            selectedContainer?.let { containerWithItems ->
                val container = containerWithItems.container
                val items = containerWithItems.items

                // Container header
                Text(
                    container.name ?: "Unnamed Container",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Container capacity
                Text("Capacity: ${items.size}/${container.maxCapacity}")

                Spacer(modifier = Modifier.height(16.dp))

                // Items in container
                if (items.isEmpty()) {
                    Text("Container is empty")
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .weight(0.6f)
                            .fillMaxWidth()
                    ) {
                        items(items) { item ->
                            val itemWithDef = allItems.find { it.item.id == item.id }
                            if (itemWithDef != null) {
                                ContainerItemCard(
                                    itemName = itemWithDef.definition.name,
                                    description = itemWithDef.definition.description,
                                    onRemove = { onMoveItem(item.id, null) },
                                    isDarkstone = itemWithDef.definition.type == "Dark Stone",
                                    quantity = item.quantity
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }

                // Add item button
                Button(
                    onClick = { showAddItemDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Add Item to Container")
                }

                // Add Dark Stone storage button if applicable
                val acceptsDarkstone = container.acceptedItemTypes.isEmpty() ||
                        container.acceptedItemTypes.contains("Dark Stone")

                if (acceptsDarkstone && onStoreDarkstone != null && characterDarkstone > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { onStoreDarkstone(container.id) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    ) {
                        Text("Store Dark Stone in ${container.name ?: "Container"} (${characterDarkstone} available)")
                    }
                }
            }
        }
    }

    // Add item dialog
    if (showAddItemDialog) {
        AddItemToContainerDialog(
            onDismiss = { showAddItemDialog = false },
            onAddItem = { itemId ->
                selectedContainer?.let { container ->
                    onMoveItem(itemId, container.container.id)
                }
                showAddItemDialog = false
            },
            availableItems = allItems.filter { itemWithDef ->
                // Only show items that aren't in containers
                itemWithDef.item.containerId == null
            }
        )
    }
}

@Composable
fun ContainerListItem(
    containerWithItems: ContainerWithItems,
    isSelected: Boolean,
    onClick: () -> Unit,
    onStoreDarkstone: ((Long) -> Unit)? = null,
    characterDarkstone: Int = 0
) {
    val container = containerWithItems.container
    val items = containerWithItems.items

    // Check if container accepts dark stone
    val acceptsDarkstone = container.acceptedItemTypes.isEmpty() ||
            container.acceptedItemTypes.contains("Dark Stone")

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

            // Add Dark Stone storage button if applicable - only show when there's darkstone available
            if (acceptsDarkstone && onStoreDarkstone != null && characterDarkstone > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { container.itemId?.let { onStoreDarkstone(it) } },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                ) {
                    Text("Store Dark Stone in ${container.name ?: "Container"} (${characterDarkstone} available)")
                }
            }
        }
    }
}

@Composable
fun ContainerItemCard(
    itemName: String,
    description: String,
    onRemove: () -> Unit,
    isDarkstone: Boolean = false,
    quantity: Int = 1
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkstone)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
            else
                MaterialTheme.colorScheme.surfaceVariant
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isDarkstone) {
                        Text(
                            "🌑 ", // Dark stone emoji indicator
                            style = MaterialTheme.typography.titleSmall
                        )
                    }
                    Text(
                        text = if (quantity > 1) "$itemName (x$quantity)" else itemName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2
                )

                if (isDarkstone) {
                    Text(
                        text = "Protected from corruption",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
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
fun AddItemToContainerDialog(
    onDismiss: () -> Unit,
    onAddItem: (Long) -> Unit,
    availableItems: List<ItemWithDefinition>
) {
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
                    text = "Add Item to Container",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

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
                    items(availableItems) { itemWithDef ->
                        ItemSelectionCard(
                            itemName = itemWithDef.definition.name,
                            description = itemWithDef.definition.description,
                            onClick = { onAddItem(itemWithDef.item.id) },
                            isDarkstone = itemWithDef.definition.type == "Dark Stone"
                        )
                        Spacer(modifier = Modifier.height(8.dp))
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
    onClick: () -> Unit,
    isDarkstone: Boolean = false
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkstone)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isDarkstone) {
                    Text(
                        "🌑 ", // Dark stone emoji indicator
                        style = MaterialTheme.typography.titleSmall
                    )
                }
                Text(
                    text = itemName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2
            )

            if (isDarkstone) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Storing will protect from corruption",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
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

    // Auto-select first stash if available and none selected
    LaunchedEffect(stashes) {
        if (selectedStash == null && stashes.isNotEmpty()) {
            selectedStash = stashes.first()
        }
    }

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
                                        onRemove = { onMoveItem(item.id, null) },
                                        isDarkstone = itemDef.type == "Dark Stone",
                                        quantity = item.quantity
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