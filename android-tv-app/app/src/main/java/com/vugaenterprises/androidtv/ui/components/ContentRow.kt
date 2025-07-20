package com.vugaenterprises.androidtv.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vugaenterprises.androidtv.data.model.Content

@Composable
fun ContentRow(
    title: String,
    content: List<Content>,
    onContentClick: (Content) -> Unit,
    modifier: Modifier = Modifier,
    showSeeAll: Boolean = false,
    onSeeAllClick: (() -> Unit)? = null
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontSize = 20.sp
                ),
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            
            if (showSeeAll && onSeeAllClick != null) {
                TextButton(
                    onClick = onSeeAllClick,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color.White.copy(alpha = 0.8f)
                    )
                ) {
                    Text(
                        text = "See All",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Light
                    )
                }
            }
        }
        
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(content) { item ->
                ContentCard(
                    content = item,
                    onClick = { onContentClick(item) }
                )
            }
        }
    }
} 