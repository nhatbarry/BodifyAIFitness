package com.example.bodifyaifitness.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.res.stringResource
import com.example.bodifyaifitness.R
import com.example.bodifyaifitness.dataclass.Exercise
import com.example.bodifyaifitness.ui.theme.ChipInactive
import com.example.bodifyaifitness.ui.theme.GymOrange
import com.example.bodifyaifitness.ui.theme.TextMuted
import com.example.bodifyaifitness.ui.theme.TextWhite

@Composable
fun SearchBarSection(
    query: String,
    results: List<Exercise>,
    selectedCategory: String,
    onQueryChange: (String) -> Unit,
    onResultClick: (Exercise) -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    var textFieldSize by remember { mutableStateOf(Size.Zero) }
    var textFieldOffsetY by remember { mutableStateOf(0) }

    val showDropdown = query.isNotBlank() && results.isNotEmpty()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 15.dp, vertical = 8.dp)
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = {
                Text(
                    text = if (selectedCategory == "All") stringResource(R.string.placeholder_search_all)
                           else stringResource(R.string.placeholder_search_category, selectedCategory),
                    color = TextMuted,
                    fontSize = 14.sp
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = if (query.isNotBlank()) GymOrange else TextMuted,
                    modifier = Modifier.size(20.dp)
                )
            },
            trailingIcon = {
                if (query.isNotBlank()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = null, tint = TextMuted, modifier = Modifier.size(18.dp))
                    }
                }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = {}),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor    = GymOrange,
                unfocusedBorderColor  = Color(0xFF2A2A3E),
                focusedTextColor      = TextWhite,
                unfocusedTextColor    = TextWhite,
                cursorColor           = GymOrange,
                focusedContainerColor = Color(0xFF12121F),
                unfocusedContainerColor = Color(0xFF12121F)
            ),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned { coords ->
                    textFieldSize = coords.size.toSize()
                    textFieldOffsetY = coords.positionInWindow().y.toInt() +
                            coords.size.height
                }
        )

        // Dropdown overlay dùng Popup để float trên các element khác
        if (showDropdown) {
            Popup(
                alignment = Alignment.TopStart,
                offset = IntOffset(
                    x = 0,
                    y = with(density) { textFieldSize.height.toDp().roundToPx() }
                ),
                properties = PopupProperties(focusable = false)
            ) {
                Box(
                    modifier = Modifier
                        .width(with(density) { textFieldSize.width.toDp() })
                        .shadow(12.dp, RoundedCornerShape(14.dp))
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF1A1A2E))
                ) {
                    LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                        items(results) { exercise ->
                            SearchResultItem(
                                exercise = exercise,
                                onClick = { onResultClick(exercise) }
                            )
                            if (exercise != results.last()) {
                                Divider(
                                    color = Color(0xFF2A2A3E),
                                    thickness = 0.5.dp,
                                    modifier = Modifier.padding(horizontal = 12.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchResultItem(
    exercise: Exercise,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Icon nhỏ
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(GymOrange.copy(alpha = 0.12f))
        ) {
            Icon(
                imageVector = Icons.Default.FitnessCenter,
                contentDescription = null,
                tint = GymOrange,
                modifier = Modifier.size(16.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Tên bài tập
        Text(
            text = exercise.name.replaceFirstChar { it.uppercase() },
            color = TextWhite,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            modifier = Modifier.weight(1f)
        )

        // Category tag nhỏ
        if (exercise.category.isNotEmpty()) {
            Text(
                text = exercise.category.replaceFirstChar { it.uppercase() },
                color = TextMuted,
                fontSize = 11.sp
            )
        }
    }
}
