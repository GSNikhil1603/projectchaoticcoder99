package com.example.util

import com.example.data.model.GpsCoordinate

/**
 * Catmull-Rom spline interpolation utility to convert rigid GPS coordinates into
 * aesthetic, fluid curved paths for map rendering and art stroke generation.
 */
object CurveInterpolation {

    /**
     * Calculates a single 1D point on a Catmull-Rom spline segment.
     */
    fun catmullRomPoint(p0: Double, p1: Double, p2: Double, p3: Double, t: Float): Double {
        val v0 = (p2 - p0) * 0.5
        val v1 = (p3 - p1) * 0.5
        val t2 = (t * t).toDouble()
        val t3 = t2 * t.toDouble()
        return (2.0 * p1 - 2.0 * p2 + v0 + v1) * t3 +
                (-3.0 * p1 + 3.0 * p2 - 2.0 * v0 - v1) * t2 +
                v0 * t.toDouble() +
                p1
    }

    /**
     * Generates a high-density, smoothed list of coordinates from an input list of waypoints.
     */
    fun generateSmoothPath(
        points: List<GpsCoordinate>,
        stepsPerSegment: Int = 20
    ): List<GpsCoordinate> {
        if (points.size < 2) return points

        val smoothed = mutableListOf<GpsCoordinate>()
        // Pad both ends with duplicate points to smoothly interpolate through endpoints
        val extended = listOf(points.first()) + points + listOf(points.last())

        for (i in 1 until extended.size - 2) {
            val p0 = extended[i - 1]
            val p1 = extended[i]
            val p2 = extended[i + 1]
            val p3 = extended[i + 2]

            for (s in 0 until stepsPerSegment) {
                val t = s.toFloat() / stepsPerSegment.toFloat()
                val lat = catmullRomPoint(p0.latitude, p1.latitude, p2.latitude, p3.latitude, t)
                val lng = catmullRomPoint(p0.longitude, p1.longitude, p2.longitude, p3.longitude, t)
                val alt = catmullRomPoint(p0.altitudeMeters, p1.altitudeMeters, p2.altitudeMeters, p3.altitudeMeters, t)

                val interpTimestamp = (p1.timestamp + (p2.timestamp - p1.timestamp) * t).toLong()

                smoothed.add(
                    GpsCoordinate(
                        latitude = lat,
                        longitude = lng,
                        altitudeMeters = alt,
                        accuracyMeters = p1.accuracyMeters,
                        speedKmh = p1.speedKmh,
                        timestamp = interpTimestamp
                    )
                )
            }
        }

        smoothed.add(points.last())
        return smoothed
    }
}
