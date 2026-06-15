package com.example.spesafacile.widget

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.action.actionStartActivity as glanceActionStartActivity
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.appwidget.cornerRadius
import androidx.glance.color.ColorProvider
import com.example.spesafacile.MainActivity
import com.example.spesafacile.data.AppDatabase
import com.example.spesafacile.data.model.ShoppingItem
import com.example.spesafacile.data.model.ShoppingList
import kotlinx.coroutines.flow.first

class SpesaWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val db = AppDatabase.getDatabase(context)
        val lists = db.shoppingListDao().getAllLists().first()
        val firstList = lists.firstOrNull()
        val items = if (firstList != null) {
            db.shoppingItemDao().getItemsByList(firstList.id).first()
                .filter { !it.isChecked }
                .take(8)
        } else {
            emptyList()
        }

        provideContent {
            SpesaWidgetContent(
                listName = firstList?.name ?: "SpesaFacile",
                items = items,
                listId = firstList?.id
            )
        }
    }

    @Composable
    private fun SpesaWidgetContent(
        listName: String,
        items: List<ShoppingItem>,
        listId: Int?
    ) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(12.dp)
                .cornerRadius(16.dp)
                .background(GlanceTheme.colors.surface),
        ) {
            // Header
            Row(
                modifier = GlanceModifier.fillMaxWidth().padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "🛒 $listName",
                    style = TextStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = GlanceTheme.colors.onSurface
                    ),
                    modifier = GlanceModifier.defaultWeight()
                )
                // Add button
                Box(
                    modifier = GlanceModifier
                        .size(32.dp)
                        .cornerRadius(16.dp)
                        .background(GlanceTheme.colors.primary)
                        .clickable(
                            actionStartActivity<MainActivity>()
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "+",
                        style = TextStyle(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = GlanceTheme.colors.onPrimary
                        )
                    )
                }
            }

            // Divider
            Box(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(GlanceTheme.colors.outline)
            ) {}

            Spacer(modifier = GlanceModifier.height(4.dp))

            if (items.isEmpty()) {
                // Empty state
                Box(
                    modifier = GlanceModifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "✅ Tutto acquistato!",
                        style = TextStyle(
                            fontSize = 14.sp,
                            color = GlanceTheme.colors.onSurfaceVariant
                        )
                    )
                }
            } else {
                // Items list
                LazyColumn(modifier = GlanceModifier.fillMaxSize()) {
                    items(items) { item ->
                        WidgetItemRow(item)
                    }
                }
            }
        }
    }

    @Composable
    private fun WidgetItemRow(item: ShoppingItem) {
        Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .padding(vertical = 4.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "○",
                style = TextStyle(
                    fontSize = 16.sp,
                    color = GlanceTheme.colors.primary
                ),
                modifier = GlanceModifier.padding(end = 8.dp)
            )
            Text(
                text = item.name,
                style = TextStyle(
                    fontSize = 14.sp,
                    color = GlanceTheme.colors.onSurface
                ),
                modifier = GlanceModifier.defaultWeight()
            )
            if (item.quantity > 1 || item.unit.isNotEmpty()) {
                Text(
                    text = "×${item.quantity}${if (item.unit.isNotEmpty()) " ${item.unit}" else ""}",
                    style = TextStyle(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = GlanceTheme.colors.primary
                    )
                )
            }
        }
    }
}
