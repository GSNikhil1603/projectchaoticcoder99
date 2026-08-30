package com.example.ui.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.example.data.model.WalkRouteEntity
import com.example.ui.theme.*

// Pre-defined inspirational quotes for "A little cheer for you"
private val CHEER_QUOTES = listOf(
    Pair("You turned every step into a", "small work of art."),
    Pair("Every stride is a stroke of", "pure campus creativity."),
    Pair("The campus is your canvas,", "and your feet are the brush."),
    Pair("Walking turns daily motion into", "timeless living art."),
    Pair("You mapped your morning in", "vibrant pastel shades.")
)

private val INSPIRATION_POPUPS = listOf(
    "✨ You've walked 7,842 steps today — vibrant energy flows through every path!",
    "🎨 Tip: Try taking the Lake loop tomorrow to unlock the Dolphin Art badge!",
    "🌿 Keep it up! 2,158 steps remaining to reach your 10,000 daily goal.",
    "✨ Creativity is walking where no thoughts have gone before.",
    "🔥 You're on a 7-day streak! Your campus map is glowing with color."
)

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToStudio: (Long) -> Unit,
    onNavigateToTracker: () -> Unit,
    onNavigateToProfile: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    var cheerIndex by remember { mutableIntStateOf(0) }
    var inspireDialogText by remember { mutableStateOf<String?>(null) }

    val currentCheer = CHEER_QUOTES[cheerIndex % CHEER_QUOTES.size]

    // Steps, distance, and calories (combines live hardware pedometer sensor with stored history)
    val liveSensorSteps = uiState.pedometerState.dailySteps
    val todaySteps = if (liveSensorSteps > 0) liveSensorSteps else (uiState.todaysRoute?.steps ?: 7842)
    val todayDistance = if (liveSensorSteps > 0) uiState.pedometerState.distanceKm else (uiState.todaysRoute?.distanceKm ?: 5.6)
    val todayCalories = if (liveSensorSteps > 0) uiState.pedometerState.caloriesBurned else (uiState.todaysRoute?.calories ?: 312)
    val goalSteps = uiState.pedometerState.dailyGoalSteps
    val goalFraction = (todaySteps.toFloat() / goalSteps.toFloat()).coerceIn(0f, 1f)
    val goalPercent = (goalFraction * 100).toInt()

    val userName = uiState.userProfile?.username ?: "Neeraj"

    // Inspiration Dialog
    if (inspireDialogText != null) {
        AlertDialog(
            onDismissRequest = { inspireDialogText = null },
            shape = RoundedCornerShape(20.dp),
            containerColor = Color.White,
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = "✨", fontSize = 20.sp)
                    Text(
                        text = "Daily Inspiration",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        ),
                        color = Color(0xFF111827)
                    )
                }
            },
            text = {
                Text(
                    text = inspireDialogText!!,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.sp,
                        lineHeight = 22.sp
                    ),
                    color = Color(0xFF374151)
                )
            },
            confirmButton = {
                Button(
                    onClick = { inspireDialogText = null },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                ) {
                    Text("Got it", fontWeight = FontWeight.SemiBold)
                }
            }
        )
    }

    Scaffold(
        containerColor = Color(0xFFFAF9F6), // Warm clean off-white background
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 1. Top Bar: "WalkArt" with organic green underline + Profile Avatar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // WalkArt Logo with green curved underline
                Column {
                    Text(
                        text = "WalkArt",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 24.sp,
                            letterSpacing = (-0.03).em
                        ),
                        color = Color(0xFF111827)
                    )
                    // Organic green brush stroke curve under WalkArt
                    Canvas(
                        modifier = Modifier
                            .width(84.dp)
                            .height(6.dp)
                            .padding(top = 1.dp)
                    ) {
                        val path = Path().apply {
                            moveTo(2f, 2f)
                            quadraticTo(size.width * 0.45f, size.height + 2f, size.width - 2f, 2f)
                        }
                        drawPath(
                            path = path,
                            color = Color(0xFF52B788), // Natural green brush stroke
                            style = Stroke(width = 3.5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                        )
                    }
                }

                // Profile Avatar Button (circular border with outline icon)
                Surface(
                    shape = CircleShape,
                    color = Color.White,
                    border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                    shadowElevation = 1.dp,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .clickable { onNavigateToProfile() }
                        .testTag("home_profile_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.Person,
                            contentDescription = "Profile",
                            tint = Color(0xFF374151),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            // 2. Greeting Section: "Hello Neeraj" + "Live. Move. Create."
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Text(
                    text = "Hello $userName",
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.02).em
                    ),
                    color = Color(0xFF111827)
                )
                Text(
                    text = "Live. Move. Create.",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = Color(0xFF6B7280)
                )
            }

            // 3. TODAY'S WALK Hero Card (soft mint->lavender pastel gradient, stats, doodle canvas, progress bar)
            Surface(
                shape = RoundedCornerShape(28.dp),
                shadowElevation = 2.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .clickable {
                        val route = uiState.todaysRoute
                        if (route != null) onNavigateToStudio(route.id) else onNavigateToTracker()
                    }
                    .testTag("hero_today_walk_card")
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFFD6F5EC), // Mint Cyan Top-Left
                                    Color(0xFFE0EAFC), // Soft Periwinkle Middle
                                    Color(0xFFECE4FB)  // Pastel Lavender Bottom-Right
                                ),
                                start = Offset(0f, 0f),
                                end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                            )
                        )
                        .padding(22.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Top Section: Left (Text & Metrics) + Right (Abstract Continuous Line Doodle Canvas)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Left Content
                            Column(
                                modifier = Modifier.weight(1.1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                // "TODAY'S WALK" Tag
                                Text(
                                    text = "TODAY'S WALK",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 12.sp,
                                        letterSpacing = 1.sp
                                    ),
                                    color = Color(0xFF0D9488)
                                )

                                // Step count: "7,842"
                                Text(
                                    text = String.format("%,d", todaySteps),
                                    style = MaterialTheme.typography.displayMedium.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 36.sp,
                                        letterSpacing = (-0.03).em
                                    ),
                                    color = Color(0xFF111827)
                                )

                                // "steps made into art"
                                Text(
                                    text = "steps made into art",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Normal
                                    ),
                                    color = Color(0xFF4B5563)
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // Metric Pills: 5.6 km & 312 kcal
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Distance Pill
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Surface(
                                            shape = CircleShape,
                                            color = Color.White.copy(alpha = 0.85f),
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = Icons.Default.DirectionsWalk,
                                                    contentDescription = null,
                                                    tint = Color(0xFF0D9488),
                                                    modifier = Modifier.size(13.dp)
                                                )
                                            }
                                        }
                                        Column {
                                            Text(
                                                text = "$todayDistance",
                                                style = MaterialTheme.typography.labelMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp
                                                ),
                                                color = Color(0xFF111827)
                                            )
                                            Text(
                                                text = "km",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontSize = 10.sp
                                                ),
                                                color = Color(0xFF6B7280)
                                            )
                                        }
                                    }

                                    // Calories Pill
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Surface(
                                            shape = CircleShape,
                                            color = Color.White.copy(alpha = 0.85f),
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = Icons.Default.LocalFireDepartment,
                                                    contentDescription = null,
                                                    tint = Color(0xFFF97316),
                                                    modifier = Modifier.size(13.dp)
                                                )
                                            }
                                        }
                                        Column {
                                            Text(
                                                text = "$todayCalories",
                                                style = MaterialTheme.typography.labelMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp
                                                ),
                                                color = Color(0xFF111827)
                                            )
                                            Text(
                                                text = "kcal",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontSize = 10.sp
                                                ),
                                                color = Color(0xFF6B7280)
                                            )
                                        }
                                    }
                                }
                            }

                            // Right Visual: Abstract Doodle Art with Soft Pastel Blobs & Continuous Loop Line
                            Box(
                                modifier = Modifier
                                    .size(width = 130.dp, height = 115.dp)
                                    .weight(0.9f)
                            ) {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val w = size.width
                                    val h = size.height

                                    // 1. Pastel Watercolor Blobs
                                    // Soft Pink/Rose Blob
                                    drawCircle(
                                        color = Color(0xFFFBCFE8).copy(alpha = 0.75f),
                                        radius = w * 0.26f,
                                        center = Offset(w * 0.65f, h * 0.38f)
                                    )
                                    // Mint/Cyan Blob
                                    drawCircle(
                                        color = Color(0xFFA7F3D0).copy(alpha = 0.85f),
                                        radius = w * 0.22f,
                                        center = Offset(w * 0.35f, h * 0.72f)
                                    )
                                    // Pastel Yellow Blob
                                    drawCircle(
                                        color = Color(0xFFFEF08A).copy(alpha = 0.85f),
                                        radius = w * 0.18f,
                                        center = Offset(w * 0.82f, h * 0.68f)
                                    )

                                    // 2. Organic Continuous Single-Line Doodle Art (Mimicking Walk Route)
                                    val doodlePath = Path().apply {
                                        moveTo(w * 0.25f, h * 0.20f)
                                        // Loop 1
                                        cubicTo(
                                            w * 0.10f, h * 0.10f,
                                            w * 0.40f, h * 0.05f,
                                            w * 0.35f, h * 0.30f
                                        )
                                        // Loop 2 down & around
                                        cubicTo(
                                            w * 0.30f, h * 0.60f,
                                            w * 0.10f, h * 0.65f,
                                            w * 0.20f, h * 0.85f
                                        )
                                        // Loop 3 center
                                        cubicTo(
                                            w * 0.30f, h * 1.05f,
                                            w * 0.55f, h * 0.50f,
                                            w * 0.50f, h * 0.30f
                                        )
                                        // Loop 4 upper right
                                        cubicTo(
                                            w * 0.45f, h * 0.10f,
                                            w * 0.75f, h * 0.15f,
                                            w * 0.65f, h * 0.45f
                                        )
                                        // Loop 5 lower right
                                        cubicTo(
                                            w * 0.55f, h * 0.75f,
                                            w * 0.85f, h * 0.90f,
                                            w * 0.80f, h * 0.50f
                                        )
                                    }

                                    // Doodle stroke
                                    drawPath(
                                        path = doodlePath,
                                        color = Color(0xFF334155), // Slate dark loop line
                                        style = Stroke(
                                            width = 2.2f,
                                            cap = StrokeCap.Round,
                                            join = StrokeJoin.Round
                                        )
                                    )
                                }
                            }
                        }

                        // Bottom Progress Section inside Hero Card
                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "$goalPercent% of daily goal",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp
                                    ),
                                    color = Color(0xFF374151)
                                )
                                Text(
                                    text = String.format("%,d", goalSteps),
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp
                                    ),
                                    color = Color(0xFF4B5563)
                                )
                            }

                            // Progress Bar with vibrant emerald green fill
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color.White.copy(alpha = 0.55f))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(goalFraction)
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFF059669)) // Emerald green
                                )
                            }
                        }
                    }
                }
            }

            // 4. "A little cheer for you" Section
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Section Header Row: Title (Left) + "New line ↺" (Right)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "A little cheer for you",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        ),
                        color = Color(0xFF1F2937)
                    )

                    Text(
                        text = "New line ↺",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        ),
                        color = Color(0xFF7C3AED), // Soft purple/indigo
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                cheerIndex = (cheerIndex + 1) % CHEER_QUOTES.size
                            }
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                            .testTag("new_cheer_line_button")
                    )
                }

                // Cheer Card (Warm Butter Cream / Soft Peach)
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = Color(0xFFFEF7EE), // Warm soft cream
                    border = BorderStroke(1.dp, Color(0xFFFDE68A).copy(alpha = 0.4f)),
                    shadowElevation = 0.5.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Cheer Heading
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = currentCheer.first,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 18.sp,
                                    letterSpacing = (-0.01).em
                                ),
                                color = Color(0xFF1F2937)
                            )
                            Text(
                                text = currentCheer.second,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 19.sp,
                                    letterSpacing = (-0.01).em
                                ),
                                color = Color(0xFFEA580C) // Warm terracotta / orange
                            )
                        }

                        // Bottom Row in Cheer Card
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${String.format("%,d", todaySteps)} steps and still moving beautifully.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                color = Color(0xFF78716C),
                                modifier = Modifier.weight(1f)
                            )

                            // "+ Inspire me" pill button
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = Color.White,
                                border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                                shadowElevation = 1.dp,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .clickable {
                                        val randomPopup = INSPIRATION_POPUPS.random()
                                        inspireDialogText = randomPopup
                                    }
                                    .testTag("inspire_me_button")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "+",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = Color(0xFF1F2937)
                                    )
                                    Text(
                                        text = "Inspire me",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        ),
                                        color = Color(0xFF1F2937)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 5. Live Campus Pedometer & Hardware Step Counter Sensor Card
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                shadowElevation = 1.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("campus_pedometer_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Header Row: Pedometer Title & Live Sensor Badge
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFFECFDF5),
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.DirectionsWalk,
                                        contentDescription = null,
                                        tint = Color(0xFF059669),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = "Campus Pedometer",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    ),
                                    color = Color(0xFF111827)
                                )
                                Text(
                                    text = uiState.pedometerState.sensorType,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Normal
                                    ),
                                    color = Color(0xFF6B7280)
                                )
                            }
                        }

                        // Live Pulse Status Badge
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (uiState.pedometerState.isTracking) Color(0xFFECFDF5) else Color(0xFFF3F4F6)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(if (uiState.pedometerState.isTracking) Color(0xFF10B981) else Color(0xFF9CA3AF))
                                )
                                Text(
                                    text = if (uiState.pedometerState.isTracking) "Live Active" else "Ready",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    ),
                                    color = if (uiState.pedometerState.isTracking) Color(0xFF065F46) else Color(0xFF6B7280)
                                )
                            }
                        }
                    }

                    // 3 Metric Blocks: Live Daily Steps, Cadence (SPM), Active Time
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Metric 1: Steps
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFFF9FAFB),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = "Daily Steps",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                    color = Color(0xFF6B7280)
                                )
                                Text(
                                    text = String.format("%,d", todaySteps),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 16.sp
                                    ),
                                    color = Color(0xFF111827)
                                )
                            }
                        }

                        // Metric 2: Cadence
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFFF9FAFB),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = "Cadence",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                    color = Color(0xFF6B7280)
                                )
                                Text(
                                    text = "${uiState.pedometerState.cadenceSpm} SPM",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 16.sp
                                    ),
                                    color = Color(0xFF0D9488)
                                )
                            }
                        }

                        // Metric 3: Active Walk
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFFF9FAFB),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = "Active Time",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                    color = Color(0xFF6B7280)
                                )
                                Text(
                                    text = "${uiState.pedometerState.activeMinutes} min",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 16.sp
                                    ),
                                    color = Color(0xFF7C3AED)
                                )
                            }
                        }
                    }

                    // Interactive Action Strip: Add +250 Steps (Test/Calibrate) + Start Walk Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Quick Add +250 steps button
                        OutlinedButton(
                            onClick = { viewModel.simulateWalkSteps(250) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("+250 Steps", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }

                        // Reset Day Steps Button
                        OutlinedButton(
                            onClick = { viewModel.resetDailySteps() },
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Reset Daily Steps",
                                modifier = Modifier.size(14.dp)
                            )
                        }

                        // Start GPS & Art Tracker Button
                        Button(
                            onClick = { onNavigateToTracker() },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                            modifier = Modifier.weight(1.1f),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Text("Track Walk", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // 6. Subtle Footer Prompt Note
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = "Your next walk is waiting for its colour.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal
                    ),
                    color = Color(0xFF9CA3AF)
                )
            }
        }
    }
}
