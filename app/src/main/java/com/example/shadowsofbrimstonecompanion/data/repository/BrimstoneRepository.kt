package com.example.shadowsofbrimstonecompanion.data.repository

import com.example.shadowsofbrimstonecompanion.data.dao.AttributesDao
import com.example.shadowsofbrimstonecompanion.data.dao.CharacterClassDefinitionDao
import com.example.shadowsofbrimstonecompanion.data.dao.CharacterDao
import com.example.shadowsofbrimstonecompanion.data.dao.ContainerDao
import com.example.shadowsofbrimstonecompanion.data.dao.ItemDao
import com.example.shadowsofbrimstonecompanion.data.dao.ItemDefinitionDao
import com.example.shadowsofbrimstonecompanion.data.dao.SkillDao
import com.example.shadowsofbrimstonecompanion.data.entity.Attributes
import com.example.shadowsofbrimstonecompanion.data.entity.Character
import com.example.shadowsofbrimstonecompanion.data.entity.CharacterClassDefinition
import com.example.shadowsofbrimstonecompanion.data.entity.Container
import com.example.shadowsofbrimstonecompanion.data.entity.ContainerWithItems
import com.example.shadowsofbrimstonecompanion.data.entity.Item
import com.example.shadowsofbrimstonecompanion.data.entity.ItemDefinition
import com.example.shadowsofbrimstonecompanion.data.entity.ItemWithDefinition
import com.example.shadowsofbrimstonecompanion.data.entity.Skill
import kotlinx.coroutines.flow.Flow

class BrimstoneRepository(
    private val characterDao: CharacterDao,
    private val attributesDao: AttributesDao,
    private val itemDao: ItemDao,
    private val skillDao: SkillDao,
    private val characterClassDefinitionDao: CharacterClassDefinitionDao,
    private val itemDefinitionDao: ItemDefinitionDao,
    private val containerDao: ContainerDao
    ) {
    // Character operations
    val allCharacters: Flow<List<Character>> = characterDao.getAllCharacters()

    suspend fun insertCharacter(character: Character): Long {
        return characterDao.insert(character)
    }

    suspend fun updateCharacter(character: Character) {
        characterDao.update(character)
    }

    suspend fun deleteCharacter(character: Character) {
        characterDao.delete(character)
    }

    fun getCharacterById(characterId: Long): Flow<Character?> {
        return characterDao.getCharacterById(characterId)
    }

    // Attributes operations
    suspend fun insertAttributes(attributes: Attributes) {
        attributesDao.insert(attributes)
    }

    suspend fun updateAttributes(attributes: Attributes) {
        attributesDao.update(attributes)
    }

    fun getAttributesForCharacter(characterId: Long): Flow<Attributes?> {
        return attributesDao.getAttributesForCharacter(characterId)
    }

    // Item operations
    suspend fun insertItem(item: Item): Long {
        return itemDao.insert(item)
    }

    suspend fun updateItem(item: Item) {
        itemDao.update(item)
    }

    suspend fun deleteItem(item: Item) {
        itemDao.delete(item)
    }

    fun getItemsForCharacter(characterId: Long): Flow<List<Item>> {
        return itemDao.getItemsForCharacter(characterId)
    }

    // Container operations
    suspend fun createContainer(itemId: Long, maxCapacity: Int, acceptedItemTypes: List<String> = emptyList(), isStash: Boolean = false, name: String? = null) {
        val container = Container(itemId, maxCapacity, acceptedItemTypes, isStash, name)
        containerDao.insert(container)
    }

    fun getContainerWithItems(containerId: Long): Flow<ContainerWithItems?> {
        return containerDao.getContainerWithItems(containerId)
    }

    fun getContainersForCharacter(characterId: Long): Flow<List<ContainerWithItems>> {
        return containerDao.getContainersForCharacter(characterId)
    }

    fun getStashes(): Flow<List<ContainerWithItems>> {
        return containerDao.getStashes()
    }

    suspend fun moveItemToContainer(itemId: Long, containerId: Long?) {
        val item = itemDao.getItemById(itemId)
        if (item != null) {
            val updatedItem = item.copy(containerId = containerId)
            itemDao.update(updatedItem)
        }
    }

    // Skill operations
    suspend fun insertSkill(skill: Skill): Long {
        return skillDao.insert(skill)
    }

    suspend fun updateSkill(skill: Skill) {
        skillDao.update(skill)
    }

    suspend fun deleteSkill(skill: Skill) {
        skillDao.delete(skill)
    }

    fun getSkillsForCharacter(characterId: Long): Flow<List<Skill>> {
        return skillDao.getSkillsForCharacter(characterId)
    }

    fun getItemsWithDefinitionsForCharacter(characterId: Long): Flow<List<ItemWithDefinition>> {
        return itemDao.getItemsWithDefinitionsForCharacter(characterId)
    }
    // Character Class Definition operations
    val allClassDefinitions: Flow<List<CharacterClassDefinition>> = characterClassDefinitionDao.getAllClassDefinitions()

    suspend fun insertClassDefinition(characterClass: CharacterClassDefinition): Long {
        return characterClassDefinitionDao.insert(characterClass)
    }

    suspend fun insertAllClassDefinitions(classes: List<CharacterClassDefinition>) {
        characterClassDefinitionDao.insertAll(classes)
    }

    suspend fun updateClassDefinition(characterClass: CharacterClassDefinition) {
        characterClassDefinitionDao.update(characterClass)
    }

    suspend fun deleteClassDefinition(characterClass: CharacterClassDefinition) {
        characterClassDefinitionDao.delete(characterClass)
    }

    suspend fun getClassDefinitionCount(): Int {
        return characterClassDefinitionDao.getClassCount()
    }
    val allItemDefinitions: Flow<List<ItemDefinition>> = itemDefinitionDao.getAllItemDefinitions()

    fun getItemDefinitionsByType(itemType: String): Flow<List<ItemDefinition>> {
        return itemDefinitionDao.getItemDefinitionsByType(itemType)
    }

    suspend fun insertItemDefinition(item: ItemDefinition): Long {
        return itemDefinitionDao.insert(item)
    }

    suspend fun insertAllItemDefinitions(items: List<ItemDefinition>) {
        itemDefinitionDao.insertAll(items)
    }

    suspend fun getItemDefinitionCount(): Int {
        return itemDefinitionDao.getItemCount()
    }
}

