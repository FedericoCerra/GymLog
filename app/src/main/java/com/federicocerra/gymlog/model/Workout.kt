package com.federicocerra.gymlog.model
import kotlinx.serialization.Serializable

@Serializable
data class Workout(
    val id: Int,
    var name: String,
    val exercises: MutableList<Exercise> = mutableListOf(),
    var isActive: Boolean = false,
    var startTime: Long? = null
)