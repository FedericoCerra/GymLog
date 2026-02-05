package com.federicocerra.gymlog.data

import android.content.Context
import android.util.Log
import com.federicocerra.gymlog.model.Workout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import kotlinx.coroutines.tasks.await

@Serializable
data class WorkoutSyncWrapper(
    val lastUpdated: Long,
    val routines: List<Workout>
)

object WorkoutManager {
    private const val TAG = "WorkoutManager"
    private val json = Json { 
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
    
    private val db get() = FirebaseFirestore.getInstance()

    private fun getFileName(): String {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "default"
        return "my_workouts_$userId.json"
    }

    suspend fun saveWorkouts(context: Context, workouts: List<Workout>) {
        val timestamp = System.currentTimeMillis()
        val wrapper = WorkoutSyncWrapper(timestamp, workouts)
        val jsonString = json.encodeToString(wrapper)

        // 1. Save Locally
        try {
            val file = File(context.filesDir, getFileName())
            file.writeText(jsonString)
        } catch (e: Exception) { 
            Log.e(TAG, "Error saving workouts locally", e)
        }

        // 2. Push to Firebase
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            try {
                val dataMap = mapOf(
                    "lastUpdated" to timestamp,
                    "routinesJson" to jsonString 
                )
                db.collection("users").document(userId)
                    .collection("data").document("workouts")
                    .set(dataMap)
                    .await()
            } catch (e: Exception) { 
                Log.e(TAG, "Error saving workouts to Firebase", e)
            }
        } else {
            Log.w(TAG, "User not authenticated, skipping Firebase save.")
        }
    }

    suspend fun loadWorkouts(context: Context): List<Workout> {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return emptyList()
        val file = File(context.filesDir, getFileName())

        var localWrapper: WorkoutSyncWrapper? = null
        if (file.exists()) {
            try {
                val content = file.readText()
                localWrapper = if (content.contains("lastUpdated")) {
                    json.decodeFromString<WorkoutSyncWrapper>(content)
                } else {
                    val oldList = json.decodeFromString<List<Workout>>(content)
                    WorkoutSyncWrapper(0, oldList)
                }
            } catch (e: Exception) { 
                 Log.e(TAG, "Error loading local workouts", e)
            }
        }

        try {
            val document = db.collection("users").document(userId)
                .collection("data").document("workouts")
                .get()
                .await()

            if (document.exists()) {
                val remoteTimestamp = document.getLong("lastUpdated") ?: 0L
                val remoteJsonContent = document.getString("routinesJson")
                
                if (remoteJsonContent != null) {
                    val remoteRoutines = if (remoteJsonContent.contains("lastUpdated")) {
                        json.decodeFromString<WorkoutSyncWrapper>(remoteJsonContent).routines
                    } else {
                        json.decodeFromString<List<Workout>>(remoteJsonContent)
                    }
                    
                    val remoteWrapper = WorkoutSyncWrapper(remoteTimestamp, remoteRoutines)
                    val localTimestamp = localWrapper?.lastUpdated ?: -1L
                    
                    if (remoteTimestamp > localTimestamp) {
                        val fullRemoteJson = json.encodeToString(remoteWrapper)
                        file.writeText(fullRemoteJson)
                        return remoteWrapper.routines
                    } else if (localTimestamp > remoteTimestamp && localWrapper != null) {
                        saveWorkouts(context, localWrapper.routines)
                    }
                }
            } else if (localWrapper != null) {
                saveWorkouts(context, localWrapper.routines)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing workouts from Firebase", e)
        }

        return localWrapper?.routines ?: emptyList()
    }
}
