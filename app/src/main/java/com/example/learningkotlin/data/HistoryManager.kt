package com.example.learningkotlin.data

import android.content.Context
import com.example.learningkotlin.model.FinishedWorkout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import kotlinx.coroutines.tasks.await

@Serializable
data class HistorySyncWrapper(
    val lastUpdated: Long,
    val workouts: List<FinishedWorkout>
)

object HistoryManager {
    private val json = Json { 
        ignoreUnknownKeys = true 
        coerceInputValues = true
        encodeDefaults = true
    }

    private val db get() = FirebaseFirestore.getInstance()

    private fun getFileName(): String {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "default"
        return "workout_history_$userId.json"
    }

    suspend fun saveFinishedWorkout(context: Context, workout: FinishedWorkout) {
        val history = loadHistory(context).toMutableList()
        history.add(0, workout)
        saveHistoryInternal(context, history)
    }

    suspend fun updateWorkout(context: Context, updatedWorkout: FinishedWorkout) {
        val history = loadHistory(context).toMutableList()
        val index = history.indexOfFirst { it.id == updatedWorkout.id }
        if (index != -1) {
            history[index] = updatedWorkout
            saveHistoryInternal(context, history)
        }
    }

    suspend fun deleteWorkout(context: Context, workoutId: Int) {
        val history = loadHistory(context).toMutableList()
        history.removeAll { it.id == workoutId }
        saveHistoryInternal(context, history)
    }

    private suspend fun saveHistoryInternal(context: Context, history: List<FinishedWorkout>) {
        val timestamp = System.currentTimeMillis()
        val wrapper = HistorySyncWrapper(timestamp, history)
        val jsonString = json.encodeToString(wrapper)

        // 1. Save Locally
        try {
            val file = File(context.filesDir, getFileName())
            file.writeText(jsonString)
        } catch (_: Exception) { }

        // 2. Save to Firebase
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            try {
                val dataMap = mapOf(
                    "lastUpdated" to timestamp,
                    "historyJson" to jsonString
                )
                db.collection("users").document(userId)
                    .collection("data").document("history")
                    .set(dataMap)
            } catch (_: Exception) { }
        }
    }

    suspend fun loadHistory(context: Context): List<FinishedWorkout> {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return emptyList()
        val file = File(context.filesDir, getFileName())

        // 1. Load Local
        var localWrapper: HistorySyncWrapper? = null
        if (file.exists()) {
            try {
                val content = file.readText()
                localWrapper = if (content.contains("lastUpdated")) {
                    json.decodeFromString<HistorySyncWrapper>(content)
                } else {
                    val oldList = json.decodeFromString<List<FinishedWorkout>>(content)
                    HistorySyncWrapper(0, oldList)
                }
            } catch (_: Exception) { }
        }

        // 2. Sync with Firebase
        try {
            val document = db.collection("users").document(userId)
                .collection("data").document("history")
                .get()
                .await()

            if (document.exists()) {
                val remoteTimestamp = document.getLong("lastUpdated") ?: 0L
                val remoteJson = document.getString("historyJson")

                if (remoteJson != null) {
                    val remoteWorkouts = json.decodeFromString<List<FinishedWorkout>>(remoteJson)
                    val remoteWrapper = HistorySyncWrapper(remoteTimestamp, remoteWorkouts)
                    val localTimestamp = localWrapper?.lastUpdated ?: -1L

                    if (remoteTimestamp > localTimestamp) {
                        // Remote is newer
                        val fullRemoteJson = json.encodeToString(remoteWrapper)
                        file.writeText(fullRemoteJson)
                        return remoteWrapper.workouts
                    } else if (localTimestamp > remoteTimestamp) {
                        // Local is newer
                        if (localWrapper != null) {
                            saveHistoryInternal(context, localWrapper.workouts)
                        }
                    }
                }
            } else if (localWrapper != null) {
                saveHistoryInternal(context, localWrapper.workouts)
            }
        } catch (_: Exception) { }

        return localWrapper?.workouts ?: emptyList()
    }
}
