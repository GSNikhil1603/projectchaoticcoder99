package com.example.ui.home

import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.WalkRouteEntity
import com.example.ui.components.ArtCanvasView
import com.example.ui.components.ArtworkThumbnailCard
import com.example.ui.components.CreationCardItem
import com.example.ui.components.StoryCardView
import com.example.ui.theme.*
import com.example.util.ArtworkShareHelper
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToStudio: (Long) -> Unit,
    onNavigateToTracker: () -> Unit,
    onNavigateToProfile: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var showSyncDialog by remember { mutableStateOf(false) }
    var showMenuDialog by remember { mutableStateOf(false) }
    var sharingRoute by remember { mutableStateOf<WalkRouteEntity?>(null) }
    var isSharingProgress by remember { mutableStateOf(false) }
    var isViewAllGrid by remember { mutableStateOf(false) }

    val displayedRoutes = when (uiState.selectedFilter) {
        HomeFilter.ALL -> uiState.routes
        HomeFilter.FAVORITES -> uiState.favoriteRoutes
    }

    val clipboardManager = LocalClipboardManager.current
    var showLogsExpanded by remember { mutableStateOf(false) }

    // Sync Diagnostics Dialog
    if (showSyncDialog) {
        AlertDialog(
            onDismissRequest = { showSyncDialog = false },
            shape = RoundedCornerShape(16.dp),
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudSync,
                        contentDescription = "Sync Status",
                        tint = ElectricPurple
                    )
                    Text(
                        text = "Sync Diagnostics & Status",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = NearBlackInk
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Local SQLite Room Health
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MutedSurface,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(text = "💾", fontSize = 16.sp)
                                Text(
                                    text = "Local Storage (Room DB): Active & Safe",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = NearBlackInk
                                )
                            }
                            Text(
                                text = "All ${uiState.routes.size} recorded walk routes, custom pigments, explorer XP, and unlocked badges are stored on your device.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }

                    // Cloud Sync State
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = when (val state = uiState.syncState) {
                            is com.example.data.sync.SyncState.Success -> if (state.isCloudSynced) AccentMintLight else ElectricPurpleLight
                            is com.example.data.sync.SyncState.Error -> Color(0xFFFFEBEE)
                            else -> MutedSurface
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(text = "☁️", fontSize = 16.sp)
                                Text(
                                    text = "Firebase Cloud Sync Status",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = NearBlackInk
                                )
                            }
                            Text(
                                text = when (val state = uiState.syncState) {
                                    is com.example.data.sync.SyncState.Syncing -> "Step: ${state.currentStep}"
                                    is com.example.data.sync.SyncState.Success -> state.message
                                    is com.example.data.sync.SyncState.Error -> "${state.errorMessage}\n\nCause: ${state.rootCauseCategory}\nRecommendation: ${state.recommendation}"
                                    else -> "Tap 'Sync Now' to test connection and synchronize with Firestore Cloud."
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }

                    // Diagnostic Logs Terminal
                    val logs = when (val state = uiState.syncState) {
                        is com.example.data.sync.SyncState.Syncing -> state.diagnosticLogs
                        is com.example.data.sync.SyncState.Success -> state.diagnosticLogs
                        is com.example.data.sync.SyncState.Error -> state.diagnosticLogs
                        else -> emptyList()
                    }

                    if (logs.isNotEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF1E293B),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(text = "📟", fontSize = 14.sp)
                                        Text(
                                            text = "Diagnostics Log (${logs.size})",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        )
                                    }

                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        TextButton(
                                            onClick = {
                                                val logText = logs.joinToString("\n") { it.formatted() }
                                                clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(logText))
                                            },
                                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text("Copy", color = ElectricPurpleLight, fontSize = 11.sp)
                                        }

                                        TextButton(
                                            onClick = { showLogsExpanded = !showLogsExpanded },
                                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                if (showLogsExpanded) "Collapse" else "Expand",
                                                color = Color.LightGray,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                }

                                val displayedLogs = if (showLogsExpanded) logs else logs.takeLast(4)
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 6.dp),
                                    verticalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    displayedLogs.forEach { entry ->
                                        val entryColor = when (entry.level) {
                                            "ERROR" -> Color(0xFFFF8A80)
                                            "WARN" -> Color(0xFFFFD180)
                                            "SUCCESS" -> Color(0xFFB9F6CA)
                                            "DEBUG" -> Color(0xFF90CAF9)
                                            else -> Color(0xFFE2E8F0)
                                        }
                                        Text(
                                            text = entry.formatted(),
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 10.sp,
                                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                                color = entryColor
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                val isSyncing = uiState.syncState is com.example.data.sync.SyncState.Syncing
                val isError = uiState.syncState is com.example.data.sync.SyncState.Error
                Button(
                    onClick = {
                        viewModel.syncFirestore()
                    },
                    enabled = !isSyncing,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isError) Color(0xFFDC2626) else ElectricPurple
                    ),
                    modifier = Modifier.testTag("dialog_retry_sync_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (isSyncing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(14.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                            Text(text = "Syncing...", color = Color.White, fontWeight = FontWeight.SemiBold)
                        } else {
                            Icon(
                                imageVector = if (isError) Icons.Default.Refresh else Icons.Default.CloudSync,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = if (isError) "Retry Cloud Sync" else "Run Sync",
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showSyncDialog = false }) {
                    Text(text = "Close", color = TextSecondary, fontWeight = FontWeight.Medium)
                }
            }
        )
    }

    // Quick Menu Dialog (triggered by ☰ button)
    if (showMenuDialog) {
        AlertDialog(
            onDismissRequest = { showMenuDialog = false },
            shape = RoundedCornerShape(16.dp),
            title = {
                Text(
                    "Campus Navigation & Tools",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = NearBlackInk
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MutedSurface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showMenuDialog = false
                                onNavigateToTracker()
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Default.DirectionsWalk, contentDescription = null, tint = ElectricPurple)
                            Text("Start GPS Walk Tracker", style = MaterialTheme.typography.labelLarge, color = NearBlackInk)
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MutedSurface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showMenuDialog = false
                                showSyncDialog = true
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Default.CloudSync, contentDescription = null, tint = ElectricPurple)
                            Text("Sync Diagnostics & Cloud", style = MaterialTheme.typography.labelLarge, color = NearBlackInk)
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MutedSurface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showMenuDialog = false
                                onNavigateToProfile()
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = TextSecondary)
                            Text("Profile & Achievements", style = MaterialTheme.typography.labelLarge, color = NearBlackInk)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showMenuDialog = false }) {
                    Text("Close", color = TextSecondary, fontWeight = FontWeight.Medium)
                }
            }
        )
    }

    Scaffold(
        containerColor = SoftWhiteBackground,
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // 1. Top App Bar: Circular Back Button (Left) & Circular Menu Button (Right)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Circular White Back Button `<`
                Surface(
                    shape = CircleShape,
                    color = PureWhiteSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, SubtleBorder),
                    shadowElevation = 0.5.dp,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .clickable { onNavigateToTracker() }
                        .testTag("top_back_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = NearBlackInk,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Circular White Hamburger Menu Button `☰`
                Surface(
                    shape = CircleShape,
                    color = PureWhiteSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, SubtleBorder),
                    shadowElevation = 0.5.dp,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .clickable { showMenuDialog = true }
                        .testTag("top_menu_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Menu",
                            tint = NearBlackInk,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // 2. Header Title: "Your artworks ✦" (Plus Jakarta Sans ExtraBold 800)
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Your artworks",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 32.sp,
                        lineHeight = 38.sp,
                        letterSpacing = (-0.02).em
                    ),
                    color = NearBlackInk
                )
                // Electric Purple 4-point sparkle star ✦
                Text(
                    text = "✦",
                    fontSize = 20.sp,
                    color = ElectricPurple,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            // Subtitle: "Every walk. Every art." with gentle purple ribbon squiggle ~
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(top = 4.dp, bottom = 18.dp)
            ) {
                Text(
                    text = "Every walk. Every art.",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 15.sp,
                        lineHeight = 24.sp,
                        color = TextMuted
                    )
                )

                // Stylized ribbon squiggle curve ~
                Canvas(modifier = Modifier.size(width = 44.dp, height = 12.dp)) {
                    val path = Path().apply {
                        moveTo(2f, size.height * 0.7f)
                        cubicTo(
                            size.width * 0.35f, size.height * 0.1f,
                            size.width * 0.65f, size.height * 1.1f,
                            size.width - 2f, size.height * 0.3f
                        )
                    }
                    drawPath(
                        path = path,
                        color = ElectricPurple.copy(alpha = 0.7f),
                        style = Stroke(
                            width = 2.5f,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                }
            }

            // 3. Segmented Filter Pills (All / Favorites)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 18.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // All Pill
                val isAllSelected = uiState.selectedFilter == HomeFilter.ALL
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (isAllSelected) ElectricPurpleLight else PureWhiteSurface,
                    border = if (isAllSelected) androidx.compose.foundation.BorderStroke(1.dp, ElectricPurple.copy(alpha = 0.3f)) else androidx.compose.foundation.BorderStroke(1.dp, SubtleBorder),
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { viewModel.setFilter(HomeFilter.ALL) }
                        .testTag("filter_all_button")
                ) {
                    Box(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "All",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = if (isAllSelected) FontWeight.SemiBold else FontWeight.Medium,
                                fontSize = 14.sp
                            ),
                            color = if (isAllSelected) ElectricPurple else NearBlackInk
                        )
                    }
                }

                // Favorites Pill
                val isFavSelected = uiState.selectedFilter == HomeFilter.FAVORITES
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (isFavSelected) ElectricPurpleLight else PureWhiteSurface,
                    border = if (isFavSelected) androidx.compose.foundation.BorderStroke(1.dp, ElectricPurple.copy(alpha = 0.3f)) else androidx.compose.foundation.BorderStroke(1.dp, SubtleBorder),
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { viewModel.setFilter(HomeFilter.FAVORITES) }
                        .testTag("filter_favorites_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = if (isFavSelected) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = null,
                            tint = if (isFavSelected) ElectricPurple else TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Favorites",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = if (isFavSelected) FontWeight.SemiBold else FontWeight.Medium,
                                fontSize = 14.sp
                            ),
                            color = if (isFavSelected) ElectricPurple else NearBlackInk
                        )
                        if (uiState.favoriteRoutes.isNotEmpty()) {
                            Text(
                                text = "(${uiState.favoriteRoutes.size})",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isFavSelected) ElectricPurple else TextMuted
                            )
                        }
                    }
                }
            }

            // 4. Hero Card: "TODAY'S ARTWORK" - Free, light, clean UI with zero compressed text
            val todaysRoute = uiState.todaysRoute
            if (todaysRoute != null && uiState.selectedFilter == HomeFilter.ALL) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = PureWhiteSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, SubtleBorder),
                    shadowElevation = 0.5.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                        .testTag("todays_artwork_hero")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        // Top Section: Left Details & Right Square Artwork Card
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Left Details Column
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "TODAY'S ARTWORK",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 11.sp,
                                        letterSpacing = 0.6.sp
                                    ),
                                    color = ElectricPurple
                                )

                                // Big Display Date (ExtraBold 800)
                                val dateParts = todaysRoute.dateString.split(" ")
                                val dateLine1 = if (dateParts.size >= 2) "${dateParts[0]} ${dateParts[1]}" else todaysRoute.dateString
                                val dateLine2 = if (dateParts.size >= 3) dateParts[2] else ""

                                Column {
                                    Text(
                                        text = dateLine1,
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 24.sp,
                                            lineHeight = 30.sp,
                                            letterSpacing = (-0.02).em
                                        ),
                                        color = NearBlackInk
                                    )
                                    if (dateLine2.isNotEmpty()) {
                                        Text(
                                            text = dateLine2,
                                            style = MaterialTheme.typography.titleLarge.copy(
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 24.sp,
                                                lineHeight = 30.sp,
                                                letterSpacing = (-0.02).em
                                            ),
                                            color = NearBlackInk
                                        )
                                    }
                                }

                                Text(
                                    text = "Created from your ${todaysRoute.distanceKm} km • ${todaysRoute.durationMinutes} min journey",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontSize = 13.sp,
                                        lineHeight = 19.sp
                                    ),
                                    color = TextMuted
                                )
                            }

                            // Right Square Artwork Preview Card
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = MutedSurface,
                                border = androidx.compose.foundation.BorderStroke(1.dp, SubtleBorder),
                                modifier = Modifier
                                    .size(104.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.linearGradient(
                                                colors = listOf(
                                                    ElectricPurpleLight.copy(alpha = 0.6f),
                                                    AccentMintLight.copy(alpha = 0.4f)
                                                )
                                            )
                                        )
                                        .padding(6.dp)
                                ) {
                                    // Artwork Canvas View
                                    ArtCanvasView(
                                        pointsJson = todaysRoute.pointsJson,
                                        blobsJson = todaysRoute.blobsJson,
                                        artStyle = todaysRoute.artStyle,
                                        modifier = Modifier.fillMaxSize()
                                    )

                                    // Sparkles on canvas frame
                                    Text(
                                        text = "✦",
                                        color = ElectricPurple,
                                        fontSize = 13.sp,
                                        modifier = Modifier.align(Alignment.TopEnd)
                                    )
                                    Text(
                                        text = "✦",
                                        color = ElectricPurple,
                                        fontSize = 11.sp,
                                        modifier = Modifier.align(Alignment.BottomStart)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Full-Width 3-Column Metrics Row (Steps, Distance & Duration) - Clean, spacious, uncompressed!
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Steps Stat Box
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MutedSurface,
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(ElectricPurpleLight),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = "👟", fontSize = 12.sp)
                                    }
                                    Column {
                                        Text(
                                            text = "${todaysRoute.steps}",
                                            style = MaterialTheme.typography.labelLarge.copy(
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 13.sp
                                            ),
                                            color = NearBlackInk,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = "steps",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                            color = TextMuted,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }

                            // Distance Stat Box
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MutedSurface,
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(AccentMintLight),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = "📍", fontSize = 12.sp)
                                    }
                                    Column {
                                        Text(
                                            text = "${todaysRoute.distanceKm}",
                                            style = MaterialTheme.typography.labelLarge.copy(
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 13.sp
                                            ),
                                            color = NearBlackInk,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = "km",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                            color = TextMuted,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }

                            // Duration Stat Box
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MutedSurface,
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(ElectricPurpleLight),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = "⏱️", fontSize = 12.sp)
                                    }
                                    Column {
                                        Text(
                                            text = "${todaysRoute.durationMinutes}m",
                                            style = MaterialTheme.typography.labelLarge.copy(
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 13.sp
                                            ),
                                            color = NearBlackInk,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = "mins",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                            color = TextMuted,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Full-Width Electric Purple Pill Button: "View artwork ->" (SemiBold 600)
                        Button(
                            onClick = { onNavigateToStudio(todaysRoute.id) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ElectricPurple
                            ),
                            shape = RoundedCornerShape(14.dp),
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("view_artwork_button")
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "View artwork",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    color = Color.White
                                )
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 5. "Your creations" Section Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Your creations",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        letterSpacing = (-0.01).em
                    ),
                    color = NearBlackInk
                )

                Text(
                    text = if (isViewAllGrid) "Show carousel <" else "View all >",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    ),
                    color = ElectricPurple,
                    modifier = Modifier
                        .clickable { isViewAllGrid = !isViewAllGrid }
                        .testTag("view_all_creations_toggle")
                )
            }

            // 6. Creations Presentation (Horizontal Carousel matching image.png or 2-column Grid)
            if (displayedRoutes.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(text = "🎨", fontSize = 36.sp)
                        Text(
                            text = if (uiState.selectedFilter == HomeFilter.FAVORITES) "No favorite artworks yet" else "No walking art recorded yet",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = NearBlackInk
                        )
                        Text(
                            text = "Walk around campus to create your next digital masterpiece!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextMuted
                        )
                    }
                }
            } else if (!isViewAllGrid) {
                // Horizontal Carousel
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    itemsIndexed(displayedRoutes) { index, route ->
                        CreationCardItem(
                            route = route,
                            index = index,
                            onClick = { onNavigateToStudio(route.id) },
                            onFavoriteToggle = { isFav ->
                                viewModel.toggleFavorite(route.id, isFav)
                            }
                        )
                    }
                }
            } else {
                // Render 2-column grid when "View all >" is active
                val chunked = displayedRoutes.chunked(2)
                chunked.forEach { rowItems ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        rowItems.forEach { route ->
                            ArtworkThumbnailCard(
                                route = route,
                                onClick = { onNavigateToStudio(route.id) },
                                onFavoriteToggle = { isFav ->
                                    viewModel.toggleFavorite(route.id, isFav)
                                },
                                onShareClick = {
                                    sharingRoute = route
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (rowItems.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Monthly Summary Banner
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = PureWhiteSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, SubtleBorder),
                shadowElevation = 0.5.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(ElectricPurpleLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = ElectricPurple,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "February 2024",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = NearBlackInk
                            )
                            Text(
                                text = "${uiState.routes.size} walks • ${uiState.routes.size} artworks",
                                style = MaterialTheme.typography.labelMedium,
                                color = TextMuted
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(ElectricPurpleLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Place,
                            contentDescription = null,
                            tint = ElectricPurple,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // "Places you visited" Electric Purple Gradient Card
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = ElectricPurple,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToTracker() }
                    .testTag("places_visited_banner")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "This month",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                        Text(
                            text = "Places you visited",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.01).em
                            ),
                            color = Color.White
                        )
                        Text(
                            text = "7 campus zones explored",
                            style = MaterialTheme.typography.labelSmall,
                            color = ElectricPurpleLight
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationSearching,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Share Story Modal Dialog for Selected Artwork
    if (sharingRoute != null) {
        val routeToShare = sharingRoute!!
        Dialog(onDismissRequest = { sharingRoute = null }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = PureWhiteSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, SubtleBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Share Path Artwork",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = NearBlackInk
                        )
                        IconButton(onClick = { sharingRoute = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                        }
                    }

                    // 9:16 Social Story Card Preview
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .height(350.dp)
                    ) {
                        StoryCardView(
                            route = routeToShare,
                            brushStyleKey = routeToShare.artStyle,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // Primary Action: Share High-Res Story Image & Caption
                    Button(
                        onClick = {
                            if (!isSharingProgress) {
                                isSharingProgress = true
                                coroutineScope.launch {
                                    ArtworkShareHelper.shareArtwork(
                                        context = context,
                                        route = routeToShare,
                                        studentName = "Campus Artist",
                                        brushStyleKey = routeToShare.artStyle
                                    ) {
                                        isSharingProgress = false
                                        sharingRoute = null
                                    }
                                }
                            }
                        },
                        enabled = !isSharingProgress,
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricPurple),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("home_modal_share_image_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (isSharingProgress) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = Color.White
                                )
                                Text("Preparing Image...", color = Color.White, fontWeight = FontWeight.SemiBold)
                            } else {
                                Icon(Icons.Default.Share, contentDescription = null, tint = Color.White)
                                Text("Share to WhatsApp / Instagram", color = Color.White, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }

                    // Secondary Quick Share Options
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val caption = ArtworkShareHelper.createShareCaption(routeToShare, "Campus Artist")
                                val textIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, caption)
                                    putExtra(Intent.EXTRA_SUBJECT, "My Walk Artwork: ${routeToShare.shapeName}")
                                }
                                context.startActivity(Intent.createChooser(textIntent, "Share text summary"))
                                sharingRoute = null
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SubtleBorder)
                        ) {
                            Text("Share Text", color = NearBlackInk, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }

                        OutlinedButton(
                            onClick = {
                                val caption = ArtworkShareHelper.createShareCaption(routeToShare, "Campus Artist")
                                clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(caption))
                                android.widget.Toast.makeText(context, "Caption copied to clipboard!", android.widget.Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SubtleBorder)
                        ) {
                            Text("Copy Caption", color = NearBlackInk, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }
                    }

                    // Open in Coloring Studio
                    TextButton(
                        onClick = {
                            val id = routeToShare.id
                            sharingRoute = null
                            onNavigateToStudio(id)
                        }
                    ) {
                        Text("Customize in Coloring Studio ➔", color = ElectricPurple, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}
