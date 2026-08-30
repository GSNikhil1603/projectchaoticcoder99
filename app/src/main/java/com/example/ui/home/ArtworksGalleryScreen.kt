package com.example.ui.home

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.ArtworkThumbnailCard
import com.example.ui.theme.*
import com.example.util.ArtworkShareHelper
import kotlinx.coroutines.launch

@Composable
fun ArtworksGalleryScreen(
    viewModel: HomeViewModel,
    onNavigateToStudio: (Long) -> Unit,
    onNavigateToTracker: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val displayedRoutes = when (uiState.selectedFilter) {
        HomeFilter.ALL -> uiState.routes
        HomeFilter.FAVORITES -> uiState.favoriteRoutes
    }

    Scaffold(
        containerColor = Color(0xFFFAFAFA),
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Artworks Gallery",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 26.sp
                        ),
                        color = Color(0xFF111827)
                    )
                    Text(
                        text = "${displayedRoutes.size} walk artworks created",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF6B7280)
                    )
                }

                // Start Walk Button
                Button(
                    onClick = onNavigateToTracker,
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New Walk", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            // Filter Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // All Pill
                val isAllSelected = uiState.selectedFilter == HomeFilter.ALL
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (isAllSelected) Color(0xFF111827) else Color.White,
                    border = if (isAllSelected) null else BorderStroke(1.dp, Color(0xFFE5E7EB)),
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { viewModel.setFilter(HomeFilter.ALL) }
                ) {
                    Text(
                        text = "All (${uiState.routes.size})",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = if (isAllSelected) Color.White else Color(0xFF374151),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }

                // Favorites Pill
                val isFavSelected = uiState.selectedFilter == HomeFilter.FAVORITES
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (isFavSelected) Color(0xFF111827) else Color.White,
                    border = if (isFavSelected) null else BorderStroke(1.dp, Color(0xFFE5E7EB)),
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { viewModel.setFilter(HomeFilter.FAVORITES) }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = if (isFavSelected) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = null,
                            tint = if (isFavSelected) Color.Red else Color(0xFF6B7280),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Favorites (${uiState.favoriteRoutes.size})",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = if (isFavSelected) Color.White else Color(0xFF374151)
                        )
                    }
                }
            }

            // Grid of Artworks
            if (displayedRoutes.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(text = "🎨", fontSize = 48.sp)
                        Text(
                            text = "No artworks found",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF111827)
                        )
                        Text(
                            text = "Complete your first GPS walk to generate artwork!",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF6B7280)
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(displayedRoutes, key = { it.id }) { route ->
                        ArtworkThumbnailCard(
                            route = route,
                            onClick = { onNavigateToStudio(route.id) },
                            onFavoriteToggle = { isFav ->
                                viewModel.toggleFavorite(route.id, isFav)
                            },
                            onShareClick = {
                                coroutineScope.launch {
                                    ArtworkShareHelper.shareArtwork(
                                        context = context,
                                        route = route,
                                        studentName = uiState.userProfile?.username ?: "Campus Artist"
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
