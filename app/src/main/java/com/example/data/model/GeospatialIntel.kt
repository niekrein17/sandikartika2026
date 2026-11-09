package com.example.data.model

/**
 * Data model for Tactical Geospatial Intelligence & Contextual Alibi
 * powered by Gemini 3.8 Flash with Google Maps Grounding.
 */
data class GeospatialIntel(
    val coordinates: String,
    val locationName: String,
    val terrainSummary: String,
    val civilianRoEAssessment: String,
    val medevacFacilities: List<String>,
    val strategicLandmarks: List<String>,
    val smartDecoyTitle: String,
    val smartDecoyMessage: String,
    val isMapsGrounded: Boolean = true,
    val sourceAttribution: String = "Google Maps Data via Gemini 3.8 Flash Grounding",
    val generatedAtEpochMs: Long = System.currentTimeMillis()
)
