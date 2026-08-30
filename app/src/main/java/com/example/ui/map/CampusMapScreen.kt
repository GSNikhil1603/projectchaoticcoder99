package com.example.ui.map

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.GpsCoordinate
import com.example.ui.theme.*

@Composable
fun CampusMapScreen(
    onNavigateToTracker: (String?) -> Unit,
    onNavigateToChallenges: () -> Unit = {},
    viewModel: CampusMapViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var zoomScale by remember { mutableFloatStateOf(1.0f) }
    var panOffsetX by remember { mutableFloatStateOf(0f) }
    var panOffsetY by remember { mutableFloatStateOf(0f) }

    // Pulsing animation for active GPS fix indicator
    val infiniteTransition = rememberInfiniteTransition(label = "gpsPulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    // Permission launcher for GPS
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (fineGranted || coarseGranted) {
            viewModel.startTracking()
        }
    }

    fun handleStartTracking() {
        val hasFine = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (hasFine || hasCoarse) {
            viewModel.startTracking()
        } else {
            val permissions = mutableListOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                permissions.add(Manifest.permission.POST_NOTIFICATIONS)
            }
            permissionLauncher.launch(permissions.toTypedArray())
        }
    }

    // Add Coordinate Dialog
    if (uiState.showAddCoordinateDialog) {
        var inputLat by remember { mutableStateOf(uiState.currentLat?.toString() ?: "12.9716") }
        var inputLng by remember { mutableStateOf(uiState.currentLng?.toString() ?: "79.1584") }
        var inputLabel by remember { mutableStateOf("Waypoint ${uiState.coordinates.size + 1}") }
        var inputError by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { viewModel.openAddCoordinateDialog(false) },
            shape = RoundedCornerShape(24.dp),
            containerColor = Color.White,
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AddLocation,
                        contentDescription = null,
                        tint = Color(0xFF059669)
                    )
                    Text(
                        text = "Add GPS Coordinate",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        ),
                        color = Color(0xFF111827)
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Enter precise latitude and longitude or capture your current location.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF6B7280)
                    )

                    OutlinedTextField(
                        value = inputLat,
                        onValueChange = {
                            inputLat = it
                            inputError = null
                        },
                        label = { Text("Latitude (°)") },
                        placeholder = { Text("e.g. 12.97159") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("input_latitude")
                    )

                    OutlinedTextField(
                        value = inputLng,
                        onValueChange = {
                            inputLng = it
                            inputError = null
                        },
                        label = { Text("Longitude (°)") },
                        placeholder = { Text("e.g. 79.15842") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("input_longitude")
                    )

                    OutlinedTextField(
                        value = inputLabel,
                        onValueChange = { inputLabel = it },
                        label = { Text("Waypoint Label (Optional)") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("input_label")
                    )

                    if (uiState.currentLat != null && uiState.currentLng != null) {
                        TextButton(
                            onClick = {
                                inputLat = String.format("%.6f", uiState.currentLat)
                                inputLng = String.format("%.6f", uiState.currentLng)
                            },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(Icons.Default.MyLocation, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Use Current GPS Location", fontSize = 12.sp, color = Color(0xFF059669))
                        }
                    }

                    if (inputError != null) {
                        Text(
                            text = inputError!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val latVal = inputLat.toDoubleOrNull()
                        val lngVal = inputLng.toDoubleOrNull()
                        if (latVal == null || latVal < -90.0 || latVal > 90.0) {
                            inputError = "Please enter a valid Latitude between -90 and 90"
                            return@Button
                        }
                        if (lngVal == null || lngVal < -180.0 || lngVal > 180.0) {
                            inputError = "Please enter a valid Longitude between -180 and 180"
                            return@Button
                        }
                        viewModel.addManualCoordinate(latVal, lngVal, inputLabel)
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669))
                ) {
                    Text("Plot Point", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.openAddCoordinateDialog(false) }) {
                    Text("Cancel", color = Color(0xFF6B7280))
                }
            }
        )
    }

    Scaffold(
        containerColor = Color(0xFFF9FAFB),
        contentWindowInsets = WindowInsets.safeDrawing
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 1. Top Coordinate HUD Header
            Surface(
                color = Color.White,
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Header Row: Title & Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (uiState.isTracking) Color(0xFF10B981) else Color(0xFF6B7280)
                                        )
                                )
                                Text(
                                    text = "Coordinate Map",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 20.sp,
                                        letterSpacing = (-0.02).em
                                    ),
                                    color = Color(0xFF111827)
                                )
                            }
                            Text(
                                text = if (uiState.isTracking) "Live GPS recording in progress" else "Plot & view real GPS coordinates",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF6B7280)
                            )
                        }

                        // Top Action Icons
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Add Coordinate Button
                            IconButton(
                                onClick = { viewModel.openAddCoordinateDialog(true) },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFECFDF5))
                                    .testTag("btn_add_coord")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AddLocationAlt,
                                    contentDescription = "Add Coordinate",
                                    tint = Color(0xFF059669),
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            // Clear All Points Button
                            if (uiState.coordinates.isNotEmpty()) {
                                IconButton(
                                    onClick = { viewModel.clearAllCoordinates() },
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFFEF2F2))
                                        .testTag("btn_clear_coords")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = "Clear Coordinates",
                                        tint = Color(0xFFEF4444),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Coordinate Readout Cards
                    val latDisplay = uiState.currentLat?.let { String.format("%.6f°", it) } ?: "--.------°"
                    val lngDisplay = uiState.currentLng?.let { String.format("%.6f°", it) } ?: "--.------°"

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFFF3F4F6))
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "LATITUDE",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    letterSpacing = 0.5.sp
                                ),
                                color = Color(0xFF6B7280)
                            )
                            Text(
                                text = latDisplay,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 15.sp
                                ),
                                color = Color(0xFF111827)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(28.dp)
                                .background(Color(0xFFD1D5DB))
                        )

                        Column {
                            Text(
                                text = "LONGITUDE",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    letterSpacing = 0.5.sp
                                ),
                                color = Color(0xFF6B7280)
                            )
                            Text(
                                text = lngDisplay,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 15.sp
                                ),
                                color = Color(0xFF111827)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(28.dp)
                                .background(Color(0xFFD1D5DB))
                        )

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "DISTANCE",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    letterSpacing = 0.5.sp
                                ),
                                color = Color(0xFF6B7280)
                            )
                            Text(
                                text = if (uiState.totalDistanceMeters >= 1000) {
                                    String.format("%.2f km", uiState.totalDistanceMeters / 1000.0)
                                } else {
                                    String.format("%.0f m", uiState.totalDistanceMeters)
                                },
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 15.sp
                                ),
                                color = Color(0xFF059669)
                            )
                        }
                    }
                }
            }

            // 2. Interactive Coordinate Map Canvas
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Color(0xFF181F1C)) // Dark emerald-slate coordinate grid canvas
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            zoomScale = (zoomScale * zoom).coerceIn(0.5f, 5.0f)
                            panOffsetX += pan.x
                            panOffsetY += pan.y
                        }
                    }
            ) {
                // Canvas Drawing Coordinate Grid & Real Paths
                val coords = uiState.coordinates
                val waypoints = uiState.waypoints

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val cx = w / 2f + panOffsetX
                    val cy = h / 2f + panOffsetY

                    // 1. Draw Grid Lines (Lat/Long intervals)
                    val gridSpacing = 60.dp.toPx() * zoomScale
                    val startX = (cx % gridSpacing) - gridSpacing
                    val startY = (cy % gridSpacing) - gridSpacing

                    var x = startX
                    while (x < w + gridSpacing) {
                        drawLine(
                            color = Color(0xFF263A30).copy(alpha = 0.6f),
                            start = Offset(x, 0f),
                            end = Offset(x, h),
                            strokeWidth = 1f
                        )
                        x += gridSpacing
                    }

                    var y = startY
                    while (y < h + gridSpacing) {
                        drawLine(
                            color = Color(0xFF263A30).copy(alpha = 0.6f),
                            start = Offset(0f, y),
                            end = Offset(w, y),
                            strokeWidth = 1f
                        )
                        y += gridSpacing
                    }

                    // Center Crosshair
                    drawLine(
                        color = Color(0xFF059669).copy(alpha = 0.5f),
                        start = Offset(cx - 20f, cy),
                        end = Offset(cx + 20f, cy),
                        strokeWidth = 1.5f
                    )
                    drawLine(
                        color = Color(0xFF059669).copy(alpha = 0.5f),
                        start = Offset(cx, cy - 20f),
                        end = Offset(cx, cy + 20f),
                        strokeWidth = 1.5f
                    )

                    // 2. Draw Recorded Coordinates Path
                    if (coords.isNotEmpty()) {
                        // Calculate coordinate bounding box to normalize to canvas
                        val minLat = coords.minOf { it.latitude }
                        val maxLat = coords.maxOf { it.latitude }
                        val minLng = coords.minOf { it.longitude }
                        val maxLng = coords.maxOf { it.longitude }

                        val latSpan = (maxLat - minLat).coerceAtLeast(0.0001)
                        val lngSpan = (maxLng - minLng).coerceAtLeast(0.0001)

                        val drawWidth = (w * 0.7f) * zoomScale
                        val drawHeight = (h * 0.7f) * zoomScale

                        fun coordToScreen(lat: Double, lng: Double): Offset {
                            val normX = ((lng - minLng) / lngSpan).toFloat()
                            val normY = (1f - ((lat - minLat) / latSpan).toFloat()) // Invert Y for screen
                            val screenX = cx - (drawWidth / 2f) + (normX * drawWidth)
                            val screenY = cy - (drawHeight / 2f) + (normY * drawHeight)
                            return Offset(screenX, screenY)
                        }

                        // Draw path lines
                        if (coords.size >= 2) {
                            val path = Path()
                            val firstPt = coordToScreen(coords[0].latitude, coords[0].longitude)
                            path.moveTo(firstPt.x, firstPt.y)

                            for (i in 1 until coords.size) {
                                val pt = coordToScreen(coords[i].latitude, coords[i].longitude)
                                path.lineTo(pt.x, pt.y)
                            }

                            // Glowing background stroke
                            drawPath(
                                path = path,
                                color = Color(0xFF10B981).copy(alpha = 0.35f),
                                style = Stroke(width = 12f * zoomScale, cap = StrokeCap.Round, join = StrokeJoin.Round)
                            )
                            // Sharp foreground line
                            drawPath(
                                path = path,
                                color = Color(0xFF34D399),
                                style = Stroke(width = 4f * zoomScale, cap = StrokeCap.Round, join = StrokeJoin.Round)
                            )
                        }

                        // Draw Start Marker
                        val startScreen = coordToScreen(coords.first().latitude, coords.first().longitude)
                        drawCircle(
                            color = Color(0xFF10B981),
                            radius = 8.dp.toPx(),
                            center = startScreen
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 4.dp.toPx(),
                            center = startScreen
                        )

                        // Draw Last Point (Live Fix / Pulsing)
                        val lastScreen = coordToScreen(coords.last().latitude, coords.last().longitude)
                        drawCircle(
                            color = Color(0xFF60A5FA).copy(alpha = pulseAlpha),
                            radius = 18.dp.toPx(),
                            center = lastScreen
                        )
                        drawCircle(
                            color = Color(0xFF3B82F6),
                            radius = 8.dp.toPx(),
                            center = lastScreen
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 4.dp.toPx(),
                            center = lastScreen
                        )
                    }
                }

                // Top-Left Coordinates Status Pill
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF111827).copy(alpha = 0.85f),
                    border = BorderStroke(1.dp, Color(0xFF374151)),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.GpsFixed,
                            contentDescription = null,
                            tint = if (uiState.isTracking) Color(0xFF10B981) else Color(0xFF9CA3AF),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "${uiState.coordinates.size} coordinates plotted",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            ),
                            color = Color.White
                        )
                    }
                }

                // Top-Right Reset & Zoom Controls
                Column(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Reset View Button
                    Surface(
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.9f),
                        shadowElevation = 2.dp,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .clickable {
                                zoomScale = 1.0f
                                panOffsetX = 0f
                                panOffsetY = 0f
                            }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.CenterFocusStrong,
                                contentDescription = "Center View",
                                tint = Color(0xFF111827),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Zoom In
                    Surface(
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.9f),
                        shadowElevation = 2.dp,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .clickable {
                                zoomScale = (zoomScale * 1.25f).coerceAtMost(5.0f)
                            }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Zoom In",
                                tint = Color(0xFF111827),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Zoom Out
                    Surface(
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.9f),
                        shadowElevation = 2.dp,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .clickable {
                                zoomScale = (zoomScale / 1.25f).coerceAtLeast(0.5f)
                            }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Remove,
                                contentDescription = "Zoom Out",
                                tint = Color(0xFF111827),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                // If empty coordinates, show friendly guide in center
                if (uiState.coordinates.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(24.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFF111827).copy(alpha = 0.85f))
                            .border(1.dp, Color(0xFF374151), RoundedCornerShape(20.dp))
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(text = "🛰️", fontSize = 36.sp)
                            Text(
                                text = "Coordinate Map Ready",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                ),
                                color = Color.White
                            )
                            Text(
                                text = "Tap 'Start GPS Walk' or enter custom latitude & longitude coordinates to plot your trail.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF9CA3AF),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            }

            // 3. Bottom Action Bar
            Surface(
                color = Color.White,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Start / Stop Tracking Button
                    Button(
                        onClick = {
                            if (uiState.isTracking) {
                                viewModel.stopTracking()
                            } else {
                                handleStartTracking()
                            }
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (uiState.isTracking) Color(0xFFEF4444) else Color(0xFF059669)
                        ),
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) {
                        Icon(
                            imageVector = if (uiState.isTracking) Icons.Default.Stop else Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (uiState.isTracking) "Stop GPS" else "Start GPS Walk",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }

                    // Save as Artwork Button (Enabled if coordinates exist)
                    if (uiState.coordinates.isNotEmpty()) {
                        OutlinedButton(
                            onClick = {
                                viewModel.saveCoordinatesAsArtwork { newId ->
                                    onNavigateToTracker(null)
                                }
                            },
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.5.dp, Color(0xFF059669)),
                            modifier = Modifier.height(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Brush,
                                contentDescription = null,
                                tint = Color(0xFF059669),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Save Art",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF059669),
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
