package com.example.learningkotlin.model

data class Exercise(
    val id: Int,
    val name: String,
    val sets: MutableList<WorkoutSet> // <--- THE BIG CHANGE: A list of rows
)