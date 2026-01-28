package com.example.learningkotlin.model

import kotlinx.serialization.Serializable

@Serializable
data class FinishedWorkout(
    val id: Int,
    val name: String,
    var date: Long, // Changed to var to allow modification
    var durationSeconds: Long, // Changed to var to allow modification
    val totalVolume: Double,
    val totalSets: Int,
    val exercises: List<Exercise>
)