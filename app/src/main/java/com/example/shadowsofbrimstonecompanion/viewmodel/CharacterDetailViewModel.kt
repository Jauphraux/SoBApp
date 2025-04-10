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
            allItems = itemsWithDefinitions
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
        val allItems: List<ItemWithDefinition> = emptyList()
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

            // Get the item being moved
            val itemToMove = storageData.value.allItems.find { it.item.id == itemId }
            if (itemToMove == null) {
                _uiMessage.value = "Item not found"
                return@launch
            }

            // Check capacity
            if (container.items.size >= container.container.maxCapacity) {
                _uiMessage.value = "Container is full"
                return@launch
            }

            // Check item type restrictions
            if (container.container.acceptedItemTypes.isNotEmpty()) {
                val isAccepted = container.container.acceptedItemTypes.any { acceptedType ->
                    itemToMove.definition.type == acceptedType ||
                            itemToMove.definition.keywords.contains(acceptedType)
                }

                if (!isAccepted) {
                    _uiMessage.value = "This container cannot hold this type of item"
                    return@launch
                }
            }

            // Check if there's already an item of the same type in the container
            val existingItem = container.items.find { existingItem ->
                val existingItemDef = storageData.value.allItems
                    .find { it.item.id == existingItem.id }?.definition
                existingItemDef?.type == itemToMove.definition.type
            }

            if (existingItem != null) {
                // Update quantity of existing item
                val updatedItem = existingItem.copy(
                    quantity = existingItem.quantity + itemToMove.item.quantity
                )
                repository.updateItem(updatedItem)
                // Delete the original item since we've merged it
                repository.deleteItem(itemToMove.item)
            } else {
                // Move the item to the container
                repository.moveItemToContainer(itemId, containerId)
            }
        }
    }

    /**
     * Converts an item with container properties into an actual usable container
     * @param item The item to convert into a container
     */
    fun useItemAsContainer(item: Item) {
        viewModelScope.launch {
            // Find the item definition to check container properties
            val itemWithDef = storageData.value.allItems.find { it.item.id == item.id }

            if (itemWithDef != null && itemWithDef.definition.isContainer) {
                // Check if container already exists for this item
                val existingContainers = storageData.value.containers
                val alreadyContainer = existingContainers.any { it.container.itemId == item.id }

                if (!alreadyContainer) {
                    // Create the container entry for this item
                    repository.createContainer(
                        itemId = item.id,
                        maxCapacity = itemWithDef.definition.containerCapacity,
                        acceptedItemTypes = itemWithDef.definition.containerAcceptedTypes,
                        isStash = false,
                        name = itemWithDef.definition.name
                    )

                    // Update the item to be its own container
                    val updatedItem = item.copy(containerId = item.id)
                    repository.updateItem(updatedItem)

                    _uiMessage.value = "${itemWithDef.definition.name} is now usable as a container"
                } else {
                    _uiMessage.value = "This item is already set up as a container"
                }
            } else {
                _uiMessage.value = "This item cannot be used as a container"
            }
        }
    }

    // Add these functions to CharacterDetailViewModel.kt

    /**
     * Increases dark stone count by 1
     */
    fun increaseDarkstone() {
        characterData.value.character?.let { character ->
            val updatedCharacter = character.copy(darkstone = character.darkstone + 1)
            updateCharacter(updatedCharacter)
        }
    }

    /**
     * Decreases dark stone count by 1, not going below 0
     */
    fun decreaseDarkstone() {
        characterData.value.character?.let { character ->
            if (character.darkstone > 0) {
                val updatedCharacter = character.copy(darkstone = character.darkstone - 1)
                updateCharacter(updatedCharacter)
            }
        }
    }

    /**
     * Gets count of dark stone stored in containers
     */
    fun getStoredDarkstoneCount(): Int {
        val darkstoneContainers = storageData.value.containers.filter { container ->
            container.container.acceptedItemTypes.contains("Dark Stone")
        }

        val storedCount = darkstoneContainers.sumOf { container ->
            container.items.count { item ->
                val definition = storageData.value.allItems
                    .find { it.item.id == item.id }?.definition
                definition?.type == "Dark Stone"
            }
        }

        return storedCount
    }

    /**
     * Store dark stone in an appropriate container
     */
    fun storeDarkstoneInContainer(containerId: Long) {
        viewModelScope.launch {
            characterData.value.character?.let { character ->
                if (character.darkstone <= 0) {
                    _uiMessage.value = "No dark stone to store"
                    return@launch
                }

                // Check if container accepts dark stone
                val container = storageData.value.containers.find {
                    it.container.id == containerId
                }

                if (container == null) {
                    _uiMessage.value = "Container not found"
                    return@launch
                }

                val acceptsDarkstone = container.container.acceptedItemTypes.isEmpty() ||
                        container.container.acceptedItemTypes.contains("Dark Stone")

                if (!acceptsDarkstone) {
                    _uiMessage.value = "This container cannot store dark stone"
                    return@launch
                }

                // Check container capacity
                if (container.items.size >= container.container.maxCapacity) {
                    _uiMessage.value = "Container is full"
                    return@launch
                }

                // Find existing dark stone item in container
                val existingDarkstoneItem = container.items.find { item ->
                    val definition = storageData.value.allItems
                        .find { it.item.id == item.id }?.definition
                    definition?.type == "Dark Stone"
                }

                if (existingDarkstoneItem != null) {
                    // Update quantity of existing dark stone item
                    val updatedItem = existingDarkstoneItem.copy(
                        quantity = existingDarkstoneItem.quantity + 1
                    )
                    repository.updateItem(updatedItem)
                } else {
                    // Create a new dark stone item
                    val darkstoneItemDef = storageData.value.allItems.find { it.definition.type == "Dark Stone" }?.definition

                    if (darkstoneItemDef == null) {
                        // Create a dark stone item definition if none exists
                        val newDarkstoneDefId = repository.insertItemDefinition(
                            ItemDefinition(
                                name = "Dark Stone",
                                description = "A piece of mysterious otherworldly stone that radiates corruption.",
                                type = "Dark Stone",
                                keywords = listOf("Artifact", "Valuable"),
                                anvilWeight = 0,
                                darkStoneCount = 1,
                                goldValue = 50
                            )
                        )

                        // Create item
                        repository.insertItem(
                            Item(
                                characterId = characterId,
                                itemDefinitionId = newDarkstoneDefId,
                                quantity = 1,
                                notes = "From character's dark stone supply",
                                containerId = containerId
                            )
                        )
                    } else {
                        // Use existing dark stone definition
                        repository.insertItem(
                            Item(
                                characterId = characterId,
                                itemDefinitionId = darkstoneItemDef.id,
                                quantity = 1,
                                notes = "From character's dark stone supply",
                                containerId = containerId
                            )
                        )
                    }
                }

                // Reduce character's dark stone count
                val updatedCharacter = character.copy(darkstone = character.darkstone - 1)
                updateCharacter(updatedCharacter)

                _uiMessage.value = "Dark stone stored in container"
            }
        }
    }

    /**
     * Remove dark stone from container and add to character's supply
     */
    fun retrieveDarkstoneFromContainer(itemId: Long) {
        viewModelScope.launch {
            // Get the item
            val item = storageData.value.allItems.find { it.item.id == itemId }

            if (item == null) {
                _uiMessage.value = "Item not found"
                return@launch
            }

            // Check if it's dark stone
            val isDarkstone = item.definition.type == "Dark Stone"

            if (!isDarkstone) {
                _uiMessage.value = "Selected item is not dark stone"
                return@launch
            }

            // If quantity is more than 1, reduce quantity instead of deleting
            if (item.item.quantity > 1) {
                val updatedItem = item.item.copy(quantity = item.item.quantity - 1)
                repository.updateItem(updatedItem)
            } else {
                // Remove the item if quantity is 1
                repository.deleteItem(item.item)
            }

            // Increase character's dark stone count
            characterData.value.character?.let { character ->
                val updatedCharacter = character.copy(
                    darkstone = character.darkstone + 1
                )
                updateCharacter(updatedCharacter)
            }

            _uiMessage.value = "Dark stone added to your supply"
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