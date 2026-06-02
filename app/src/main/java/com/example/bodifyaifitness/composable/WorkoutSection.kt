package com.example.bodifyaifitness.composable

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.bodifyaifitness.dataclass.WorkoutCategory

@Composable
fun WorkoutSection(categories: List<WorkoutCategory>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Workout Categories",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(15.dp)
        )
        LazyColumn(
            contentPadding = PaddingValues(start = 15.dp, end = 15.dp, bottom = 100.dp),
            modifier = Modifier.fillMaxHeight()
        ) {
            items(categories) { category ->
                WorkoutCard(category = category)
            }
        }
    }
}
