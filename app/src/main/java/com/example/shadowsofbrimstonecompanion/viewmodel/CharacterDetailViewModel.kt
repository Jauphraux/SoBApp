package com.example.shadowsofbrimstonecompanion.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.shadowsofbrimstonecompanion.BrimstoneApplication
import com.example.shadowsofbrimstonecompanion.data.entity.Attributes
import com.example.shadowsofbrimstonecompanion.data.entity.Character
import com.example.shadowsofbrimstonecompanion.data.entity.Container
import com.example.shadowsofbrimstonecompanion.data.entity.ContainerWithItems
import com.example.shadowsofbrimstonecompanion.data.entity.Item
import com.example.shadowsofbrimstonecompanion.data.entity.ItemDefinition
import com.example.shadowsofbrimstonecompanion.data.entity.ItemWithDefinition
import com.example.shadowsofbrimstonecompanion.data.entity.Skill
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    val itemsWithDefinitions: Flow<List<ItemWithDefinition>> =
        repository.getItemsWithDefinitionsForCharacter(characterId)
    val skills: Flow<List<Skill>> = repository.getSkillsForCharacter(characterId)
    val allItemDefinitions: Flow<List<ItemDefinition>> = repository.allItemDefinitions
    val containers: Flow<List<ContainerWithItems>> =
        repository.getContainersForCharacter(characterId)
    val stashes: Flow<List<ContainerWithItems>> = repository.getStashes()

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

    // Combined data for container management
    val storageData: StateFlow<StorageState> = combine(
        containers,
        stashes,
        itemsWithDefinitions
    ) { containers, stashes, itemsWithDefinitions ->
        StorageState(
            containers = containers,
            stashes = stashes,
            allItems = itemsWithDefinitions,
            looseItems = itemsWithDefinitions.filter { it.item.containerId == null }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = StorageState()
    )

    // State flow for UI messages
    private val _uiMessage = MutableStateFlow<String?>(null)
    val uiMessage: StateFlow<String?> = _uiMessage.asStateFlow()

    // Selected tab for container management
    private val _selectedStorageTab = MutableStateFlow(StorageTab.CONTAINERS)
    val selectedStorageTab: StateFlow<StorageTab> = _selectedStorageTab.asStateFlow()

    // Show container screen state
    private val _showContainerScreen = MutableStateFlow(false)
    val showContainerScreen: StateFlow<Boolean> = _showContainerScreen.asStateFlow()

    // Function to clear UI messages
    fun clearUiMessage() {
        _uiMessage.value = null
    }

    // Function to toggle container screen visibility
    fun toggleContainerScreen(show: Boolean) {
        _showContainerScreen.value = show
    }

    // Function to change storage tab
    fun setStorageTab(tab: StorageTab) {
        _selectedStorageTab.value = tab
    }

    // State class to hold all character detail data
    data class CharacterDetailState(
        val character: Character? = null,
        val attributes: Attributes? = null,
        val itemsWithDefinitions: List<ItemWithDefinition> = emptyList(),
        val skills: List<Skill> = emptyList(),
        val allItemDefinitions: List<ItemDefinition> = emptyList(),
        val isLoading: Boolean = true
    )

    // State class for storage management
    data class StorageState(
        val containers: List<ContainerWithItems> = emptyList(),
        val stashes: List<ContainerWithItems> = emptyList(),
        val allItems: List<ItemWithDefinition> = emptyList(),
        val looseItems: List<ItemWithDefinition> = emptyList()
    )

    // Enum for storage tabs
    enum class StorageTab {
        CONTAINERS, STASHES
    }

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

    /**
     * Calculates the total anvil weight of all items carried by the character
     * @return Total number of anvil symbols
     */
    fun calculateTotalAnvilWeight(): Int {
        return characterData.value.itemsWithDefinitions.sumOf { itemWithDef ->
            // Only count items not in containers toward encumbrance
            if (itemWithDef.item.containerId == null) {
                itemWithDef.definition.anvilWeight * itemWithDef.item.quantity
            } else {
                0
            }
        }
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
            // Get the item's definition to check its equipment slot
            val itemDef = characterData.value.itemsWithDefinitions
                .find { it.item.id == item.id }?.definition

            if (itemDef?.equipSlot == null) {
                // Item has no equip slot, just update its state
                val updatedItem = item.copy(equipped = !item.equipped)
                repository.updateItem(updatedItem)
                return@launch
            }

            // Handling unequipping (always allowed)
            if (item.equipped) {
                val updatedItem = item.copy(equipped = false)
                repository.updateItem(updatedItem)
                return@launch
            }

            // Handling equipping - need to check slot availability
            val equippedItems = characterData.value.itemsWithDefinitions
                .filter { it.item.equipped }

            // Special handling for "Two-Handed" items
            if (itemDef.equipSlot == "Two-Handed") {
                // Check if any hand slot is occupied
                val handSlotOccupied = equippedItems.any {
                    it.definition.equipSlot == "Hand" || it.definition.equipSlot == "Two-Handed"
                }

                if (handSlotOccupied) {
                    // Cannot equip - hand slots are occupied
                    _uiMessage.value =
                        "Cannot equip ${itemDef.name} - unequip items from your hands first"
                    return@launch
                }
            } else if (itemDef.equipSlot == "Hand") {
                // Check if two-handed item is equipped or both hands are full
                val twoHandedEquipped = equippedItems.any {
                    it.definition.equipSlot == "Two-Handed"
                }

                if (twoHandedEquipped) {
                    // Cannot equip - two-handed item is equipped
                    _uiMessage.value =
                        "Cannot equip ${itemDef.name} - unequip your two-handed item first"
                    return@launch
                }

                // Count equipped hand items
                val handItemsCount = equippedItems.count {
                    it.definition.equipSlot == "Hand"
                }

                if (handItemsCount >= 2) {
                    // Cannot equip - both hands are full
                    _uiMessage.value = "Cannot equip ${itemDef.name} - both hands are full"
                    return@launch
                }
            } else {
                // For all other slots, check if the specific slot is already occupied
                val slotOccupied = equippedItems.any {
                    it.definition.equipSlot == itemDef.equipSlot
                }

                if (slotOccupied) {
                    // Cannot equip - slot is occupied
                    _uiMessage.value =
                        "Cannot equip ${itemDef.name} - ${itemDef.equipSlot} slot is already occupied"
                    return@launch
                }
            }

            // If we got here, we can equip the item
            val updatedItem = item.copy(equipped = true)
            repository.updateItem(updatedItem)
        }
    }

    fun deleteItem(item: Item) {
        viewModelScope.launch {
            repository.deleteItem(item)
        }
    }

    fun sellItem(item: Item, percentageValue: Int) {
        viewModelScope.launch {
            // Get the current character
            characterData.value.character?.let { character ->
                // Get the item definition to calculate value
                val itemDefinition = characterData.value.itemsWithDefinitions
                    .find { it.item.id == item.id }?.definition

                if (itemDefinition != null) {
                    // Calculate gold to add based on percentage
                    val goldValue = itemDefinition.goldValue * item.quantity
                    val goldToAdd = (goldValue * percentageValue) / 100

                    // Update character's gold
                    val updatedCharacter = character.copy(
                        gold = character.gold + goldToAdd
                    )
                    updateCharacter(updatedCharacter)

                    // Delete the sold item
                    deleteItem(item)
                }
            }
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

    // Container management functions
    fun createContainer(
        itemId: Long,
        maxCapacity: Int,
        acceptedItemTypes: List<String> = emptyList(),
        isStash: Boolean = false,
        name: String? = null
    ) {
        viewModelScope.launch {
            repository.createContainer(itemId, maxCapacity, acceptedItemTypes, isStash, name)
        }
    }

    fun createStash(name: String, capacity: Int, acceptedTypes: List<String> = emptyList()) {
        viewModelScope.launch {
            // Create a virtual item for the stash
            val stashItemId = repository.insertItemDefinition(
                ItemDefinition(
                    name = name,
                    description = "A permanent storage location",
                    type = "Stash",
                    keywords = listOf("Container", "Storage"),
                    isContainer = true,
                    containerCapacity = capacity,
                    containerAcceptedTypes = acceptedTypes
                )
            )

            // Create the container
            repository.createContainer(
                itemId = stashItemId,
                maxCapacity = capacity,
                acceptedItemTypes = acceptedTypes,
                isStash = true,
                name = name
            )
        }
    }

    fun moveItemToContainer(itemId: Long, containerId: Long?) {
        viewModelScope.launch {
            // If containerId is null, we're removing from container
            if (containerId == null) {
                repository.moveItemToContainer(itemId, null)
                return@launch
            }

            // Check if container exists and has capacity
            val container = storageData.value.containers.find { it.container.itemId == containerId }
                ?: storageData.value.stashes.find { it.container.itemId == containerId }

            if (container == null) {
                _uiMessage.value = "Container not found"
                return@launch
            }

            // Check capacity
            if (container.items.size >= container.container.maxCapacity) {
                _uiMessage.value = "Container is full"
                return@launch
            }

            // Check item type restrictions
            if (container.container.acceptedItemTypes.isNotEmpty()) {
                val item = storageData.value.allItems.find { it.item.id == itemId }
                if (item != null) {
                    val isAccepted = container.container.acceptedItemTypes.any { acceptedType ->
                        item.definition.type == acceptedType ||
                                item.definition.keywords.contains(acceptedType)
                    }

                    if (!isAccepted) {
                        _uiMessage.value = "This container cannot hold this type of item"
                        return@launch
                    }
                }
            }

            // All checks passed, move the item
            repository.moveItemToContainer(itemId, containerId)
        }
    }

    // Function to check if an item is a container
    fun isItemContainer(itemId: Long): Boolean {
        return storageData.value.allItems.any {
            it.item.id == itemId && it.definition.isContainer
        }
    }

    // Function to create a container from an item
    fun createContainerFromItem(itemId: Long) {
        viewModelScope.launch {
            val itemWithDef = storageData.value.allItems.find { it.item.id == itemId }
            if (itemWithDef != null && itemWithDef.definition.isContainer) {
                repository.createContainer(
                    itemId = itemId,
                    maxCapacity = itemWithDef.definition.containerCapacity,
                    acceptedItemTypes = itemWithDef.definition.containerAcceptedTypes,
                    isStash = false,
                    name = itemWithDef.definition.name
                )
            } else {
                _uiMessage.value = "This item cannot be used as a container"
            }
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