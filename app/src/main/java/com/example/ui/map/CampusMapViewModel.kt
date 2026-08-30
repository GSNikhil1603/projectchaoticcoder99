package com.example.ui.map

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.generator.RouteArtEngine
import com.example.data.model.GpsCoordinate
import com.example.data.model.PointF
import com.example.data.model.WalkRouteEntity
import com.example.data.repository.RouteRepository
import com.example.service.LocationService
import com.example.util.GpsDistanceCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class CoordinateWaypoint(
    val id: String = UUID.randomUUID().toString(),
    val latitude: Double,
    val longitude: Double,
    val label: String = "Waypoint",
    val altitude: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis()
)

data class CoordinateMapUiState(
    val isTracking: Boolean = false,
    val isPaused: Boolean = false,
    val currentLat: Double? = null,
    val currentLng: Double? = null,
    val gpsAccuracyMeters: Float = 0f,
    val altitudeMeters: Double = 0.0,
    val speedKmh: Double = 0.0,
    val totalDistanceMeters: Double = 0.0,
    val coordinates: List<GpsCoordinate> = emptyList(),
    val waypoints: List<CoordinateWaypoint> = emptyList(),
    val isFullscreen: Boolean = false,
    val isGridOverlayEnabled: Boolean = true,
    val showAddCoordinateDialog: Boolean = false,
    val selectedWaypoint: CoordinateWaypoint? = null,
    val newlySavedRouteId: Long? = null
)

class CampusMapViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = RouteRepository(application)

    private val _uiState = MutableStateFlow(CoordinateMapUiState())
    val uiState: StateFlow<CoordinateMapUiState> = _uiState.asStateFlow()

    init {
        // Collect real location updates from LocationService
        viewModelScope.launch {
            LocationService.locationUpdates.collect { update ->
                val lat = update.location.latitude
                val lng = update.location.longitude
                val newGps = GpsCoordinate(
                    latitude = lat,
                    longitude = lng,
                    altitudeMeters = update.altitudeMeters,
                    accuracyMeters = if (update.location.hasAccuracy()) update.location.accuracy else 0f,
                    speedKmh = update.currentSpeedKmh,
                    timestamp = update.timestamp
                )

                _uiState.update { current ->
                    val updatedList = if (current.isTracking && !current.isPaused) {
                        current.coordinates + newGps
                    } else {
                        current.coordinates
                    }

                    val totalDist = if (updatedList.size >= 2) {
                        GpsDistanceCalculator.calculateTotalDistanceMeters(updatedList)
                    } else {
                        current.totalDistanceMeters
                    }

                    current.copy(
                        currentLat = lat,
                        currentLng = lng,
                        gpsAccuracyMeters = if (update.location.hasAccuracy()) update.location.accuracy else 0f,
                        altitudeMeters = update.altitudeMeters,
                        speedKmh = update.currentSpeedKmh,
                        coordinates = updatedList,
                        totalDistanceMeters = totalDist
                    )
                }
            }
        }
    }

    fun startTracking() {
        LocationService.start(getApplication())
        _uiState.update { it.copy(isTracking = true, isPaused = false) }
    }

    fun pauseTracking() {
        LocationService.pause(getApplication())
        _uiState.update { it.copy(isPaused = true) }
    }

    fun resumeTracking() {
        LocationService.resume(getApplication())
        _uiState.update { it.copy(isPaused = false) }
    }

    fun stopTracking() {
        LocationService.stop(getApplication())
        _uiState.update { it.copy(isTracking = false, isPaused = false) }
    }

    fun clearAllCoordinates() {
        _uiState.update {
            it.copy(
                coordinates = emptyList(),
                waypoints = emptyList(),
                totalDistanceMeters = 0.0,
                selectedWaypoint = null
            )
        }
    }

    fun addManualCoordinate(lat: Double, lng: Double, label: String = "Coordinate Point") {
        val newGps = GpsCoordinate(
            latitude = lat,
            longitude = lng,
            altitudeMeters = _uiState.value.altitudeMeters,
            accuracyMeters = 1.0f,
            speedKmh = 0.0,
            timestamp = System.currentTimeMillis()
        )
        val newWaypoint = CoordinateWaypoint(
            latitude = lat,
            longitude = lng,
            label = label.ifBlank { "Point ${uiState.value.coordinates.size + 1}" },
            altitude = _uiState.value.altitudeMeters
        )

        _uiState.update { current ->
            val updatedCoords = current.coordinates + newGps
            val updatedWaypoints = current.waypoints + newWaypoint
            val totalDist = if (updatedCoords.size >= 2) {
                GpsDistanceCalculator.calculateTotalDistanceMeters(updatedCoords)
            } else {
                0.0
            }

            current.copy(
                currentLat = lat,
                currentLng = lng,
                coordinates = updatedCoords,
                waypoints = updatedWaypoints,
                totalDistanceMeters = totalDist,
                showAddCoordinateDialog = false
            )
        }
    }

    fun toggleGridOverlay() {
        _uiState.update { it.copy(isGridOverlayEnabled = !it.isGridOverlayEnabled) }
    }

    fun toggleFullscreen() {
        _uiState.update { it.copy(isFullscreen = !it.isFullscreen) }
    }

    fun openAddCoordinateDialog(show: Boolean) {
        _uiState.update { it.copy(showAddCoordinateDialog = show) }
    }

    fun selectWaypoint(waypoint: CoordinateWaypoint?) {
        _uiState.update { it.copy(selectedWaypoint = waypoint) }
    }

    fun saveCoordinatesAsArtwork(onComplete: (Long) -> Unit) {
        viewModelScope.launch {
            val coords = _uiState.value.coordinates
            if (coords.isEmpty()) return@launch

            val distanceKm = (_uiState.value.totalDistanceMeters / 1000.0).coerceAtLeast(0.5)

            // Convert GPS coordinates into canvas points
            val minLat = coords.minOf { it.latitude }
            val maxLat = coords.maxOf { it.latitude }
            val minLng = coords.minOf { it.longitude }
            val maxLng = coords.maxOf { it.longitude }
            val latSpan = (maxLat - minLat).coerceAtLeast(0.0001)
            val lngSpan = (maxLng - minLng).coerceAtLeast(0.0001)

            val rawPoints = coords.map {
                val nx = (((it.longitude - minLng) / lngSpan) * 800.0 + 100.0).toFloat()
                val ny = ((1.0 - ((it.latitude - minLat) / latSpan)) * 800.0 + 100.0).toFloat()
                PointF(nx, ny, it.altitudeMeters)
            }

            val normalized = RouteArtEngine.normalizePoints(rawPoints)
            val simplified = RouteArtEngine.simplifyPoints(normalized, 3.0f)
            val (shapeName, category) = RouteArtEngine.classifyShape(simplified, distanceKm)
            val blobs = RouteArtEngine.generateColorBlobs(simplified)

            val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
            val isoFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val now = Date()

            val route = WalkRouteEntity(
                dateString = dateFormat.format(now),
                isoDate = isoFormat.format(now),
                steps = (distanceKm * 1350).toInt().coerceAtLeast(200),
                distanceKm = Math.round(distanceKm * 100.0) / 100.0,
                durationMinutes = ((coords.size * 5) / 60).coerceAtLeast(5),
                calories = (distanceKm * 55).toInt().coerceAtLeast(15),
                title = "Coordinate Walk",
                shapeName = shapeName,
                shapeCategory = category,
                pointsJson = RouteArtEngine.pointsToJson(simplified),
                blobsJson = RouteArtEngine.blobsToJson(blobs),
                strokesJson = "[]",
                stickersJson = "[]",
                isFavorite = false,
                campusName = "GPS Walk",
                artStyle = "Pastel Bloom",
                createdAt = System.currentTimeMillis()
            )

            val newId = repository.insertRoute(route)
            _uiState.update { it.copy(newlySavedRouteId = newId) }
            onComplete(newId)
        }
    }

    override fun onCleared() {
        LocationService.stop(getApplication())
        super.onCleared()
    }
}
