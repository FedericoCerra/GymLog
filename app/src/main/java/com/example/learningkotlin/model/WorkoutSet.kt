package com.example.learningkotlin.model
import kotlinx.serialization.Serializable

@Serializable
data class WorkoutSet(
    val id: Int,
    var weight: Double,
    var reps: Int,
    var isDone: Boolean = false // Mutable so we can check the box
)