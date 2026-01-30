package com.example.learningkotlin.model
import kotlinx.serialization.Serializable

@Serializable
enum class SetType {
    NORMAL, WARMUP, DROP, FAILURE
}

@Serializable
data class WorkoutSet(
    val id: Int,
    var weight: Double,
    var reps: Int,
    var isDone: Boolean = false,
    var type: SetType = SetType.NORMAL
)