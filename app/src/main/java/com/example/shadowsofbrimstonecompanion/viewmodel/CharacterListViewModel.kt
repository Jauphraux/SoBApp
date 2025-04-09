package com.example.shadowsofbrimstonecompanion.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.shadowsofbrimstonecompanion.BrimstoneApplication
import com.example.shadowsofbrimstonecompanion.data.entity.Character
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CharacterListViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = (application as BrimstoneApplication).repository

    // Characters as a StateFlow that can be collected in Compose
    val characters: StateFlow<List<Character>> = repository.allCharacters
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    fun deleteCharacter(character: Character) {
        viewModelScope.launch {
            repository.deleteCharacter(character)
        }
    }
}
