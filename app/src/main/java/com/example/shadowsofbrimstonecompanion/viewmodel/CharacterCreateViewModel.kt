package com.example.shadowsofbrimstonecompanion.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.shadowsofbrimstonecompanion.BrimstoneApplication
import com.example.shadowsofbrimstonecompanion.data.entity.Attributes
import com.example.shadowsofbrimstonecompanion.data.entity.Character
import com.example.shadowsofbrimstonecompanion.data.entity.CharacterClassDefinition
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class CharacterCreateViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = (application as BrimstoneApplication).repository

    // Dynamic character class list from database
    private val _characterClasses = MutableStateFlow<List<CharacterClassDefinition>>(emptyList())
    val characterClasses: StateFlow<List<CharacterClassDefinition>> = _characterClasses.asStateFlow()

    // Creation state
    private val _characterName = MutableStateFlow("")
    val characterName: StateFlow<String> = _characterName.asStateFlow()

    private val _selectedClassIndex = MutableStateFlow(0)
    val selectedClassIndex: StateFlow<Int> = _selectedClassIndex.asStateFlow()

    init {
        // Load character classes from the repository
        viewModelScope.launch {
            repository.allClassDefinitions.collectLatest { classes ->
                _characterClasses.value = classes
            }
        }
    }

    // Functions to update state
    fun updateCharacterName(name: String) {
        _characterName.value = name
    }

    fun selectCharacterClass(index: Int) {
        if (_characterClasses.value.isNotEmpty() && index in _characterClasses.value.indices) {
            _selectedClassIndex.value = index
        }
    }

    // Create a new character and return its ID
    suspend fun createCharacter(): Long {
        if (_characterClasses.value.isEmpty() || _selectedClassIndex.value >= _characterClasses.value.size) {
            return -1L
        }

        val selectedClass = _characterClasses.value[_selectedClassIndex.value]

        // Create the character
        val character = Character(
            name = _characterName.value.ifEmpty { selectedClass.name },
            characterClass = selectedClass.name,
            health = selectedClass.startingHealth,
            maxHealth = selectedClass.startingHealth,
            sanity = selectedClass.startingSanity,
            maxSanity = selectedClass.startingSanity,
            xp = 0,
            gold = 100, // Starting gold
            darkstone = 0,
            initiative = 0,
            combat = 0,
            defense = 0
        )

        val characterId = repository.insertCharacter(character)

        // Create the attributes directly from the class definition
        val attributes = Attributes(
            characterId = characterId,
            agility = selectedClass.startingAttributes["Agility"] ?: 0,
            strength = selectedClass.startingAttributes["Strength"] ?: 0,
            lore = selectedClass.startingAttributes["Lore"] ?: 0,
            luck = selectedClass.startingAttributes["Luck"] ?: 0,
            cunning = selectedClass.startingAttributes["Cunning"] ?: 0,
            spirit = selectedClass.startingAttributes["Spirit"] ?: 0
        )

        repository.insertAttributes(attributes)

        // Return the new character's ID
        return characterId
    }

    // Function to be called when creating a character
    fun onCreateCharacter(onComplete: (Long) -> Unit) {
        viewModelScope.launch {
            val characterId = createCharacter()
            onComplete(characterId)
        }
    }
}