package com.example.learningkotlin.data

import android.content.Context
import com.example.learningkotlin.model.ExerciseDefinition
import kotlinx.serialization.json.Json
import java.io.InputStreamReader

object ExerciseLibrary {
    private var definitions: List<ExerciseDefinition> = emptyList()

    fun load(context: Context) {
        if (definitions.isNotEmpty()) return
        
        try {
            val assets = context.assets
            val files = assets.list("exercises") ?: emptyArray()
            
            val loadedDefinitions = mutableListOf<ExerciseDefinition>()
            val jsonParser = Json { 
                ignoreUnknownKeys = true 
                coerceInputValues = true
            }

            for (fileName in files) {
                if (fileName.endsWith(".json")) {
                    try {
                        assets.open("exercises/$fileName").use { inputStream ->
                            val reader = InputStreamReader(inputStream)
                            val jsonString = reader.readText()
                            val def = jsonParser.decodeFromString<ExerciseDefinition>(jsonString)
                            loadedDefinitions.add(def)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
            
            definitions = loadedDefinitions
            
        } catch (e: Exception) {
            e.printStackTrace()
            definitions = listOf(
                ExerciseDefinition(id = "bench_press", name = "Bench Press (Barbell)", primaryMuscles = listOf("chest")),
                ExerciseDefinition(id = "squat", name = "Squat (Barbell)", primaryMuscles = listOf("quads"))
            )
        }
    }

    fun getDefinitions(): List<ExerciseDefinition> = definitions.sortedBy { it.name }

    fun getAllMuscles(): List<String> {
        return definitions.flatMap { it.primaryMuscles }.distinct().sorted()
    }
}
