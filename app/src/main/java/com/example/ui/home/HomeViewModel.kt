package com.example.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.UserProfileEntity
import com.example.data.model.WalkRouteEntity
import com.example.data.repository.RouteRepository
import com.example.data.sync.SyncState
import com.example.service.PedometerService
import com.example.service.PedometerState
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class HomeUiState(
    val routes: List<WalkRouteEntity> = emptyList(),
    val favoriteRoutes: List<WalkRouteEntity> = emptyList(),
    val selectedFilter: HomeFilter = HomeFilter.ALL,
    val todaysRoute: WalkRouteEntity? = null,
    val userProfile: UserProfileEntity? = null,
    val syncState: SyncState = SyncState.Idle,
    val pedometerState: PedometerState = PedometerState(),
    val isLoading: Boolean = false
)

enum class HomeFilter {
    ALL,
    FAVORITES
}

private data class BaseHomeData(
    val routes: List<WalkRouteEntity>,
    val favoriteRoutes: List<WalkRouteEntity>,
    val selectedFilter: HomeFilter,
    val userProfile: UserProfileEntity?
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = RouteRepository(application)

    private val _selectedFilter = MutableStateFlow(HomeFilter.ALL)
    val selectedFilter: StateFlow<HomeFilter> = _selectedFilter.asStateFlow()

    init {
        // Ensure PedometerService is running for daily step tracking
        PedometerService.start(application)
    }

    private val baseDataFlow = combine(
        repository.allRoutes,
        repository.favoriteRoutes,
        _selectedFilter,
        repository.userProfile
    ) { allRoutes, favRoutes, filter, profile ->
        BaseHomeData(allRoutes, favRoutes, filter, profile)
    }

    val uiState: StateFlow<HomeUiState> = combine(
        baseDataFlow,
        repository.syncState,
        PedometerService.pedometerState
    ) { base, syncState, pedoState ->
        HomeUiState(
            routes = base.routes,
            favoriteRoutes = base.favoriteRoutes,
            selectedFilter = base.selectedFilter,
            todaysRoute = base.routes.firstOrNull(),
            userProfile = base.userProfile,
            syncState = syncState,
            pedometerState = pedoState,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState(isLoading = true)
    )

    fun setFilter(filter: HomeFilter) {
        _selectedFilter.value = filter
    }

    fun syncFirestore() {
        viewModelScope.launch {
            repository.syncWithFirestore()
        }
    }

    fun toggleFavorite(routeId: Long, isFavorite: Boolean) {
        viewModelScope.launch {
            repository.toggleFavorite(routeId, isFavorite)
        }
    }

    fun restartPedometerService() {
        PedometerService.start(getApplication())
    }

    fun resetDailySteps() {
        PedometerService.resetDaily(getApplication())
    }

    fun simulateWalkSteps(steps: Int = 250) {
        PedometerService.addManualSteps(getApplication(), steps)
    }
}

