package com.example.ui.map

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.theme.*

// Design tokens per specification
private val ColorDarkPrimary = Color(0xFF1A1A1A)
private val ColorTextSecondary = Color(0xFF6B7280)
private val ColorAccentPurple = Color(0xFF7C3AED)
private val ColorLightBackground = Color(0xFFFAFAFA)
private val ColorLightGrayInput = Color(0xFFF5F5F5)
private val ColorLakeOverlay = Color(0x663B82F6) // translucent blue
private val ColorLakeStroke = Color(0xFF2563EB)
private val ColorLightGreenPill = Color(0xFFD1FAE5)
private val ColorDarkGreenText = Color(0xFF065F46)
private val ColorLightBluePill = Color(0xFFDBEAFE)
private val ColorDarkBlueText = Color(0xFF1E40AF)
private val ColorBorder = Color(0xFFE5E7EB)

@Composable
fun CampusMapScreen(
    onNavigateToTracker: (String?) -> Unit,
    onNavigateToChallenges: () -> Unit = {},
    viewModel: CampusMapViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val filteredBuildings = remember(uiState.selectedCategory, uiState.searchQuery) {
        viewModel.getFilteredBuildings()
    }

    var zoomScale by remember { mutableFloatStateOf(1.0f) }
    var panOffsetX by remember { mutableFloatStateOf(0f) }
    var panOffsetY by remember { mutableFloatStateOf(0f) }

    // Pulsing animation for active route or user location
    val infiniteTransition = rememberInfiniteTransition(label = "markerPulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Scaffold(
        containerColor = ColorLightBackground,
        contentWindowInsets = WindowInsets.safeDrawing
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Header, Search, and Tabs (Hidden if fullscreen is enabled)
            AnimatedVisibility(
                visible = !uiState.isFullscreen,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    // 1. Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            // Top label in small uppercase gray text
                            Text(
                                text = "OFFICE OF STUDENT'S WELFARE",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    letterSpacing = 1.1.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 10.sp
                                ),
                                color = ColorTextSecondary
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Large bold title: "VIT Vellore Campus" (font-size: 28, font-weight: 800, color: #1A1A1A)
                                Text(
                                    text = "VIT Vellore Campus",
                                    style = MaterialTheme.typography.displayMedium.copy(
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = (-0.02).em
                                    ),
                                    color = ColorDarkPrimary
                                )
                                // Small pill badge: "372 ACRES" in light green background (#D1FAE5), dark green text (#065F46)
                                Surface(
                                    shape = RoundedCornerShape(24.dp),
                                    color = ColorLightGreenPill
                                ) {
                                    Text(
                                        text = "372 ACRES",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp
                                        ),
                                        color = ColorDarkGreenText,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }

                        // Top-right: Fullscreen toggle icon button
                        IconButton(
                            onClick = { viewModel.toggleFullscreen() },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(ColorLightGrayInput)
                                .testTag("fullscreen_toggle_button")
                        ) {
                            Icon(
                                imageVector = if (uiState.isFullscreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                                contentDescription = "Toggle Fullscreen",
                                tint = ColorDarkPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 2. Search Bar
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = ColorLightGrayInput,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = ColorTextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            androidx.compose.foundation.text.BasicTextField(
                                value = uiState.searchQuery,
                                onValueChange = { viewModel.updateSearchQuery(it) },
                                singleLine = true,
                                textStyle = MaterialTheme.typography.bodyMedium.copy(
                                    color = ColorDarkPrimary,
                                    fontSize = 14.sp
                                ),
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                    imeAction = androidx.compose.ui.text.input.ImeAction.Search
                                ),
                                keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                                    onSearch = {
                                        // Dismiss keyboard gracefully
                                    }
                                ),
                                decorationBox = { innerTextField ->
                                    if (uiState.searchQuery.isEmpty()) {
                                        Text(
                                            text = "Search SJT, TT, VIT Lake, Hostels, Gates...",
                                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                                            color = ColorTextSecondary
                                        )
                                    }
                                    innerTextField()
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("campus_map_search_input")
                            )
                            if (uiState.searchQuery.isNotEmpty()) {
                                IconButton(
                                    onClick = { viewModel.updateSearchQuery("") },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Clear",
                                        tint = ColorTextSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 3. Map Type Tabs (horizontal scrollable pills)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MapLayerMode.entries.forEach { mode ->
                            val isSelected = uiState.selectedLayerMode == mode
                            Surface(
                                shape = RoundedCornerShape(24.dp),
                                color = if (isSelected) ColorDarkPrimary else Color.White,
                                border = if (isSelected) null else BorderStroke(1.dp, ColorDarkPrimary),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(24.dp))
                                    .clickable { viewModel.selectLayerMode(mode) }
                                    .testTag("map_type_tab_${mode.name}")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                                ) {
                                    val tabIcon = when (mode) {
                                        MapLayerMode.STANDARD -> Icons.Default.Map
                                        MapLayerMode.ROUTE_ART -> Icons.Default.Route
                                        MapLayerMode.SHUTTLE -> Icons.Default.DirectionsBus
                                    }
                                    Icon(
                                        imageVector = tabIcon,
                                        contentDescription = mode.label,
                                        tint = if (isSelected) Color.White else ColorDarkPrimary,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Text(
                                        text = mode.label,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 12.sp
                                        ),
                                        color = if (isSelected) Color.White else ColorDarkPrimary
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // 4. Zone Filter Chips (horizontal scrollable)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CampusCategory.entries.forEach { cat ->
                            val isSelected = uiState.selectedCategory == cat
                            Surface(
                                shape = RoundedCornerShape(24.dp),
                                color = if (isSelected) ColorLightBluePill else ColorLightGrayInput,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(24.dp))
                                    .clickable { viewModel.selectCategory(cat) }
                                    .testTag("zone_filter_${cat.name}")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    val catIcon = when (cat) {
                                        CampusCategory.ALL -> Icons.Default.Map
                                        CampusCategory.ACADEMIC -> Icons.Default.Apartment
                                        CampusCategory.HOSTELS -> Icons.Default.Bed
                                        CampusCategory.SPORTS -> Icons.Default.DirectionsRun
                                        CampusCategory.TRANSIT -> Icons.Default.DirectionsBus
                                    }
                                    Icon(
                                        imageVector = catIcon,
                                        contentDescription = cat.displayName,
                                        tint = if (isSelected) ColorDarkBlueText else ColorTextSecondary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = cat.displayName,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 11.sp
                                        ),
                                        color = if (isSelected) ColorDarkBlueText else ColorTextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 5. Map View (takes rest of the screen) + Overlays + Bottom Cards
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color(0xFFF0F4F8))
            ) {
                // Interactive Vector / Pinch-to-zoom Map Canvas
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                zoomScale = (zoomScale * zoom).coerceIn(0.7f, 4.0f)
                                panOffsetX += pan.x
                                panOffsetY += pan.y
                            }
                        }
                ) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("campus_map_canvas")
                    ) {
                        val canvasWidth = size.width
                        val canvasHeight = size.height
                        val baseScale = (canvasWidth / 1000f) * zoomScale
                        val centerX = (canvasWidth / 2f) + panOffsetX
                        val centerY = (canvasHeight / 2f) + panOffsetY

                        fun toCanvasOffset(normX: Float, normY: Float): Offset {
                            val localX = (normX - 500f) * baseScale + centerX
                            val localY = (normY - 500f) * baseScale + centerY
                            return Offset(localX, localY)
                        }

                        fun toCanvasSize(w: Float, h: Float): Size {
                            return Size(w * baseScale, h * baseScale)
                        }

                        // Light minimal map background
                        drawRect(
                            color = Color(0xFFF9FAFB),
                            size = size
                        )

                        // Draw subtle campus boundary & lawns
                        val sjtGround = toCanvasOffset(680f, 700f)
                        drawRoundRect(
                            color = Color(0xFFE2F3E7),
                            topLeft = Offset(sjtGround.x - 75f * baseScale, sjtGround.y - 60f * baseScale),
                            size = toCanvasSize(150f, 120f),
                            cornerRadius = CornerRadius(20f * baseScale)
                        )

                        val openStadium = toCanvasOffset(510f, 145f)
                        drawRoundRect(
                            color = Color(0xFFF1EDE6),
                            topLeft = Offset(openStadium.x - 60f * baseScale, openStadium.y - 45f * baseScale),
                            size = toCanvasSize(120f, 90f),
                            cornerRadius = CornerRadius(16f * baseScale)
                        )

                        // 5a. Custom blue translucent polygon overlay for "VIT Lake" with a stroke
                        val lakePath = Path().apply {
                            val pt1 = toCanvasOffset(405f, 680f)
                            val pt2 = toCanvasOffset(450f, 640f)
                            val pt3 = toCanvasOffset(520f, 630f)
                            val pt4 = toCanvasOffset(600f, 660f)
                            val pt5 = toCanvasOffset(650f, 715f)
                            val pt6 = toCanvasOffset(610f, 770f)
                            val pt7 = toCanvasOffset(535f, 785f)
                            val pt8 = toCanvasOffset(465f, 765f)
                            val pt9 = toCanvasOffset(420f, 730f)

                            moveTo(pt1.x, pt1.y)
                            quadraticTo(pt2.x, pt2.y, pt3.x, pt3.y)
                            quadraticTo(pt4.x, pt4.y, pt5.x, pt5.y)
                            quadraticTo(pt6.x, pt6.y, pt7.x, pt7.y)
                            quadraticTo(pt8.x, pt8.y, pt9.x, pt9.y)
                            close()
                        }

                        // Lake Fill (Translucent Blue #663B82F6)
                        drawPath(
                            path = lakePath,
                            color = ColorLakeOverlay
                        )
                        // Lake Stroke (#2563EB)
                        drawPath(
                            path = lakePath,
                            color = ColorLakeStroke,
                            style = Stroke(width = 3.5f * baseScale)
                        )

                        // Minimalist internal roads
                        val roadColor = Color(0xFFFFFFFF)
                        val roadBorderColor = Color(0xFFE5E7EB)

                        val mainRoad = Path().apply {
                            val r1 = toCanvasOffset(30f, 620f)
                            val r2 = toCanvasOffset(380f, 870f)
                            val r3 = toCanvasOffset(710f, 925f)
                            val r4 = toCanvasOffset(980f, 930f)
                            moveTo(r1.x, r1.y)
                            lineTo(r2.x, r2.y)
                            lineTo(r3.x, r3.y)
                            lineTo(r4.x, r4.y)
                        }
                        drawPath(path = mainRoad, color = roadBorderColor, style = Stroke(width = 16f * baseScale, cap = StrokeCap.Round))
                        drawPath(path = mainRoad, color = roadColor, style = Stroke(width = 10f * baseScale, cap = StrokeCap.Round))

                        val internalAvenues = Path().apply {
                            val p1 = toCanvasOffset(110f, 600f) // Gate 1
                            val p2 = toCanvasOffset(380f, 540f)
                            val p3 = toCanvasOffset(520f, 520f)
                            val p4 = toCanvasOffset(760f, 480f)
                            val p5 = toCanvasOffset(960f, 440f)
                            moveTo(p1.x, p1.y)
                            lineTo(p2.x, p2.y)
                            lineTo(p3.x, p3.y)
                            lineTo(p4.x, p4.y)
                            lineTo(p5.x, p5.y)

                            val h1 = toCanvasOffset(270f, 210f)
                            val h2 = toCanvasOffset(500f, 320f)
                            val h3 = toCanvasOffset(720f, 210f)
                            val h4 = toCanvasOffset(840f, 290f)
                            moveTo(h1.x, h1.y)
                            lineTo(h2.x, h2.y)
                            lineTo(h3.x, h3.y)
                            lineTo(h4.x, h4.y)
                        }
                        drawPath(path = internalAvenues, color = roadBorderColor, style = Stroke(width = 12f * baseScale, cap = StrokeCap.Round))
                        drawPath(path = internalAvenues, color = roadColor, style = Stroke(width = 8f * baseScale, cap = StrokeCap.Round))

                        // 5b. Show Purple Polyline for "Campus Walking Art Routes" (color: #7C3AED, width: 4)
                        if (uiState.selectedLayerMode == MapLayerMode.ROUTE_ART || uiState.selectedLayerMode == MapLayerMode.STANDARD) {
                            val activeRoute = uiState.selectedArtRoute
                            if (activeRoute.canvasPoints.size >= 2) {
                                val artPolyline = Path()
                                val startP = toCanvasOffset(activeRoute.canvasPoints[0].x, activeRoute.canvasPoints[0].y)
                                artPolyline.moveTo(startP.x, startP.y)
                                for (i in 1 until activeRoute.canvasPoints.size) {
                                    val nextP = toCanvasOffset(activeRoute.canvasPoints[i].x, activeRoute.canvasPoints[i].y)
                                    artPolyline.lineTo(nextP.x, nextP.y)
                                }

                                // Soft glow
                                drawPath(
                                    path = artPolyline,
                                    color = ColorAccentPurple.copy(alpha = 0.25f),
                                    style = Stroke(width = 12f * baseScale, cap = StrokeCap.Round, join = StrokeJoin.Round)
                                )
                                // Solid Polyline (color: #7C3AED, width: 4dp equivalent)
                                drawPath(
                                    path = artPolyline,
                                    color = ColorAccentPurple,
                                    style = Stroke(width = 4f * baseScale, cap = StrokeCap.Round, join = StrokeJoin.Round)
                                )

                                // Route vertices
                                activeRoute.canvasPoints.forEach { pt ->
                                    val ptPos = toCanvasOffset(pt.x, pt.y)
                                    drawCircle(color = Color.White, radius = 5f * baseScale, center = ptPos)
                                    drawCircle(color = ColorAccentPurple, radius = 3f * baseScale, center = ptPos)
                                }
                            }
                        }

                        // 5c. Custom markers for key buildings with colored circle backgrounds and white icons
                        filteredBuildings.forEach { b ->
                            val markerCenter = toCanvasOffset(b.normX, b.normY)
                            val isSelected = b.id == uiState.selectedBuilding?.id

                            if (isSelected) {
                                drawCircle(
                                    color = b.color.copy(alpha = 0.3f),
                                    radius = 24f * baseScale,
                                    center = markerCenter
                                )
                            }

                            // Marker Outer Shadow & White Rim
                            drawCircle(
                                color = Color(0x33000000),
                                radius = (if (isSelected) 17f else 14f) * baseScale,
                                center = Offset(markerCenter.x, markerCenter.y + 2f * baseScale)
                            )
                            drawCircle(
                                color = Color.White,
                                radius = (if (isSelected) 16f else 13f) * baseScale,
                                center = markerCenter
                            )

                            // Marker Colored Circle
                            drawCircle(
                                color = b.color,
                                radius = (if (isSelected) 13f else 10f) * baseScale,
                                center = markerCenter
                            )

                            // Inner White dot/icon placeholder
                            drawCircle(
                                color = Color.White,
                                radius = (if (isSelected) 5f else 4f) * baseScale,
                                center = markerCenter
                            )
                        }

                        // User Live Indicator
                        val userLoc = toCanvasOffset(430f, 650f)
                        drawCircle(
                            color = Color(0xFF10B981).copy(alpha = pulseAlpha),
                            radius = 20f * baseScale,
                            center = userLoc
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 9f * baseScale,
                            center = userLoc
                        )
                        drawCircle(
                            color = Color(0xFF10B981),
                            radius = 6f * baseScale,
                            center = userLoc
                        )
                    }
                }

                // Interactive building click targets overlay
                val localDensity = androidx.compose.ui.platform.LocalDensity.current.density
                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    val canvasWidth = maxWidth.value
                    val canvasHeight = maxHeight.value
                    val baseScale = (canvasWidth / 1000f) * zoomScale
                    val centerX = (canvasWidth / 2f) + (panOffsetX / localDensity)
                    val centerY = (canvasHeight / 2f) + (panOffsetY / localDensity)

                    filteredBuildings.forEach { b ->
                        val localX = (b.normX - 500f) * baseScale + centerX
                        val localY = (b.normY - 500f) * baseScale + centerY
                        Box(
                            modifier = Modifier
                                .offset(x = (localX - 22).dp, y = (localY - 22).dp)
                                .size(44.dp)
                                .clickable {
                                    viewModel.selectBuilding(b)
                                }
                        )
                    }
                }

                // Building Callout Dialog / Bubble (when marker tapped)
                if (uiState.selectedBuilding != null) {
                    val building = uiState.selectedBuilding!!
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color.White,
                        shadowElevation = 6.dp,
                        border = BorderStroke(1.dp, ColorBorder),
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 16.dp, start = 20.dp, end = 20.dp)
                            .fillMaxWidth(0.9f)
                            .testTag("building_callout")
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(building.color),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = building.emoji, fontSize = 18.sp)
                                }
                                Column {
                                    Text(
                                        text = building.name,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        ),
                                        color = ColorDarkPrimary
                                    )
                                    Text(
                                        text = "${building.category.displayName} • ${building.popularArtRoute}",
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                        color = ColorTextSecondary
                                    )
                                }
                            }
                            IconButton(
                                onClick = { viewModel.selectBuilding(null) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = ColorTextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                // Fullscreen Exit Floating Button if in Fullscreen Mode
                if (uiState.isFullscreen) {
                    IconButton(
                        onClick = { viewModel.toggleFullscreen() },
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(16.dp)
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .shadow(4.dp, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FullscreenExit,
                            contentDescription = "Exit Fullscreen",
                            tint = ColorDarkPrimary
                        )
                    }
                }

                // 5d. Right Side: Vertical zoom in/out button stack (white circular buttons with +/- icons, subtle shadow)
                // & Right side: green circular "locate me" button at bottom
                Column(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Zoom In Button
                    Surface(
                        shape = CircleShape,
                        color = Color.White,
                        shadowElevation = 4.dp,
                        border = BorderStroke(1.dp, ColorBorder),
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .clickable { zoomScale = (zoomScale * 1.3f).coerceAtMost(4.0f) }
                            .testTag("zoom_in_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "Zoom In", tint = ColorDarkPrimary, modifier = Modifier.size(20.dp))
                        }
                    }

                    // Zoom Out Button
                    Surface(
                        shape = CircleShape,
                        color = Color.White,
                        shadowElevation = 4.dp,
                        border = BorderStroke(1.dp, ColorBorder),
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .clickable { zoomScale = (zoomScale / 1.3f).coerceAtLeast(0.7f) }
                            .testTag("zoom_out_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(imageVector = Icons.Default.Remove, contentDescription = "Zoom Out", tint = ColorDarkPrimary, modifier = Modifier.size(20.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Green Circular "Locate Me" Button
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF10B981), // Green marker #10B981
                        shadowElevation = 4.dp,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .clickable {
                                // Smoothly center on user / lake
                                zoomScale = 1.2f
                                panOffsetX = 0f
                                panOffsetY = 0f
                            }
                            .testTag("locate_me_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.MyLocation,
                                contentDescription = "Locate Me",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                // 6. Bottom Route Cards (horizontal scrollable, peeking from bottom)
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    // Header text: "Tap to view on map"
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Campus Walking Art Routes",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            ),
                            color = ColorDarkPrimary
                        )
                        Text(
                            text = "Tap to view on map",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            color = ColorTextSecondary
                        )
                    }

                    // Horizontal Scrollable Route Cards (staggered / tilted for visual interest)
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        itemsIndexed(ART_ROUTES) { index, route ->
                            val isSelected = uiState.selectedArtRoute.id == route.id
                            // Slight tilt/stagger angle for visual interest (-1.5deg or 1.5deg)
                            val tiltAngle = if (index % 2 == 0) -1.2f else 1.2f

                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = Color.White,
                                shadowElevation = if (isSelected) 6.dp else 2.dp,
                                border = if (isSelected) BorderStroke(1.5.dp, ColorAccentPurple) else BorderStroke(1.dp, ColorBorder),
                                modifier = Modifier
                                    .width(220.dp)
                                    .rotate(tiltAngle)
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable {
                                        viewModel.selectArtRoute(route)
                                        // Smooth zoom/center to this route
                                        zoomScale = 1.3f
                                    }
                                    .testTag("route_card_${route.id}")
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        // Small emoji icon (🎨 for art routes)
                                        Text(text = route.emoji, fontSize = 16.sp)
                                        // Title: bold (font-weight: 700, size: 15)
                                        Text(
                                            text = route.title,
                                            style = MaterialTheme.typography.titleSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp
                                            ),
                                            color = ColorDarkPrimary,
                                            maxLines = 1
                                        )
                                    }

                                    // Subtitle: "2.4 km • ~28 mins" in gray
                                    Text(
                                        text = "${route.distanceKm} km • ~${route.durationMins} mins",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontSize = 12.sp
                                        ),
                                        color = ColorTextSecondary
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    // Full-width gradient button at bottom: "Walk This Route" with purple->blue gradient
                                    Button(
                                        onClick = { onNavigateToTracker(route.title) },
                                        shape = RoundedCornerShape(16.dp),
                                        contentPadding = PaddingValues(0.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color.Transparent
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(38.dp)
                                            .background(
                                                Brush.horizontalGradient(
                                                    colors = listOf(
                                                        Color(0xFF7C3AED), // Vibrant Purple #7C3AED
                                                        Color(0xFF3B82F6)  // Vibrant Blue #3B82F6
                                                    )
                                                ),
                                                shape = RoundedCornerShape(16.dp)
                                            )
                                            .testTag("walk_route_button_${route.id}")
                                    ) {
                                        Text(
                                            text = "Walk This Route",
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 13.sp
                                            ),
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
