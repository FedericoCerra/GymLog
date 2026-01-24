package com.example.learningkotlin.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.learningkotlin.model.WorkoutSet

@Composable
fun SetRow(
    index: Int,
    set: WorkoutSet,
    onDelete: () -> Unit
) {
    var weightText by remember { mutableStateOf(set.weight.toString().removeSuffix(".0")) }
    var repsText by remember { mutableStateOf(set.reps.toString()) }
    var isChecked by remember { mutableStateOf(set.isDone) }
    var showMenu by remember { mutableStateOf(false) }

    // Logic for Green Row background (keep hardcoded green or use primaryContainer)
    val rowColor = if (isChecked) Color(0xFF4CAF50).copy(alpha = 0.2f) else Color.Transparent

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(rowColor, RoundedCornerShape(4.dp))
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. Set Number
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$index",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showMenu = true }
            )
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Delete Set") },
                    onClick = {
                        onDelete()
                        showMenu = false
                    }
                )
            }
        }

        // 2. Previous
        Text(
            text = "-",
            modifier = Modifier.weight(2f),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        // 3. Inputs
        TableInput(
            value = weightText,
            onValueChange = {
                weightText = it
                set.weight = it.toDoubleOrNull() ?: 0.0
            },
            modifier = Modifier.weight(1.5f)
        )

        TableInput(
            value = repsText,
            onValueChange = {
                repsText = it
                set.reps = it.toIntOrNull() ?: 0
            },
            modifier = Modifier.weight(1.5f)
        )

        // 4. Checkbox
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
            Surface(
                onClick = {
                    isChecked = !isChecked
                    set.isDone = isChecked
                },
                shape = RoundedCornerShape(4.dp),
                color = if (isChecked) Color(0xFF4CAF50) else MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.size(28.dp)
            ) {
                if (isChecked) {
                    Icon(Icons.Default.Check, "Done", tint = Color.White, modifier = Modifier.padding(4.dp))
                }
            }
        }
    }
}

// Private helper just for this file (or move to a generic file if used elsewhere)
@Composable
private fun TableInput(value: String, onValueChange: (String) -> Unit, modifier: Modifier) {
    Box(
        modifier = modifier.padding(horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = TextStyle(
                textAlign = TextAlign.Center,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    MaterialTheme.colorScheme.surfaceVariant,
                    RoundedCornerShape(4.dp)
                )
                .padding(vertical = 8.dp)
        )
    }
}