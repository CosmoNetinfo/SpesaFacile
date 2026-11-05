package com.example.spesafacile.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp

@Composable
fun NotebookSheet(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val line = MaterialTheme.colorScheme.outline.copy(alpha = 0.09f)
    val margin = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
    Surface(modifier, shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.surface, shadowElevation = 1.dp) {
        Box(Modifier.drawBehind {
            var y = 32.dp.toPx()
            while (y < size.height) { drawLine(line, Offset(0f, y), Offset(size.width, y), 1.dp.toPx()); y += 32.dp.toPx() }
            drawLine(margin, Offset(14.dp.toPx(), 0f), Offset(14.dp.toPx(), size.height), 1.dp.toPx())
        }) { content() }
    }
}
