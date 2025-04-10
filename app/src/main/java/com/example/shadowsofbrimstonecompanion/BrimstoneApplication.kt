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
            try {
                // Always reload class definitions from JSON
                loadInitialClassDefinitions()

                // Always reload item definitions from JSON
                loadInitialItemDefinitions()

                // Create default stash if none exists
                createDefaultStashIfNeeded(applicationScope)
            } catch (e: Exception) {
                Log.e("BrimstoneApp", "Error initializing app data", e)
            }
        }
    }

    private fun createDefaultStashIfNeeded(applicationScope: CoroutineScope) {
        applicationScope.launch {
            try {
                // Check if Town Storage already exists
                val stashes = repository.getStashes().first()
                val townStorageExists = stashes.any { it.container.name == "Town Storage" }

                if (!townStorageExists) {
                    Log.d("BrimstoneApp", "No Town Storage found, creating system container")

                    // Create the Town Storage as a system container
                    repository.createContainer(
                        itemId = null,  // No associated item
                        maxCapacity = 20,
                        acceptedItemTypes = emptyList(),
                        isStash = true,
                        name = "Town Storage",
                        isSystemContainer = true
                    )

                    Log.d("BrimstoneApp", "Created Town Storage system container")
                } else {
                    Log.d("BrimstoneApp", "Town Storage already exists. Skipping creation.")
                }
            } catch (e: Exception) {
                Log.e("BrimstoneApp", "Error creating Town Storage", e)
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

            // Clear existing class definitions
            repository.deleteAllClassDefinitions()

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

            // Clear existing item definitions
            repository.deleteAllItemDefinitions()

            // Insert the items into the database
            repository.insertAllItemDefinitions(itemDefinitions)

            Log.d("BrimstoneApp", "Loaded ${itemDefinitions.size} item definitions")
        } catch (e: Exception) {
            Log.e("BrimstoneApp", "Error loading initial item definitions", e)
        }
    }
}