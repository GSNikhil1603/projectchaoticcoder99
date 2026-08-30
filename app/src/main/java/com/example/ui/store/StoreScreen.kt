package com.example.ui.store

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CustomColorEntity
import com.example.data.model.StoreItemEntity

private val StoreBg = Color(0xFFF9F7FC)
private val TextDark = Color(0xFF1F1A40)
private val TextMutedPurple = Color(0xFF7A7593)
private val PurpleHeader = Color(0xFF7047EB)
private val PurpleButton = Color(0xFF8B5CF6)
private val CardBorder = Color(0xFFF0ECF8)

@Composable
fun StoreScreen(
    viewModel: StoreViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    val categoryScrollState = rememberScrollState()

    var selectedTab by remember { mutableStateOf("ALL") }
    var showColorMixer by remember { mutableStateOf(false) }

    var redSlider by remember { mutableFloatStateOf(0.5f) }
    var greenSlider by remember { mutableFloatStateOf(0.84f) }
    var blueSlider by remember { mutableFloatStateOf(0.78f) }

    Scaffold(
        containerColor = StoreBg,
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Top Header with Title and Coin Counter Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Art Store\n& Color Lab",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark,
                        lineHeight = 34.sp,
                        modifier = Modifier.testTag("store_title")
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Unlock palettes, mix custom pigments & level up.",
                        fontSize = 13.sp,
                        color = TextMutedPurple,
                        lineHeight = 18.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Yellow Coin Pill
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color(0xFFFEF08A),
                    border = BorderStroke(1.dp, Color(0xFFFDE047)),
                    shadowElevation = 2.dp,
                    modifier = Modifier.testTag("coin_badge")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF59E0B)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "🪙", fontSize = 12.sp)
                        }
                        Text(
                            text = "${uiState.userProfile?.totalCoins ?: 125}",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            color = Color(0xFF451A03)
                        )
                    }
                }
            }

            // 2. Level / XP Card
            val userProfile = uiState.userProfile
            val currentLvl = userProfile?.currentLevel ?: 2
            val currentXp = userProfile?.currentXp ?: 830
            val rankTitle = userProfile?.explorerRank ?: "Path Pioneer"
            val nextLvlXp = when (currentLvl) {
                1 -> 300
                2 -> 830
                3 -> 1500
                4 -> 2500
                else -> 4000
            }
            val xpProgress = (currentXp.toFloat() / nextLvlXp.toFloat()).coerceIn(0f, 1f)

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                border = BorderStroke(1.dp, CardBorder),
                shadowElevation = 1.dp,
                modifier = Modifier.fillMaxWidth().testTag("level_card")
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
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
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFFCD34D)
                            ) {
                                Text(
                                    text = "Lvl $currentLvl",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = Color(0xFF451A03),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                            Text(
                                text = rankTitle,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = TextDark
                            )
                        }

                        Text(
                            text = "$currentXp/$nextLvlXp XP",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF9E9AA7)
                        )
                    }

                    // Purple Progress Bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFFEDE9FE))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(xpProgress)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFF8B5CF6), Color(0xFFA78BFA))
                                    )
                                )
                        )
                    }
                }
            }

            // Purchase / Success Notification Banner
            if (uiState.purchaseSuccessMessage != null) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFD1FAE5),
                    border = BorderStroke(1.dp, Color(0xFFA7F3D0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF059669),
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = uiState.purchaseSuccessMessage ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF065F46)
                        )
                    }
                }
            }

            // 3. Category Filter Tabs Row
            val tabs = listOf(
                "ALL" to "All items",
                "OUTLINE" to "Brushes",
                "PALETTE" to "Palettes",
                "CUSTOM" to "My pigments"
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(categoryScrollState),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    tabs.forEach { (key, label) ->
                        val isSelected = selectedTab == key
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSelected) Color(0xFF1F1A40) else Color(0xFFF0EDF7),
                            modifier = Modifier
                                .clickable {
                                    selectedTab = key
                                    viewModel.selectCategory(key)
                                }
                                .testTag("tab_$key")
                        ) {
                            Text(
                                text = label,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else Color(0xFF5E5873),
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp)
                            )
                        }
                    }
                }

                // Scroll Indicator Line with Left & Right Arrows
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "◀",
                        fontSize = 9.sp,
                        color = Color(0xFF9E9AA7),
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    Box(
                        modifier = Modifier
                            .width(180.dp)
                            .height(5.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color(0xFF88849E))
                    )
                    Text(
                        text = "▶",
                        fontSize = 9.sp,
                        color = Color(0xFF9E9AA7),
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }

            // 4. BRUSHES SECTION
            if (selectedTab == "ALL" || selectedTab == "OUTLINE") {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "BRUSHES",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = PurpleHeader,
                        letterSpacing = 0.5.sp,
                        modifier = Modifier.padding(start = 2.dp)
                    )

                    // Fine ink pen
                    val fineInk = uiState.storeItems.find { it.id == "br_fine_ink" }
                        ?: StoreItemEntity(
                            id = "br_fine_ink",
                            title = "Fine ink pen",
                            description = "Crisp architectural line, smooth tapering.",
                            category = "OUTLINE",
                            costCoins = 0,
                            isUnlocked = true,
                            previewHex = "#1E293B",
                            styleKey = "INK"
                        )
                    BrushItemCard(
                        title = fineInk.title,
                        description = fineInk.description,
                        isUnlocked = fineInk.isUnlocked,
                        costCoins = fineInk.costCoins,
                        userCoins = uiState.userProfile?.totalCoins ?: 0,
                        brushType = BrushStrokeType.FINE_INK,
                        onBuy = { viewModel.buyItem(fineInk) }
                    )

                    // Cyber neon pulse
                    val cyberNeon = uiState.storeItems.find { it.id == "br_cyber_neon" }
                        ?: StoreItemEntity(
                            id = "br_cyber_neon",
                            title = "Cyber neon pulse",
                            description = "Glowing cyan-violet stroke for night routes.",
                            category = "OUTLINE",
                            costCoins = 0,
                            isUnlocked = true,
                            previewHex = "#22D3EE",
                            styleKey = "NEON"
                        )
                    BrushItemCard(
                        title = cyberNeon.title,
                        description = cyberNeon.description,
                        isUnlocked = cyberNeon.isUnlocked,
                        costCoins = cyberNeon.costCoins,
                        userCoins = uiState.userProfile?.totalCoins ?: 0,
                        brushType = BrushStrokeType.CYBER_NEON,
                        onBuy = { viewModel.buyItem(cyberNeon) }
                    )

                    // Aquarelle flow
                    val aquarelle = uiState.storeItems.find { it.id == "br_aquarelle" }
                        ?: StoreItemEntity(
                            id = "br_aquarelle",
                            title = "Aquarelle flow",
                            description = "Soft watercolor stroke that bleeds gently.",
                            category = "OUTLINE",
                            costCoins = 300,
                            isUnlocked = false,
                            previewHex = "#A78BFA",
                            styleKey = "WATERCOLOR"
                        )
                    BrushItemCard(
                        title = aquarelle.title,
                        description = aquarelle.description,
                        isUnlocked = aquarelle.isUnlocked,
                        costCoins = aquarelle.costCoins,
                        userCoins = uiState.userProfile?.totalCoins ?: 0,
                        brushType = BrushStrokeType.AQUARELLE,
                        onBuy = { viewModel.buyItem(aquarelle) }
                    )

                    // Campus chalk
                    val campusChalk = uiState.storeItems.find { it.id == "br_chalk" }
                        ?: StoreItemEntity(
                            id = "br_chalk",
                            title = "Campus chalk",
                            description = "Textured chalk outline, graffiti style.",
                            category = "OUTLINE",
                            costCoins = 200,
                            isUnlocked = false,
                            previewHex = "#F59E0B",
                            styleKey = "CHALK"
                        )
                    BrushItemCard(
                        title = campusChalk.title,
                        description = campusChalk.description,
                        isUnlocked = campusChalk.isUnlocked,
                        costCoins = campusChalk.costCoins,
                        userCoins = uiState.userProfile?.totalCoins ?: 0,
                        brushType = BrushStrokeType.CHALK,
                        onBuy = { viewModel.buyItem(campusChalk) }
                    )
                }
            }

            // 5. PALETTES SECTION
            if (selectedTab == "ALL" || selectedTab == "PALETTE") {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "PALETTES",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = PurpleHeader,
                        letterSpacing = 0.5.sp,
                        modifier = Modifier.padding(start = 2.dp)
                    )

                    // Pastel bloom
                    val pastelBloom = uiState.storeItems.find { it.id == "pal_pastel_bloom" }
                        ?: StoreItemEntity(
                            id = "pal_pastel_bloom",
                            title = "Pastel bloom",
                            description = "Mints, lavenders, honey peach.",
                            category = "PALETTE",
                            costCoins = 0,
                            isUnlocked = true,
                            previewHex = "#D1FAE5",
                            styleKey = "PALETTE_PASTEL"
                        )
                    PaletteItemCard(
                        title = pastelBloom.title,
                        description = pastelBloom.description,
                        colors = listOf(Color(0xFFD1FAE5), Color(0xFFE9D5FF), Color(0xFFFED7AA), Color(0xFFFCE7F3)),
                        isUnlocked = pastelBloom.isUnlocked,
                        costCoins = pastelBloom.costCoins,
                        userCoins = uiState.userProfile?.totalCoins ?: 0,
                        onBuy = { viewModel.buyItem(pastelBloom) }
                    )

                    // Sunset bloom
                    val sunsetBloom = uiState.storeItems.find { it.id == "pal_sunset_bloom" }
                        ?: StoreItemEntity(
                            id = "pal_sunset_bloom",
                            title = "Sunset bloom",
                            description = "Warm oranges into rosy pink.",
                            category = "PALETTE",
                            costCoins = 320,
                            isUnlocked = false,
                            previewHex = "#FB923C",
                            styleKey = "PALETTE_SUNSET"
                        )
                    PaletteItemCard(
                        title = sunsetBloom.title,
                        description = sunsetBloom.description,
                        colors = listOf(Color(0xFFFBBF24), Color(0xFFFB923C), Color(0xFFF43F5E), Color(0xFFBE123C)),
                        isUnlocked = sunsetBloom.isUnlocked,
                        costCoins = sunsetBloom.costCoins,
                        userCoins = uiState.userProfile?.totalCoins ?: 0,
                        onBuy = { viewModel.buyItem(sunsetBloom) }
                    )

                    // Ocean drift
                    val oceanDrift = uiState.storeItems.find { it.id == "pal_ocean_drift" }
                        ?: StoreItemEntity(
                            id = "pal_ocean_drift",
                            title = "Ocean drift",
                            description = "Teal to deep navy blue.",
                            category = "PALETTE",
                            costCoins = 280,
                            isUnlocked = false,
                            previewHex = "#38BDF8",
                            styleKey = "PALETTE_OCEAN"
                        )
                    PaletteItemCard(
                        title = oceanDrift.title,
                        description = oceanDrift.description,
                        colors = listOf(Color(0xFF5EEAD4), Color(0xFF38BDF8), Color(0xFF2563EB), Color(0xFF1E3A8A)),
                        isUnlocked = oceanDrift.isUnlocked,
                        costCoins = oceanDrift.costCoins,
                        userCoins = uiState.userProfile?.totalCoins ?: 0,
                        onBuy = { viewModel.buyItem(oceanDrift) }
                    )

                    // Neon grid
                    val neonGrid = uiState.storeItems.find { it.id == "pal_neon_grid" }
                        ?: StoreItemEntity(
                            id = "pal_neon_grid",
                            title = "Neon grid",
                            description = "Electric pink, cyan and violet.",
                            category = "PALETTE",
                            costCoins = 450,
                            isUnlocked = false,
                            previewHex = "#EC4899",
                            styleKey = "PALETTE_NEON"
                        )
                    PaletteItemCard(
                        title = neonGrid.title,
                        description = neonGrid.description,
                        colors = listOf(Color(0xFFEC4899), Color(0xFF06B6D4), Color(0xFF8B5CF6), Color(0xFF4338CA)),
                        isUnlocked = neonGrid.isUnlocked,
                        costCoins = neonGrid.costCoins,
                        userCoins = uiState.userProfile?.totalCoins ?: 0,
                        onBuy = { viewModel.buyItem(neonGrid) }
                    )
                }
            }

            // 6. MY PIGMENTS SECTION
            if (selectedTab == "ALL" || selectedTab == "CUSTOM") {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "MY PIGMENTS",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = PurpleHeader,
                            letterSpacing = 0.5.sp,
                            modifier = Modifier.padding(start = 2.dp)
                        )

                        TextButton(
                            onClick = { showColorMixer = !showColorMixer },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (showColorMixer) "Close Mixer ✕" else "Mix New Pigment 🧪",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = PurpleHeader
                            )
                        }
                    }

                    // Pigments Cards Horizontal Row
                    val pigmentScrollState = rememberScrollState()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(pigmentScrollState),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Amber trail
                        PigmentSwatchCard(
                            name = "Amber trail",
                            gradientColors = listOf(Color(0xFFFBBF24), Color(0xFFEA580C)),
                            statusText = "Owned"
                        )

                        // Coastal mist
                        PigmentSwatchCard(
                            name = "Coastal mist",
                            gradientColors = listOf(Color(0xFF38BDF8), Color(0xFF0284C7)),
                            statusText = "Owned"
                        )

                        // Dusk violet
                        PigmentSwatchCard(
                            name = "Dusk violet",
                            gradientColors = listOf(Color(0xFFA855F7), Color(0xFF6D28D9)),
                            statusText = "Owned"
                        )

                        // User-created pigments
                        uiState.customColors.forEach { customColor ->
                            if (customColor.name !in listOf("Amber trail", "Coastal mist", "Dusk violet")) {
                                val c = try {
                                    Color(android.graphics.Color.parseColor(customColor.hexCode))
                                } catch (_: Exception) {
                                    Color(0xFF8B5CF6)
                                }
                                PigmentSwatchCard(
                                    name = customColor.name,
                                    gradientColors = listOf(c.copy(alpha = 0.8f), c),
                                    statusText = "Owned"
                                )
                            }
                        }
                    }

                    // Expandable Color Laboratory Mixer
                    AnimatedVisibility(
                        visible = showColorMixer || selectedTab == "CUSTOM",
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color.White,
                            border = BorderStroke(1.dp, CardBorder),
                            shadowElevation = 2.dp,
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "Color Lab: Mix Custom Pigments",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextDark
                                )

                                val mixedColor = try {
                                    Color(android.graphics.Color.parseColor(uiState.mixedColorHex))
                                } catch (_: Exception) {
                                    Color(0xFF80D6C6)
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(54.dp)
                                            .clip(CircleShape)
                                            .background(mixedColor)
                                            .border(2.dp, Color(0xFFE5E7EB), CircleShape)
                                    )

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = uiState.mixedColorHex,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = TextDark
                                        )
                                        Text(
                                            text = "RGB(${uiState.redVal}, ${uiState.greenVal}, ${uiState.blueVal})",
                                            fontSize = 12.sp,
                                            color = TextMutedPurple
                                        )
                                    }
                                }

                                // Sliders for RGB
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text("Red: ${(redSlider * 255).toInt()}", fontSize = 11.sp, color = TextMutedPurple)
                                    Slider(
                                        value = redSlider,
                                        onValueChange = {
                                            redSlider = it
                                            viewModel.mixColor(redSlider, greenSlider, blueSlider)
                                        },
                                        colors = SliderDefaults.colors(
                                            thumbColor = Color(0xFFEF4444),
                                            activeTrackColor = Color(0xFFEF4444)
                                        )
                                    )

                                    Text("Green: ${(greenSlider * 255).toInt()}", fontSize = 11.sp, color = TextMutedPurple)
                                    Slider(
                                        value = greenSlider,
                                        onValueChange = {
                                            greenSlider = it
                                            viewModel.mixColor(redSlider, greenSlider, blueSlider)
                                        },
                                        colors = SliderDefaults.colors(
                                            thumbColor = Color(0xFF10B981),
                                            activeTrackColor = Color(0xFF10B981)
                                        )
                                    )

                                    Text("Blue: ${(blueSlider * 255).toInt()}", fontSize = 11.sp, color = TextMutedPurple)
                                    Slider(
                                        value = blueSlider,
                                        onValueChange = {
                                            blueSlider = it
                                            viewModel.mixColor(redSlider, greenSlider, blueSlider)
                                        },
                                        colors = SliderDefaults.colors(
                                            thumbColor = Color(0xFF3B82F6),
                                            activeTrackColor = Color(0xFF3B82F6)
                                        )
                                    )
                                }

                                var pigmentNameInput by remember { mutableStateOf("") }
                                OutlinedTextField(
                                    value = pigmentNameInput,
                                    onValueChange = { pigmentNameInput = it },
                                    placeholder = { Text("Pigment name (e.g., Midnight Horizon)", fontSize = 13.sp) },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp)
                                )

                                Button(
                                    onClick = {
                                        viewModel.saveCurrentPigment(
                                            name = pigmentNameInput.ifBlank { "Custom Pigment #${(100..999).random()}" }
                                        )
                                        pigmentNameInput = ""
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = PurpleButton),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "Save Pigment (+50 XP) ✨",
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

enum class BrushStrokeType {
    FINE_INK,
    CYBER_NEON,
    AQUARELLE,
    CHALK
}

@Composable
private fun BrushItemCard(
    title: String,
    description: String,
    isUnlocked: Boolean,
    costCoins: Int,
    userCoins: Int,
    brushType: BrushStrokeType,
    onBuy: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        border = BorderStroke(1.dp, CardBorder),
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Visual Preview Stroke
            when (brushType) {
                BrushStrokeType.FINE_INK -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF1E293B))
                    )
                }
                BrushStrokeType.CYBER_NEON -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        Color(0xFF22D3EE),
                                        Color(0xFF818CF8),
                                        Color(0xFFA855F7)
                                    )
                                )
                            )
                    )
                }
                BrushStrokeType.AQUARELLE -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        Color(0xFFC4B5FD),
                                        Color(0xFFA78BFA)
                                    )
                                )
                            )
                    )
                }
                BrushStrokeType.CHALK -> {
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                    ) {
                        val segmentWidth = 14.dp.toPx()
                        val gap = 6.dp.toPx()
                        var startX = 0f
                        while (startX < size.width) {
                            drawRoundRect(
                                color = Color(0xFFF59E0B),
                                topLeft = Offset(startX, 0f),
                                size = Size(segmentWidth.coerceAtMost(size.width - startX), size.height),
                                cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                            )
                            startX += segmentWidth + gap
                        }
                    }
                }
            }

            // Title, Description & Action Button Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = description,
                        fontSize = 12.sp,
                        color = TextMutedPurple,
                        lineHeight = 16.sp
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                if (isUnlocked) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFD1FAE5)
                    ) {
                        Text(
                            text = "Unlocked ✓",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF059669),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                } else {
                    Button(
                        onClick = onBuy,
                        enabled = userCoins >= costCoins,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PurpleButton,
                            disabledContainerColor = PurpleButton.copy(alpha = 0.4f)
                        ),
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "$costCoins",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(text = "🪙", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PaletteItemCard(
    title: String,
    description: String,
    colors: List<Color>,
    isUnlocked: Boolean,
    costCoins: Int,
    userCoins: Int,
    onBuy: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        border = BorderStroke(1.dp, CardBorder),
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 4 Color Circles Row
                Row(
                    horizontalArrangement = Arrangement.spacedBy((-6).dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    colors.take(4).forEach { color ->
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(1.5.dp, Color.White, CircleShape)
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = description,
                        fontSize = 12.sp,
                        color = TextMutedPurple,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            if (isUnlocked) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFD1FAE5)
                ) {
                    Text(
                        text = "Unlocked ✓",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF059669),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            } else {
                Button(
                    onClick = onBuy,
                    enabled = userCoins >= costCoins,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PurpleButton,
                        disabledContainerColor = PurpleButton.copy(alpha = 0.4f)
                    ),
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "$costCoins",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(text = "🪙", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun PigmentSwatchCard(
    name: String,
    gradientColors: List<Color>,
    statusText: String = "Owned"
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        border = BorderStroke(1.dp, CardBorder),
        shadowElevation = 1.dp,
        modifier = Modifier
            .width(115.dp)
            .height(135.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 14.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(gradientColors))
                    .border(1.5.dp, Color(0xFFF3F0F9), CircleShape)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = name,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = statusText,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = TextMutedPurple
            )
        }
    }
}
