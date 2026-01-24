package com.example.learningkotlin.model

data class Workout(
    val id: Int,
    val name: String,
    val exercises: MutableList<Exercise> = mutableListOf() // Starts empty
)