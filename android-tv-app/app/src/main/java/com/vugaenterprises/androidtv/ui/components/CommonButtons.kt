package com.vugaenterprises.androidtv.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TVButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }
    
    Button(
        onClick = onClick,
        modifier = modifier
            .onFocusChanged { isFocused = it.isFocused }
            .height(56.dp)
            .widthIn(min = 200.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isFocused) Color.White else Color(0xFF464646),
            contentColor = if (isFocused) Color.Black else Color.White
        ),
        shape = RoundedCornerShape(28.dp)
    ) {
        Text(
            text = text,
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
    }
}

@Composable
fun TVOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }
    
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .onFocusChanged { isFocused = it.isFocused }
            .height(56.dp)
            .widthIn(min = 200.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (isFocused) Color.White else Color.Transparent,
            contentColor = if (isFocused) Color.Black else Color.White
        ),
        border = ButtonDefaults.outlinedButtonBorder.copy(
            brush = Brush.linearGradient(
                colors = listOf(
                    if (isFocused) Color.White else Color.White.copy(alpha = 0.5f),
                    if (isFocused) Color.White else Color.White.copy(alpha = 0.5f)
                )
            ),
            width = 2.dp
        ),
        shape = RoundedCornerShape(28.dp)
    ) {
        Text(
            text = text,
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
    }
}