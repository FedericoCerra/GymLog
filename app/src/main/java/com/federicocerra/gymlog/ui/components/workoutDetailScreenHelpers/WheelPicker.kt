package com.federicocerra.gymlog.ui.components.workoutDetailScreenHelpers

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WheelPicker(
    items: List<String>,
    initialIndex: Int,
    onIndexSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val itemHeight = 50.dp
    val visibleItemsCount = 3
    val scrollState = rememberLazyListState(initialFirstVisibleItemIndex = initialIndex)
    val snapFlingBehavior = rememberSnapFlingBehavior(lazyListState = scrollState)

    // Sync state back to caller
    LaunchedEffect(scrollState.isScrollInProgress) {
        if (!scrollState.isScrollInProgress) {
            onIndexSelected(scrollState.firstVisibleItemIndex)
        }
    }

    Box(
        modifier = modifier.height(itemHeight * visibleItemsCount),
        contentAlignment = Alignment.Center
    ) {
        // Highlighting bar behind the selected item
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(itemHeight)
                .background(Color.White.copy(alpha = 0.05f), MaterialTheme.shapes.medium)
        )

        LazyColumn(
            state = scrollState,
            flingBehavior = snapFlingBehavior,
            contentPadding = PaddingValues(vertical = itemHeight), // Offset so center is visible
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(items.size) { index ->
                Box(
                    modifier = Modifier
                        .height(itemHeight)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    val isSelected by remember { 
                        derivedStateOf { scrollState.firstVisibleItemIndex == index } 
                    }
                    Text(
                        text = items[index],
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Normal,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray,
                        modifier = Modifier.alpha(if (isSelected) 1f else 0.4f)
                    )
                }
            }
        }
    }
}

@Composable
fun TimeWheelPicker(
    initialTotalSeconds: Int,
    isHoursMode: Boolean = false,
    onTimeChange: (Int) -> Unit
) {
    var hours by remember { mutableIntStateOf(if (isHoursMode) initialTotalSeconds / 3600 else 0) }
    var minutes by remember { mutableIntStateOf(if (isHoursMode) (initialTotalSeconds % 3600) / 60 else initialTotalSeconds / 60) }
    var seconds by remember { mutableIntStateOf(if (isHoursMode) 0 else initialTotalSeconds % 60) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isHoursMode) {
            // Hours
            WheelPicker(
                items = (0..23).map { it.toString().padStart(2, '0') },
                initialIndex = hours,
                onIndexSelected = { 
                    hours = it
                    onTimeChange(hours * 3600 + minutes * 60)
                },
                modifier = Modifier.weight(1f)
            )
            Text("h", color = Color.Gray, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp))
        }

        // Minutes
        WheelPicker(
            items = (0..59).map { it.toString().padStart(2, '0') },
            initialIndex = minutes,
            onIndexSelected = { 
                minutes = it
                if (isHoursMode) {
                    onTimeChange(hours * 3600 + minutes * 60)
                } else {
                    onTimeChange(minutes * 60 + seconds)
                }
            },
            modifier = Modifier.weight(1f)
        )
        
        Text(if (isHoursMode) "m" else "m", color = Color.Gray, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp))

        if (!isHoursMode) {
            // Seconds
            WheelPicker(
                items = (0..59 step 5).map { it.toString().padStart(2, '0') },
                initialIndex = (seconds / 5).coerceIn(0, 11),
                onIndexSelected = { 
                    seconds = it * 5
                    onTimeChange(minutes * 60 + seconds)
                },
                modifier = Modifier.weight(1f)
            )
            Text("s", color = Color.Gray, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp))
        }
    }
}
