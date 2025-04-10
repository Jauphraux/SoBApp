package com.example.shadowsofbrimstonecompanion.ui

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.shadowsofbrimstonecompanion.data.entity.Attributes
import com.example.shadowsofbrimstonecompanion.data.entity.Character
import com.example.shadowsofbrimstonecompanion.data.entity.Skill
import com.example.shadowsofbrimstonecompanion.viewmodel.CharacterDetailViewModel
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterDetailScreen(
    characterId: Long,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: CharacterDetailViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val savedStateHandle = SavedStateHandle().apply {
                    set("characterId", characterId)
                }
                return CharacterDetailViewModel(
                    application = context.applicationContext as Application,
                    savedStateHandle = savedStateHandle
                ) as T
            }
        }
    )

    val state by viewModel.characterData.collectAsState()
    val character = state.character
    val attributes = state.attributes
    val showContainerScreen by viewModel.showContainerScreen.collectAsState()
    val storageData by viewModel.storageData.collectAsState()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Character", "Inventory", "Gear", "Skills")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            character?.name ?: "Loading...",
                            style = MaterialTheme.typography.titleLarge
                        )
                        if (character != null) {
                            Text(
                                " - Level ${character.level} ${character.characterClass}",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
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
        if (showContainerScreen) {
            ContainerScreen(
                containers = storageData.containers,
                allItems = storageData.allItems,
                onMoveItem = { itemId, containerId -> viewModel.moveItemToContainer(itemId, containerId) },
                onClose = { viewModel.toggleContainerScreen(false) },
                onStoreDarkstone = { containerId -> viewModel.storeDarkstoneInContainer(containerId) },
                characterDarkstone = state.character?.darkstone ?: 0
            )
        } else {
            if (state.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    // Tab Row
                    TabRow(selectedTabIndex = selectedTabIndex) {
                        tabs.forEachIndexed { index, title ->
                            Tab(
                                selected = selectedTabIndex == index,
                                onClick = { selectedTabIndex = index },
                                text = { Text(title) }
                            )
                        }
                    }

                    // Tab Content
                    when (selectedTabIndex) {
                        0 -> {
                            // Calculate modifiers when showing the character tab
                            val itemModifiers = viewModel.calculateEquippedItemModifiers()

                            CharacterTab(
                                character = character,
                                attributes = attributes,
                                itemModifiers = itemModifiers,
                                onHealthIncrease = { viewModel.increaseHealth() },
                                onHealthDecrease = { viewModel.decreaseHealth() },
                                onSanityIncrease = { viewModel.increaseSanity() },
                                onSanityDecrease = { viewModel.decreaseSanity() },
                                onAddXP = { viewModel.addExperience(10) },
                                onLevelUp = { viewModel.levelUp() },
                                viewModel = viewModel
                            )
                        }
                        1 -> {
                            Column(modifier = Modifier.fillMaxSize()) {
                                // Container management button
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    Button(
                                        onClick = { viewModel.toggleContainerScreen(true) },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                    ) {
                                        Icon(Icons.Default.Inventory2, contentDescription = "Manage Containers")
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Manage Containers")
                                    }
                                }

                                // Existing inventory tab content
                                InventoryTab(
                                    itemsWithDefinitions = state.itemsWithDefinitions.filter { it.item.containerId == null }, // Only show loose items
                                    allItemDefinitions = state.allItemDefinitions,
                                    onToggleEquipped = { viewModel.toggleItemEquipped(it) },
                                    onDeleteItem = { viewModel.deleteItem(it) },
                                    onAddItem = { itemDefId, quantity, notes ->
                                        viewModel.addItem(itemDefId, quantity, notes)
                                    },
                                    onSellItem = { item, percentage -> viewModel.sellItem(item, percentage) },
                                    onUseAsContainer = { item -> viewModel.useItemAsContainer(item) }, // Added this line
                                    currentEncumbrance = viewModel.calculateTotalAnvilWeight(),
                                    maxEncumbrance = state.attributes?.strength?.plus(5) ?: 0,
                                    errorMessage = viewModel.uiMessage.collectAsState().value,
                                    onErrorMessageShown = { viewModel.clearUiMessage() }
                                )
                            }
                        }
                        2 -> EquipmentScreen(
                            itemsWithDefinitions = state.itemsWithDefinitions,
                            allItems = state.itemsWithDefinitions,
                            onToggleEquipped = { viewModel.toggleItemEquipped(it) },
                            errorMessage = viewModel.uiMessage.collectAsState().value,
                            onErrorMessageShown = { viewModel.clearUiMessage() }
                        )
                        3 -> SkillsTab(
                            skills = state.skills,
                            onUpgradeSkill = { viewModel.upgradeSkill(it) },
                            onDeleteSkill = { viewModel.deleteSkill(it) },
                            onAddSkill = { name, description ->
                                viewModel.addSkill(name, description = description)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CharacterTab(
    character: Character?,
    attributes: Attributes?,
    itemModifiers: Map<String, Int>,
    onHealthIncrease: () -> Unit,
    onHealthDecrease: () -> Unit,
    onSanityIncrease: () -> Unit,
    onSanityDecrease: () -> Unit,
    onAddXP: () -> Unit,
    onLevelUp: () -> Unit,
    viewModel: CharacterDetailViewModel
) {
    if (character == null || attributes == null) return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Vitals section
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Vitals",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Health
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Health: ${character.health}/${character.maxHealth}",
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onHealthDecrease) {
                        Icon(Icons.Default.Close, contentDescription = "Decrease Health")
                    }
                    IconButton(onClick = onHealthIncrease) {
                        Icon(Icons.Default.Add, contentDescription = "Increase Health")
                    }
                }

                LinearProgressIndicator(
                    progress = { character.health.toFloat() / character.maxHealth },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    color = MaterialTheme.colorScheme.error
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Sanity
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Sanity: ${character.sanity}/${character.maxSanity}",
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onSanityDecrease) {
                        Icon(Icons.Default.Close, contentDescription = "Decrease Sanity")
                    }
                    IconButton(onClick = onSanityIncrease) {
                        Icon(Icons.Default.Add, contentDescription = "Increase Sanity")
                    }
                }

                LinearProgressIndicator(
                    progress = { character.sanity.toFloat() / character.maxSanity },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .padding(vertical = 2.dp),
                    color = MaterialTheme.colorScheme.tertiary
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Experience
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Experience: ${character.xp} XP")
                        Text(
                            "Level ${character.level}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Button(onClick = onAddXP) {
                        Text("Add XP")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onLevelUp,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        Text("Level Up")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Attributes section
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Attributes",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Attribute rows with modifiers
                AttributeRow("Agility", attributes.agility, itemModifiers["Agility"] ?: 0)
                AttributeRow("Strength", attributes.strength, itemModifiers["Strength"] ?: 0)
                AttributeRow("Lore", attributes.lore, itemModifiers["Lore"] ?: 0)
                AttributeRow("Luck", attributes.luck, itemModifiers["Luck"] ?: 0)
                AttributeRow("Cunning", attributes.cunning, itemModifiers["Cunning"] ?: 0)
                AttributeRow("Spirit", attributes.spirit, itemModifiers["Spirit"] ?: 0)

                // Derived stats section (only show if there are modifiers)
                if (itemModifiers["Initiative"] != null || itemModifiers["Combat"] != null || itemModifiers["Defense"] != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Derived Stats",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    if (itemModifiers["Initiative"] != null) {
                        AttributeRow("Initiative", character.initiative, itemModifiers["Initiative"] ?: 0)
                    }
                    if (itemModifiers["Combat"] != null) {
                        AttributeRow("Combat", character.combat, itemModifiers["Combat"] ?: 0)
                    }
                    if (itemModifiers["Defense"] != null) {
                        AttributeRow("Defense", character.defense, itemModifiers["Defense"] ?: 0)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Resources section
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Resources",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Gold
                Text("Gold: ${character.gold}")

                // Dark Stone controls - adding this section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Dark Stone: ${character.darkstone}")
                        val storedDarkstone = viewModel.getStoredDarkstoneCount()
                        if (storedDarkstone > 0) {
                            Text(
                                "($storedDarkstone stored in containers)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    IconButton(onClick = { viewModel.decreaseDarkstone() }) {
                        Icon(Icons.Default.Remove, contentDescription = "Decrease Dark Stone")
                    }
                    IconButton(onClick = { viewModel.increaseDarkstone() }) {
                        Icon(Icons.Default.Add, contentDescription = "Increase Dark Stone")
                    }
                }
            }
        }

        // Add this new Card for Encumbrance
        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Encumbrance",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Get current encumbrance from viewModel
                val currentWeight = viewModel.calculateTotalAnvilWeight()
                val maxWeight = attributes.strength + 5

                // Display current/max weight
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Carrying: $currentWeight / $maxWeight anvils")
                }

                // Encumbrance progress bar
                LinearProgressIndicator(
                    progress = { currentWeight.toFloat() / maxWeight.toFloat() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .padding(vertical = 2.dp),
                    color = when {
                        currentWeight > maxWeight -> MaterialTheme.colorScheme.error
                        currentWeight >= maxWeight * 0.8f -> MaterialTheme.colorScheme.tertiary
                        else -> MaterialTheme.colorScheme.primary
                    }
                )

                // Warning message if overencumbered
                if (currentWeight > maxWeight) {
                    Text(
                        "Overencumbered! You may suffer penalties.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AttributeRow(
    name: String,
    baseValue: Int,
    modifier: Int = 0
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(name, style = MaterialTheme.typography.bodyLarge)

        Row {
            if (modifier != 0) {
                Text(
                    text = "$baseValue",
                    style = MaterialTheme.typography.bodyLarge
                )

                Text(
                    text = " ${if (modifier > 0) "+" else ""}$modifier",
                    color = if (modifier > 0)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyLarge
                )

                Text(
                    text = " = ${baseValue + modifier}",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge
                )
            } else {
                Text(
                    text = "$baseValue",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
    Divider()
}

@Composable
fun SkillsTab(
    skills: List<Skill>,
    onUpgradeSkill: (Skill) -> Unit,
    onDeleteSkill: (Skill) -> Unit,
    onAddSkill: (String, String) -> Unit
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
                "Skills",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Button(onClick = { showAddDialog = true }) {
                Text("Add Skill")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (skills.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("No skills learned yet")
            }
        } else {
            LazyColumn {
                items(skills) { skill ->
                    SkillCard(
                        skill = skill,
                        onUpgrade = { onUpgradeSkill(skill) },
                        onDelete = { onDeleteSkill(skill) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }

    if (showAddDialog) {
        AddSkillDialog(
            onDismiss = { showAddDialog = false },
            onAddSkill = { name, description ->
                onAddSkill(name, description)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun SkillCard(
    skill: Skill,
    onUpgrade: () -> Unit,
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = skill.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Row {
                    Text(
                        text = "Level ${skill.level}",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(end = 8.dp, top = 4.dp)
                    )

                    IconButton(onClick = { expanded = !expanded }) {
                        Icon(
                            if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = if (expanded) "Collapse" else "Expand"
                        )
                    }
                }
            }

            if (expanded) {
                if (skill.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = skill.description,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

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

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(onClick = onUpgrade) {
                        Text("Upgrade")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSkillDialog(
    onDismiss: () -> Unit,
    onAddSkill: (String, String) -> Unit
) {
    var skillName by remember { mutableStateOf("") }
    var skillDescription by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Skill") },
        text = {
            Column {
                OutlinedTextField(
                    value = skillName,
                    onValueChange = { skillName = it },
                    label = { Text("Skill Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = skillDescription,
                    onValueChange = { skillDescription = it },
                    label = { Text("Description") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (skillName.isNotBlank()) {
                        onAddSkill(skillName, skillDescription)
                    }
                },
                enabled = skillName.isNotBlank()
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