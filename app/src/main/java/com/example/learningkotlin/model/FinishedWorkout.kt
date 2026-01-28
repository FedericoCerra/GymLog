package com.example.learningkotlin.model

import kotlinx.serialization.Serializable

@Serializable
data class FinishedWorkout(
    val id: Int,
    val name: String,
    val date: Long, // timestamp
    val durationSeconds: Long,
    val totalVolume: Double,
    val totalSets: Int,
    val exercises: List<Exercise> // Copy of exercises as they were at finish
)