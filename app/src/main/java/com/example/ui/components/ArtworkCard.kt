package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.WalkRouteEntity
import com.example.ui.theme.*

/**
 * Clean square Creation Card matching the friendly modern aesthetic
 */
@Composable
fun CreationCardItem(
    route: WalkRouteEntity,
    index: Int = 0,
    onClick: () -> Unit,
    onFavoriteToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val heartColor by animateColorAsState(
        targetValue = if (route.isFavorite) ElectricPurple else TextMuted.copy(alpha = 0.5f),
        animationSpec = spring(),
        label = "heartColor"
    )

    // Soft glowing pastel background per card
    val glowColor = when (index % 4) {
        0 -> Color(0xFFF5F3FF) // Soft Electric Purple
        1 -> Color(0xFFECFDF5) // Soft Mint
        2 -> Color(0xFFEFF6FF) // Soft Sky
        else -> Color(0xFFFFF7ED) // Soft Amber
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .width(82.dp)
            .clickable(onClick = onClick)
            .testTag("creation_card_${route.id}")
    ) {
        // Square Artwork Card (14-16dp rounded corners, subtle shadow, clean border)
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = PureWhiteSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, SubtleBorder),
            shadowElevation = 0.5.dp,
            modifier = Modifier.size(82.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                glowColor,
                                PureWhiteSurface
                            )
                        )
                    )
                    .padding(6.dp)
            ) {
                // Generative artwork mini canvas
                ArtCanvasView(
                    pointsJson = route.pointsJson,
                    blobsJson = route.blobsJson,
                    artStyle = route.artStyle,
                    modifier = Modifier.fillMaxSize()
                )

                // Favorite Heart Badge Top-Right
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.85f))
                        .clickable { onFavoriteToggle(!route.isFavorite) }
                        .testTag("creation_fav_${route.id}"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (route.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = heartColor,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Date text underneath (e.g. "23 Feb")
        Text(
            text = formatShortDate(route.dateString),
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp
            ),
            color = NearBlackInk
        )
    }
}

@Composable
fun ArtworkThumbnailCard(
    route: WalkRouteEntity,
    onClick: () -> Unit,
    onFavoriteToggle: (Boolean) -> Unit,
    onShareClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val heartColor by animateColorAsState(
        targetValue = if (route.isFavorite) ElectricPurple else TextMuted,
        animationSpec = spring(),
        label = "heartColor"
    )

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(PureWhiteSurface)
            .border(1.dp, SubtleBorder, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(10.dp)
            .testTag("artwork_card_${route.id}")
    ) {
        // Thumbnail Art Canvas Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.05f)
                .clip(RoundedCornerShape(12.dp))
                .background(MutedSurface)
                .padding(8.dp)
        ) {
            ArtCanvasView(
                pointsJson = route.pointsJson,
                blobsJson = route.blobsJson,
                artStyle = route.artStyle,
                modifier = Modifier.fillMaxSize()
            )

            // Top Action Buttons: Share (Left) & Favorite (Right)
            if (onShareClick != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.9f))
                        .clickable { onShareClick() }
                        .testTag("share_btn_${route.id}"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share Artwork",
                        tint = TextSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            // Favorite Button Top-Right
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.9f))
                    .clickable { onFavoriteToggle(!route.isFavorite) }
                    .testTag("fav_btn_${route.id}"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (route.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = heartColor,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Date and Title
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = formatShortDate(route.dateString),
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = NearBlackInk
            )
            Text(
                text = "${route.distanceKm} km",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted
            )
        }
    }
}

private fun formatShortDate(dateStr: String): String {
    val parts = dateStr.split(" ")
    return if (parts.size >= 2) {
        "${parts[0]} ${parts[1].take(3)}"
    } else {
        dateStr
    }
}
