package com.example

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.example.ui.theme.*

@Composable
fun RecommendationScreen(crop: String, onBack: () -> Unit) {
    val t = LocalAppTranslations.current
    val context = LocalContext.current
    val repository = remember { MockMarketRepository() }
    val decisionEngine = remember { DecisionEngine() }
    val coroutineScope = rememberCoroutineScope()
    
    var summaryData by remember { mutableStateOf<Map<String, Double>>(emptyMap()) }
    var recommendation by remember { mutableStateOf<RecommendationResult?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(crop) {
        coroutineScope.launch {
            summaryData = repository.getMarketSummary(crop)
            val todayPrice = summaryData["Today"] ?: 0.0
            val tomorrowPrice = summaryData["Tomorrow"] ?: 0.0
            val breakEven = 16.0
            val shelfLife = if (crop.equals("Tomato", ignoreCase = true) || crop == t.tomato) 3 else 10
            
            recommendation = decisionEngine.getSellingRecommendation(
                currentPrice = todayPrice,
                forecastPrice = tomorrowPrice,
                breakEvenPrice = breakEven,
                remainingShelfLifeDays = shelfLife,
                bestBid = todayPrice + 0.3,
                forecastConfidence = 78,
                t = t
            )
            isLoading = false
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(t.viewSellingRecommendation, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
        ) {
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else {
                recommendation?.let { rec ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(24.dp)) {
                            Text("Crop: $crop", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            RecommendationRow("Today:", String.format("₹%.2f/kg", summaryData["Today"] ?: 0.0))
                            RecommendationRow("Tomorrow expected:", String.format("~₹%.2f/kg", summaryData["Tomorrow"] ?: 0.0))
                            RecommendationRow("Farmer break-even:", "₹16.00/kg")
                            RecommendationRow("Remaining shelf life:", if (crop.equals("Tomato", ignoreCase = true) || crop == t.tomato) "3 days" else "10 days")
                            RecommendationRow("Current best buyer bid:", String.format("₹%.2f/kg", (summaryData["Today"] ?: 0.0) + 0.3))
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            val additionalReturn = rec.expectedAdditionalReturn
                            if (additionalReturn > 0) {
                                RecommendationRow("Expected better price:", String.format("₹%.2f/kg", summaryData["Tomorrow"] ?: 0.0))
                                RecommendationRow("Estimated additional return:", String.format("₹%.0f", additionalReturn), isHighlight = true)
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                            
                            Text(t.recommendationLabel, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(rec.action, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(rec.reason, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RecommendationRow(label: String, value: String, isHighlight: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = if (isHighlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}
