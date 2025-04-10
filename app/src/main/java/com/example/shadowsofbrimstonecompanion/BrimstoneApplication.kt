package com.example.shadowsofbrimstonecompanion

import android.app.Application
import android.util.Log
import com.example.shadowsofbrimstonecompanion.data.database.AppDatabase
import com.example.shadowsofbrimstonecompanion.data.entity.CharacterClassDefinition
import com.example.shadowsofbrimstonecompanion.data.entity.Item
import com.example.shadowsofbrimstonecompanion.data.entity.ItemDefinition
import com.example.shadowsofbrimstonecompanion.data.repository.BrimstoneRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BrimstoneApplication : Application() {
    // Initialize database
    private val database by lazy { AppDatabase.getDatabase(this) }

    // Initialize repository
    val repository by lazy {
        BrimstoneRepository(
            database.characterDao(),
            database.attributesDao(),
            database.itemDao(),
            database.skillDao(),
            database.characterClassDefinitionDao(),
            database.itemDefinitionDao(),
            database.containerDao() // Added containerDao for container support
        )
    }

    override fun onCreate() {
        super.onCreate()

        // Initialize app data if needed
        initializeAppData()
    }

    private fun initializeAppData() {
        val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        applicationScope.launch {
            // Check if class definitions need to be loaded
            if (repository.getClassDefinitionCount() == 0) {
                loadInitialClassDefinitions()
            }

            // Check if item definitions need to be loaded
            if (repository.getItemDefinitionCount() == 0) {
                loadInitialItemDefinitions()
            }

            // Create default stash if none exists
            createDefaultStashIfNeeded(applicationScope)
        }
    }

    private fun createDefaultStashIfNeeded(applicationScope: CoroutineScope) {
        applicationScope.launch {
            try {
                // Check if any stashes exist
                val stashes = repository.getStashes().first()

                if (stashes.isEmpty()) {
                    Log.d("BrimstoneApp", "No stashes found, creating default stash")

                    // First create an ItemDefinition
                    val stashItemDef = ItemDefinition(
                        name = "Town Storage",
                        description = "A secure location to store items in town",
                        type = "Stash",
                        keywords = listOf("Container", "Storage"),
                        isContainer = true,
                        containerCapacity = 20,
                        containerAcceptedTypes = emptyList() // Accepts everything
                    )

                    // Insert the ItemDefinition and get its ID
                    val itemDefId = repository.insertItemDefinition(stashItemDef)
                    Log.d("BrimstoneApp", "Created stash item definition with ID: $itemDefId")

                    // Now create a virtual Item entry - this is critical for maintaining foreign key relationships
                    val virtualItemId = repository.insertItem(
                        Item(
                            characterId = -1, // Use a special value like -1 to indicate system items
                            itemDefinitionId = itemDefId,
                            quantity = 1,
                            notes = "System generated stash"
                        )
                    )
                    Log.d("BrimstoneApp", "Created virtual item with ID: $virtualItemId")

                    // Only now create the Container using the Item ID
                    repository.createContainer(
                        itemId = virtualItemId,
                        maxCapacity = 20,
                        acceptedItemTypes = emptyList(),
                        isStash = true,
                        name = "Town Storage"
                    )

                    Log.d("BrimstoneApp", "Created default town storage stash")
                } else {
                    Log.d("BrimstoneApp", "Stashes already exist. Skipping default stash creation.")
                }
            } catch (e: Exception) {
                Log.e("BrimstoneApp", "Error creating default stash", e)
            }
        }
    }

    private suspend fun loadInitialClassDefinitions() {
        try {
            // Read the JSON from assets
            val jsonString = assets.open("initial_classes.json").bufferedReader().use { it.readText() }
            val gson = Gson()
            val classesType = object : TypeToken<List<CharacterClassDefinition>>() {}.type
            val classDefinitions: List<CharacterClassDefinition> = gson.fromJson(jsonString, classesType)

            // Insert the classes into the database
            repository.insertAllClassDefinitions(classDefinitions)

            Log.d("BrimstoneApp", "Loaded ${classDefinitions.size} class definitions")
        } catch (e: Exception) {
            Log.e("BrimstoneApp", "Error loading initial class definitions", e)
        }
    }

    private suspend fun loadInitialItemDefinitions() {
        try {
            // Read the JSON from assets
            val jsonString = assets.open("initial_items.json").bufferedReader().use { it.readText() }
            val gson = Gson()
            val itemsType = object : TypeToken<List<ItemDefinition>>() {}.type
            val itemDefinitions: List<ItemDefinition> = gson.fromJson(jsonString, itemsType)

            // Insert the items into the database
            repository.insertAllItemDefinitions(itemDefinitions)

            Log.d("BrimstoneApp", "Loaded ${itemDefinitions.size} item definitions")
        } catch (e: Exception) {
            Log.e("BrimstoneApp", "Error loading initial item definitions", e)
        }
    }
}