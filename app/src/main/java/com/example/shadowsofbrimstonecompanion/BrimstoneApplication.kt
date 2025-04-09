package com.example.shadowsofbrimstonecompanion

import android.app.Application
import android.util.Log
import com.example.shadowsofbrimstonecompanion.data.database.AppDatabase
import com.example.shadowsofbrimstonecompanion.data.entity.CharacterClassDefinition
import com.example.shadowsofbrimstonecompanion.data.entity.ItemDefinition
import com.example.shadowsofbrimstonecompanion.data.repository.BrimstoneRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
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
            database.itemDefinitionDao()
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