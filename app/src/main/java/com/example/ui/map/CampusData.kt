package com.example.ui.map

import androidx.compose.ui.graphics.Color
import com.example.data.model.PointF

// Building Marker Data Model
data class CampusBuilding(
    val id: String,
    val name: String,
    val shortCode: String,
    val latitude: Double,
    val longitude: Double,
    val category: CampusCategory,
    val iconName: String,
    val color: Color,
    val emoji: String,
    val description: String,
    val popularArtRoute: String = "Campus Loop",
    val estimatedStepsFromGate1: Int = 1200,
    // Canvas normalized coordinates (0..1000) mapped to VIT Vellore (12.9692 N, 79.1559 E)
    val normX: Float,
    val normY: Float
)

// Preset Walking Art Route Model
data class CampusArtRouteData(
    val id: String,
    val title: String,
    val shapeName: String,
    val emoji: String,
    val distanceKm: Double,
    val durationMins: Int,
    val color: Color,
    val description: String,
    val polylineCoords: List<Pair<Double, Double>>,
    val canvasPoints: List<PointF>
)

/**
 * 8+ Iconic VIT Vellore Campus Locations with accurate coordinates & categories
 */
val CAMPUS_LOCATIONS = listOf(
    CampusBuilding(
        id = "tt",
        name = "Technology Tower (TT)",
        shortCode = "TT",
        latitude = 12.9710,
        longitude = 79.1530,
        category = CampusCategory.ACADEMIC,
        iconName = "building",
        color = Color(0xFFEF4444), // Red marker #EF4444
        emoji = "🗼",
        description = "Core Computer Science, IT & Electronics engineering laboratories and amphitheaters.",
        popularArtRoute = "TT to SJT Infinity Path",
        estimatedStepsFromGate1 = 450,
        normX = 240f,
        normY = 710f
    ),
    CampusBuilding(
        id = "sjt",
        name = "Silver Jubilee Tower (SJT)",
        shortCode = "SJT",
        latitude = 12.9722,
        longitude = 79.1620,
        category = CampusCategory.ACADEMIC,
        iconName = "building",
        color = Color(0xFF7C3AED), // Purple marker #7C3AED (TT/SJT Auditorium)
        emoji = "🏛️",
        description = "Iconic 8-storey multi-department academic tower with smart lecture complexes.",
        popularArtRoute = "TT to SJT Infinity Path",
        estimatedStepsFromGate1 = 2100,
        normX = 710f,
        normY = 570f
    ),
    CampusBuilding(
        id = "sjt_ground",
        name = "SJT Ground",
        shortCode = "SJT-GRD",
        latitude = 12.9715,
        longitude = 79.1610,
        category = CampusCategory.SPORTS,
        iconName = "flag",
        color = Color(0xFFF97316), // Orange marker #F97316
        emoji = "🎪",
        description = "Spacious induction venue, convocation grounds, and Rivera festival arena.",
        popularArtRoute = "Induction Festival Ring",
        estimatedStepsFromGate1 = 2200,
        normX = 680f,
        normY = 700f
    ),
    CampusBuilding(
        id = "vit_lake",
        name = "VIT Lake",
        shortCode = "LAKE",
        latitude = 12.9692,
        longitude = 79.1559,
        category = CampusCategory.SPORTS,
        iconName = "water",
        color = Color(0xFF3B82F6), // Blue marker #3B82F6
        emoji = "🌊",
        description = "Scenic campus freshwater lake with scenic perimeter walking track and gazebos.",
        popularArtRoute = "VIT Lake Dolphin Loop",
        estimatedStepsFromGate1 = 1100,
        normX = 520f,
        normY = 695f
    ),
    CampusBuilding(
        id = "mh_blocks",
        name = "MH Blocks (Men's Hostels)",
        shortCode = "MH",
        latitude = 12.9750,
        longitude = 79.1575,
        category = CampusCategory.HOSTELS,
        iconName = "bed",
        color = Color(0xFF10B981), // Green marker #10B981
        emoji = "🏢",
        description = "Central Men's Hostel residential blocks including MH-B, MH-F, and MH-Q quads.",
        popularArtRoute = "Hostel Quad Geometric Crown",
        estimatedStepsFromGate1 = 2000,
        normX = 475f,
        normY = 360f
    ),
    CampusBuilding(
        id = "prp",
        name = "Pearl Research Park (PRP)",
        shortCode = "PRP",
        latitude = 12.9730,
        longitude = 79.1645,
        category = CampusCategory.ACADEMIC,
        iconName = "science",
        color = Color(0xFFEF4444),
        emoji = "🔬",
        description = "Advanced multidisciplinary research hub, innovation labs, and faculty offices.",
        popularArtRoute = "PRP Diamond Circuit",
        estimatedStepsFromGate1 = 2800,
        normX = 860f,
        normY = 520f
    ),
    CampusBuilding(
        id = "open_stadium",
        name = "Open Stadium & Track",
        shortCode = "STAD",
        latitude = 12.9775,
        longitude = 79.1560,
        category = CampusCategory.SPORTS,
        iconName = "sports",
        color = Color(0xFFF97316),
        emoji = "🏟️",
        description = "400m Olympic standard synthetic athletic track, football ground, and viewing stands.",
        popularArtRoute = "Stadium Sprint Star Art",
        estimatedStepsFromGate1 = 2300,
        normX = 510f,
        normY = 145f
    ),
    CampusBuilding(
        id = "gate_1",
        name = "Main Entrance (Gate 1)",
        shortCode = "GATE-1",
        latitude = 12.9680,
        longitude = 79.1510,
        category = CampusCategory.TRANSIT,
        iconName = "gate",
        color = Color(0xFF6B7280),
        emoji = "🚪",
        description = "Primary university gate on Katpadi Main Road with security checkpoint and visitor lounge.",
        popularArtRoute = "Welcome Gate Straight",
        estimatedStepsFromGate1 = 0,
        normX = 110f,
        normY = 605f
    )
)

