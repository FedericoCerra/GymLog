package com.example.learningkotlin.model
import kotlinx.serialization.Serializable

@Serializable
data class Workout(
    val id: Int,
    var name: String,
    val exercises: MutableList<Exercise> = mutableListOf() // Starts empty
)