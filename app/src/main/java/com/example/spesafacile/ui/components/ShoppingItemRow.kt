package com.example.spesafacile.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.spesafacile.data.model.Category
import com.example.spesafacile.data.model.ShoppingItem
import com.example.spesafacile.theme.CategoryAltro
import com.example.spesafacile.theme.CategoryBevande
import com.example.spesafacile.theme.CategoryCarne
import com.example.spesafacile.theme.CategoryCasa
import com.example.spesafacile.theme.CategoryFrutta
import com.example.spesafacile.theme.CategoryIgiene
import com.example.spesafacile.theme.CategoryLatticini
import com.example.spesafacile.theme.CategoryPanePasta
import com.example.spesafacile.theme.CategorySnack
import com.example.spesafacile.theme.CategorySurgelati

fun getCategoryColor(categoryName: String): Color {
    return when (categoryName) {
        Category.FRUTTA_VERDURA.name -> CategoryFrutta
        Category.LATTICINI.name -> CategoryLatticini
        Category.CARNE_PESCE.name -> CategoryCarne
        Category.PANE_PASTA.name -> CategoryPanePasta
        Category.BEVANDE.name -> CategoryBevande
        Category.SURGELATI.name -> CategorySurgelati
        Category.SNACK_DOLCI.name -> CategorySnack
        Category.IGIENE.name -> CategoryIgiene
        Category.CASA.name -> CategoryCasa
        else -> CategoryAltro
    }
}

fun getCategoryDisplayName(categoryName: String): String {
    return try {
        Category.valueOf(categoryName).displayName
    } catch (_: Exception) {
        categoryName
    }
}

@Composable
fun ShoppingItemRow(
    item: ShoppingItem,
    onToggle: (ShoppingItem) -> Unit,
    onDelete: (ShoppingItem) -> Unit,
    onQuantityChange: (ShoppingItem, Int) -> Unit
) {
    var showQuantityControls by remember { mutableStateOf(false) }

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onDelete(item)
                true
            } else {
                false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val alignment = Alignment.CenterEnd
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.errorContainer, RoundedCornerShape(12.dp))
                    .padding(horizontal = 20.dp),
                contentAlignment = alignment
            ) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = "Elimina",
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        },
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true
    ) {
        val rowAlpha by animateFloatAsState(
            targetValue = if (item.isChecked) 0.5f else 1f,
            label = "rowAlpha"
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .alpha(rowAlpha),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = item.isChecked,
                onCheckedChange = { onToggle(item) },
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary,
                    checkmarkColor = MaterialTheme.colorScheme.onPrimary
                )
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
            ) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodyLarge,
                    textDecoration = if (item.isChecked) TextDecoration.LineThrough else TextDecoration.None,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Chip categoria
                    val catColor = getCategoryColor(item.category)
                    Text(
                        text = getCategoryDisplayName(item.category),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        modifier = Modifier
                            .background(catColor, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )

                    // Badge quantità
                    val quantityText = buildString {
                        append("×${item.quantity}")
                        if (item.unit.isNotBlank()) append(" ${item.unit}")
                    }
                    Text(
                        text = quantityText,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant,
                                RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                            .then(
                                Modifier.padding(0.dp) // placeholder for clickable
                            )
                    )
                }
            }

            // Controlli quantità
            AnimatedVisibility(
                visible = showQuantityControls,
                enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(),
                exit = slideOutHorizontally(targetOffsetX = { it }) + fadeOut()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { onQuantityChange(item, item.quantity - 1) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Outlined.Remove,
                            contentDescription = "Diminuisci",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                        text = "${item.quantity}",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                    IconButton(
                        onClick = { onQuantityChange(item, item.quantity + 1) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Outlined.Add,
                            contentDescription = "Aumenta",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            if (!showQuantityControls) {
                IconButton(onClick = { showQuantityControls = true }) {
                    Text(
                        text = "×${item.quantity}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            } else {
                IconButton(onClick = { showQuantityControls = false }) {
                    Icon(
                        Icons.Outlined.Delete,
                        contentDescription = "Chiudi",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            IconButton(onClick = { onDelete(item) }) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = "Elimina",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