/**
 * 3+ Campus Walking Art Routes with detailed geometry
 */
val ART_ROUTES = listOf(
    CampusArtRouteData(
        id = "route_lake_dolphin",
        title = "VIT Lake Dolphin Loop",
        shapeName = "Dolphin",
        emoji = "🎨",
        distanceKm = 2.4,
        durationMins = 28,
        color = Color(0xFF7C3AED), // Campus Walking Art Route in #7C3AED
        description = "Walk along the curved perimeter path of VIT Lake to create a fluid aquatic dolphin artwork.",
        polylineCoords = listOf(
            Pair(12.9680, 79.1545),
            Pair(12.9690, 79.1540),
            Pair(12.9702, 79.1550),
            Pair(12.9708, 79.1570),
            Pair(12.9698, 79.1585),
            Pair(12.9685, 79.1580),
            Pair(12.9678, 79.1565),
            Pair(12.9680, 79.1545)
        ),
        canvasPoints = listOf(
            PointF(420f, 660f),
            PointF(470f, 640f),
            PointF(530f, 650f),
            PointF(590f, 670f),
            PointF(630f, 710f),
            PointF(600f, 760f),
            PointF(540f, 775f),
            PointF(480f, 760f),
            PointF(440f, 730f),
            PointF(410f, 680f),
            PointF(420f, 660f)
        )
    ),
    CampusArtRouteData(
        id = "route_tt_sjt_infinity",
        title = "TT to SJT Infinity Path",
        shapeName = "Infinity",
        emoji = "🎨",
        distanceKm = 3.6,
        durationMins = 42,
        color = Color(0xFF7C3AED),
        description = "Trace a figure-eight crossing between Technology Tower, Library, and Silver Jubilee Tower.",
        polylineCoords = listOf(
            Pair(12.9710, 79.1530),
            Pair(12.9715, 79.1560),
            Pair(12.9725, 79.1590),
            Pair(12.9722, 79.1620),
            Pair(12.9705, 79.1610),
            Pair(12.9700, 79.1570),
            Pair(12.9710, 79.1530)
        ),
        canvasPoints = listOf(
            PointF(240f, 710f),
            PointF(320f, 600f),
            PointF(450f, 530f),
            PointF(600f, 500f),
            PointF(710f, 570f),
            PointF(680f, 680f),
            PointF(550f, 630f),
            PointF(450f, 530f),
            PointF(300f, 650f),
            PointF(240f, 710f)
        )
    ),
    CampusArtRouteData(
        id = "route_hostel_crown",
        title = "Hostel Quad Geometric Crown",
        shapeName = "Crown",
        emoji = "🎨",
        distanceKm = 3.1,
        durationMins = 35,
        color = Color(0xFF7C3AED),
        description = "Weave through MH-B, MH-F, and MH-N quads creating sharp majestic crown peaks.",
        polylineCoords = listOf(
            Pair(12.9740, 79.1540),
            Pair(12.9760, 79.1560),
            Pair(12.9745, 79.1575),
            Pair(12.9765, 79.1595),
            Pair(12.9740, 79.1610),
            Pair(12.9730, 79.1570),
            Pair(12.9740, 79.1540)
        ),
        canvasPoints = listOf(
            PointF(315f, 200f),
            PointF(400f, 290f),
            PointF(475f, 200f),
            PointF(600f, 290f),
            PointF(740f, 195f),
            PointF(700f, 380f),
            PointF(500f, 380f),
            PointF(315f, 380f),
            PointF(315f, 200f)
        )
    ),
    CampusArtRouteData(
        id = "route_stadium_star",
        title = "Stadium Sprint Star Art",
        shapeName = "Star",
        emoji = "🎨",
        distanceKm = 2.8,
        durationMins = 32,
        color = Color(0xFF7C3AED),
        description = "Sprint across the Open Stadium and Helipad avenues to draw a five-pointed star.",
        polylineCoords = listOf(
            Pair(12.9780, 79.1560),
            Pair(12.9770, 79.1575),
            Pair(12.9760, 79.1565),
            Pair(12.9765, 79.1545),
            Pair(12.9780, 79.1560)
        ),
        canvasPoints = listOf(
            PointF(510f, 80f),
            PointF(550f, 160f),
            PointF(630f, 160f),
            PointF(570f, 210f),
            PointF(590f, 290f),
            PointF(510f, 240f),
            PointF(430f, 290f),
            PointF(450f, 210f),
            PointF(390f, 160f),
            PointF(470f, 160f),
            PointF(510f, 80f)
        )
    )
)
