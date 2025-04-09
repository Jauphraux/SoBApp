package com.example.shadowsofbrimstonecompanion.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.shadowsofbrimstonecompanion.BrimstoneApplication
import com.example.shadowsofbrimstonecompanion.data.entity.Attributes
import com.example.shadowsofbrimstonecompanion.data.entity.Character
import com.example.shadowsofbrimstonecompanion.data.entity.Item
import com.example.shadowsofbrimstonecompanion.data.entity.ItemDefinition
import com.example.shadowsofbrimstonecompanion.data.entity.ItemWithDefinition
import com.example.shadowsofbrimstonecompanion.data.entity.Skill
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class CharacterDetailViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {
    private val repository = (application as BrimstoneApplication).repository

    // Get character ID from saved state (passed from navigation)
    private val characterId: Long = savedStateHandle["characterId"] ?: -1L

    // Character data flows
    val character: Flow<Character?> = repository.getCharacterById(characterId)
    val attributes: Flow<Attributes?> = repository.getAttributesForCharacter(characterId)
    val itemsWithDefinitions: Flow<List<ItemWithDefinition>> = repository.getItemsWithDefinitionsForCharacter(characterId)
    val skills: Flow<List<Skill>> = repository.getSkillsForCharacter(characterId)
    val allItemDefinitions: Flow<List<ItemDefinition>> = repository.allItemDefinitions

    // Combined data for easy access in the UI
    val characterData: StateFlow<CharacterDetailState> = combine(
        character,
        attributes,
        itemsWithDefinitions,
        skills,
        allItemDefinitions
    ) { character, attributes, itemsWithDefinitions, skills, allItemDefinitions ->
        CharacterDetailState(
            character = character,
            attributes = attributes,
            itemsWithDefinitions = itemsWithDefinitions,
            skills = skills,
            allItemDefinitions = allItemDefinitions,
            isLoading = character == null || attributes == null
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CharacterDetailState()
    )

    // State class to hold all character detail data
    data class CharacterDetailState(
        val character: Character? = null,
        val attributes: Attributes? = null,
        val itemsWithDefinitions: List<ItemWithDefinition> = emptyList(),
        val skills: List<Skill> = emptyList(),
        val allItemDefinitions: List<ItemDefinition> = emptyList(),
        val isLoading: Boolean = true
    )

    /**
     * Calculates all stat modifiers from equipped items
     * @return Map of attribute names to their total modifier values
     */
    fun calculateEquippedItemModifiers(): Map<String, Int> {
        val modifiers = mutableMapOf<String, Int>()

        // Get all equipped items with their definitions
        val equippedItems = characterData.value.itemsWithDefinitions
            .filter { it.item.equipped }

        // Accumulate all modifiers
        equippedItems.forEach { itemWithDef ->
            itemWithDef.definition.statModifiers.forEach { (stat, value) ->
                modifiers[stat] = (modifiers[stat] ?: 0) + value
            }
        }

        return modifiers
    }

    // Actions
    fun increaseHealth(amount: Int = 1) {
        characterData.value.character?.let { character ->
            val newHealth = (character.health + amount).coerceAtMost(character.maxHealth)
            val updatedCharacter = character.copy(health = newHealth)
            updateCharacter(updatedCharacter)
        }
    }

    fun decreaseHealth(amount: Int = 1) {
        characterData.value.character?.let { character ->
            val newHealth = (character.health - amount).coerceAtLeast(0)
            val updatedCharacter = character.copy(health = newHealth)
            updateCharacter(updatedCharacter)
        }
    }

    fun increaseSanity(amount: Int = 1) {
        characterData.value.character?.let { character ->
            val newSanity = (character.sanity + amount).coerceAtMost(character.maxSanity)
            val updatedCharacter = character.copy(sanity = newSanity)
            updateCharacter(updatedCharacter)
        }
    }

    fun decreaseSanity(amount: Int = 1) {
        characterData.value.character?.let { character ->
            val newSanity = (character.sanity - amount).coerceAtLeast(0)
            val updatedCharacter = character.copy(sanity = newSanity)
            updateCharacter(updatedCharacter)
        }
    }

    fun addExperience(amount: Int) {
        characterData.value.character?.let { character ->
            val updatedCharacter = character.copy(xp = character.xp + amount)
            updateCharacter(updatedCharacter)
        }
    }

    fun levelUp() {
        // This would typically involve a more complex process including
        // attribute increases, new skills, etc.
        characterData.value.character?.let { character ->
            val updatedCharacter = character.copy(
                level = character.level + 1,
                maxHealth = character.maxHealth + 2,
                health = character.health + 2,
                xp = 0 // Reset XP after level up
            )
            updateCharacter(updatedCharacter)
        }
    }

    private fun updateCharacter(character: Character) {
        viewModelScope.launch {
            repository.updateCharacter(character)
        }
    }

    fun updateAttributes(attributes: Attributes) {
        viewModelScope.launch {
            repository.updateAttributes(attributes)
        }
    }

    fun addItem(itemDefinitionId: Long, quantity: Int = 1, notes: String = "") {
        viewModelScope.launch {
            val item = Item(
                characterId = characterId,
                itemDefinitionId = itemDefinitionId,
                quantity = quantity,
                notes = notes
            )
            repository.insertItem(item)
        }
    }

    fun toggleItemEquipped(item: Item) {
        viewModelScope.launch {
            val updatedItem = item.copy(equipped = !item.equipped)
            repository.updateItem(updatedItem)
        }
    }

    fun deleteItem(item: Item) {
        viewModelScope.launch {
            repository.deleteItem(item)
        }
    }

    fun addSkill(name: String, level: Int = 1, description: String = "") {
        viewModelScope.launch {
            val skill = Skill(
                characterId = characterId,
                name = name,
                level = level,
                description = description
            )
            repository.insertSkill(skill)
        }
    }

    fun upgradeSkill(skill: Skill) {
        viewModelScope.launch {
            val updatedSkill = skill.copy(level = skill.level + 1)
            repository.updateSkill(updatedSkill)
        }
    }

    fun deleteSkill(skill: Skill) {
        viewModelScope.launch {
            repository.deleteSkill(skill)
        }
    }

    companion object {
        class Factory(
            private val characterId: Long,
            private val application: Application
        ) : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(CharacterDetailViewModel::class.java)) {
                    val savedStateHandle = SavedStateHandle().apply {
                        set("characterId", characterId)
                    }
                    return CharacterDetailViewModel(
                        application = application,
                        savedStateHandle = savedStateHandle
                    ) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
}