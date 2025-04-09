package com.example.shadowsofbrimstonecompanion.ui.navigation

object NavigationRoutes {
    const val CHARACTER_LIST = "characterList"
    const val CHARACTER_DETAIL = "characterDetail"
    const val CHARACTER_CREATE = "characterCreate"

    // Routes with arguments
    fun characterDetailRoute(characterId: Long): String = "characterDetail/$characterId"
}
