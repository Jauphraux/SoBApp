package com.example.shadowsofbrimstonecompanion.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.shadowsofbrimstonecompanion.viewmodel.CharacterCreateViewModel
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.collectAsState
import androidx.compose.material3.ExposedDropdownMenuDefaults


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterCreateScreen(
    onNavigateBack: () -> Unit,
    onCharacterCreated: (Long) -> Unit,
    viewModel: CharacterCreateViewModel = viewModel()
) {
    val characterName by viewModel.characterName.collectAsState()
    val selectedClassIndex by viewModel.selectedClassIndex.collectAsState()
    val characterClassesList by viewModel.characterClasses.collectAsState()

    val selectedClass = if (characterClassesList.isNotEmpty() && selectedClassIndex < characterClassesList.size) {
        characterClassesList[selectedClassIndex]
    } else {
        null
    }

    var showConfirmation by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create New Character") },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Character Name
            OutlinedTextField(
                value = characterName,
                onValueChange = { viewModel.updateCharacterName(it) },
                label = { Text("Character Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Character Class Selection
            Text(
                text = "Choose Character Class",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (characterClassesList.isEmpty()) {
                Text("Loading character classes...", style = MaterialTheme.typography.bodyMedium)
            } else if (selectedClass != null) {
                // Dropdown menu implementation
                var expanded by remember { mutableStateOf(false) }

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedClass.name,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        label = { Text("Character Class") }
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        characterClassesList.forEachIndexed { index, classDefinition ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = classDefinition.name,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                },
                                onClick = {
                                    viewModel.selectCharacterClass(index)
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Class Description
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = selectedClass.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "HP: ${selectedClass.startingHealth} · Sanity: ${selectedClass.startingSanity}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = selectedClass.description)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Class Attributes
                Text(
                    text = "Starting Attributes",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Attributes
                selectedClass.startingAttributes.forEach { (attribute, value) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = attribute,
                            modifier = Modifier.width(80.dp),
                            style = MaterialTheme.typography.bodyLarge
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        Text(
                            text = value.toString(),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }

                    Divider()
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Create Button
                Button(
                    onClick = { showConfirmation = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Create Character")
                }
            }
        }
    }

    // Confirmation Dialog
    if (showConfirmation && selectedClass != null) {
        AlertDialog(
            onDismissRequest = { showConfirmation = false },
            title = { Text("Create Character") },
            text = {
                Text(
                    "Create a new ${selectedClass.name}" +
                            (if (characterName.isNotEmpty()) " named $characterName" else "") +
                            "?"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmation = false
                        viewModel.onCreateCharacter(onCharacterCreated)
                    }
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}