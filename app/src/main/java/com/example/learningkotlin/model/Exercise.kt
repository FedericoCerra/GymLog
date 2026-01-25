package com.example.learningkotlin.model

data class Exercise(
    val id: Int,
    var name: String,
    val sets: MutableList<WorkoutSet>,
    var restTimer: Int = 90
)