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
    var type: SetType = SetType.NORMAL,
    var isWeightPR: Boolean = false,
    var is1RMPR: Boolean = false,
    var isVolumePR: Boolean = false
) {
    fun calculate1RM(): Double {
        if (reps <= 0) return 0.0
        if (reps == 1) return weight
        return weight * (1 + reps / 30.0)
    }

    fun calculateVolume(): Double = weight * reps
}
