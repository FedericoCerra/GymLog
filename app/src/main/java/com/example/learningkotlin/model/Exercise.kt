package com.example.learningkotlin.model
import kotlinx.serialization.Serializable

@Serializable
data class Exercise(
    val id: Int,
    var name: String,
    val sets: MutableList<WorkoutSet>,
    var restTimer: Int = 90
)