package com.example.ui.map

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class CampusCategory(val displayName: String, val emoji: String) {
    ALL("All Zones", "🗺️"),
    ACADEMIC("Academic & Admin", "🏫"),
    HOSTELS("Mens Hostels", "🛏️"),
    SPORTS("Sports & Lake", "🏃"),
    TRANSIT("Gates & Transit", "🚌")
}

enum class MapLayerMode(val label: String, val emoji: String) {
    STANDARD("Standard Map", "🗺️"),
    ROUTE_ART("Route Art Paths", "🎨"),
    SHUTTLE("Shuttle Transit", "🚌")
}

data class CampusMapUiState(
    val searchQuery: String = "",
    val selectedCategory: CampusCategory = CampusCategory.ALL,
    val selectedLayerMode: MapLayerMode = MapLayerMode.ROUTE_ART, // Default Route Art Paths
    val selectedBuilding: CampusBuilding? = null,
    val selectedArtRoute: CampusArtRouteData = ART_ROUTES[0], // Default to Lake Dolphin
    val isFullscreen: Boolean = false
)

class CampusMapViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(CampusMapUiState())
    val uiState: StateFlow<CampusMapUiState> = _uiState.asStateFlow()

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun selectCategory(category: CampusCategory) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun selectLayerMode(mode: MapLayerMode) {
        _uiState.update { it.copy(selectedLayerMode = mode) }
    }

    fun selectBuilding(building: CampusBuilding?) {
        _uiState.update { it.copy(selectedBuilding = building) }
    }

    fun selectArtRoute(route: CampusArtRouteData) {
        _uiState.update { it.copy(selectedArtRoute = route) }
    }

    fun toggleFullscreen() {
        _uiState.update { it.copy(isFullscreen = !it.isFullscreen) }
    }

    fun getFilteredBuildings(): List<CampusBuilding> {
        val state = _uiState.value
        val query = state.searchQuery.trim().lowercase()
        return CAMPUS_LOCATIONS.filter { building ->
            val matchesCategory = when (state.selectedCategory) {
                CampusCategory.ALL -> true
                CampusCategory.ACADEMIC -> building.category == CampusCategory.ACADEMIC
                CampusCategory.HOSTELS -> building.category == CampusCategory.HOSTELS
                CampusCategory.SPORTS -> building.category == CampusCategory.SPORTS
                CampusCategory.TRANSIT -> building.category == CampusCategory.TRANSIT
            }
            val matchesQuery = query.isEmpty() ||
                    building.name.lowercase().contains(query) ||
                    building.shortCode.lowercase().contains(query) ||
                    building.description.lowercase().contains(query)
            matchesCategory && matchesQuery
        }
    }
}
