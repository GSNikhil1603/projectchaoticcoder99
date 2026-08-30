package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.example.data.model.PointF
import com.example.ui.theme.*

data class CampusLandmark(
    val id: String = "wp",
    val name: String = "Waypoint",
    val emoji: String = "📍",
    val x: Float = 500f,
    val y: Float = 500f,
    val description: String = "GPS Waypoint"
)

val VIT_CAMPUS_LANDMARKS = emptyList<CampusLandmark>()

@Composable
fun CampusMapView(
    walkPath: List<PointF>,
    isTracking: Boolean,
    modifier: Modifier = Modifier,
    selectedLandmarkId: String? = null,
    onLandmarkClick: ((CampusLandmark) -> Unit)? = null,
    onPointAdded: ((PointF) -> Unit)? = null
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFF1E2622)) // Dark slate-green coordinate canvas
            .pointerInput(isTracking) {
                if (isTracking) {
                    detectDragGestures { change, _ ->
                        change.consume()
                        val normX = (change.position.x / size.width) * 1000f
                        val normY = (change.position.y / size.height) * 1000f
                        onPointAdded?.invoke(PointF(normX, normY))
                    }
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val scaleX = width / 1000f
            val scaleY = height / 1000f

            // 1. Draw Clean Coordinate Grid
            val gridStep = 80.dp.toPx()
            var gx = 0f
            while (gx < width) {
                drawLine(
                    color = Color(0xFF2D3B34),
                    start = Offset(gx, 0f),
                    end = Offset(gx, height),
                    strokeWidth = 1f
                )
                gx += gridStep
            }

            var gy = 0f
            while (gy < height) {
                drawLine(
                    color = Color(0xFF2D3B34),
                    start = Offset(0f, gy),
                    end = Offset(width, gy),
                    strokeWidth = 1f
                )
                gy += gridStep
            }

            // 2. Draw Live GPS Coordinate Path
            if (walkPath.size >= 2) {
                val path = Path()
                path.moveTo(walkPath[0].x * scaleX, walkPath[0].y * scaleY)
                for (i in 1 until walkPath.size) {
                    path.lineTo(walkPath[i].x * scaleX, walkPath[i].y * scaleY)
                }

                // Glowing background stroke
                drawPath(
                    path = path,
                    color = Color(0x6610B981),
                    style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
                // Foreground path
                drawPath(
                    path = path,
                    color = Color(0xFF34D399),
                    style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                )

                // Start Marker (Origin)
                drawCircle(
                    color = Color(0xFF10B981),
                    radius = 7.dp.toPx(),
                    center = Offset(walkPath.first().x * scaleX, walkPath.first().y * scaleY)
                )
                drawCircle(
                    color = Color.White,
                    radius = 3.dp.toPx(),
                    center = Offset(walkPath.first().x * scaleX, walkPath.first().y * scaleY)
                )

                // Current Live Position Marker (pulsing)
                val currentPt = walkPath.last()
                drawCircle(
                    color = Color(0xFF60A5FA).copy(alpha = pulseAlpha),
                    radius = 16.dp.toPx(),
                    center = Offset(currentPt.x * scaleX, currentPt.y * scaleY)
                )
                drawCircle(
                    color = Color.White,
                    radius = 7.dp.toPx(),
                    center = Offset(currentPt.x * scaleX, currentPt.y * scaleY)
                )
                drawCircle(
                    color = Color(0xFF3B82F6),
                    radius = 4.dp.toPx(),
                    center = Offset(currentPt.x * scaleX, currentPt.y * scaleY)
                )
            }
        }

        // Live Drawing Status Banner if tracking
        if (isTracking && walkPath.size < 5) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF111827).copy(alpha = 0.9f))
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "🛰️ Walk to trace live GPS coordinates in real-time",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White
                )
            }
        }
    }
}
