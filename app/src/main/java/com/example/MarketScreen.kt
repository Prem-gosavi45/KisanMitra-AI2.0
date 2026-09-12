package com.example

import android.Manifest
import android.os.Build
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.LocalOffer
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.abs
import com.example.ui.theme.*

@Composable
fun MarketScreen(onBack: () -> Unit, onRecommendation: (String) -> Unit, onNavigateToAuction: () -> Unit) {
    val t = LocalAppTranslations.current
    val context = LocalContext.current
    val priceAlertManager = remember { PriceAlertManager(context) }
    val repository = remember { MockMarketRepository() }
    val decisionEngine = remember { DecisionEngine() }
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    
    var selectedCropKey by remember { mutableStateOf("tomato") }
    var selectedTimeKey by remember { mutableStateOf("today") }
    
    var marketData by remember { mutableStateOf<MarketData?>(null) }
    var chartData by remember { mutableStateOf<List<MarketPrice>>(emptyList()) }
    var regionalMarkets by remember { mutableStateOf<List<RegionalMarket>>(emptyList()) }
    
    var recommendation by remember { mutableStateOf<RecommendationResult?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Notifications permission is required for price alerts.")
            }
        }
    }

    fun loadData() {
        isLoading = true
        coroutineScope.launch {
            val cropQuery = when(selectedCropKey) {
                "potato" -> "Potato"
                "onion" -> "Onion"
                else -> "Tomato"
            }
            val data = repository.getMarketData(cropQuery)
            marketData = data
            
            regionalMarkets = repository.getRegionalMarkets(cropQuery)
            
            chartData = when(selectedTimeKey) {
                "yesterday" -> repository.getYesterdayPrices(cropQuery)
                "tomorrow" -> repository.getTomorrowForecast(cropQuery)
                "7days" -> data.sevenDayForecast
                else -> repository.getTodayPrices(cropQuery)
            }
                
            val breakEven = 16.0
            val shelfLife = if (selectedCropKey == "tomato") 3 else 10
                
            recommendation = decisionEngine.getSellingRecommendation(
                currentPrice = data.todayPrice,
                forecastPrice = data.tomorrowForecast,
                breakEvenPrice = breakEven,
                remainingShelfLifeDays = shelfLife,
                bestBid = data.todayPrice + 0.3,
                forecastConfidence = data.confidence,
                t = t
            )
            isLoading = false
        }
    }
    
    LaunchedEffect(selectedCropKey, selectedTimeKey) {
        loadData()
    }
    
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(t.marketPriceTitle, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }
                IconButton(onClick = { loadData() }) {
                    Icon(Icons.Filled.Refresh, contentDescription = "Refresh", tint = MaterialTheme.colorScheme.onSurface)
                }
            }
        },
        bottomBar = { MarketBottomNav(t, onBack, onNavigateToAuction) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Crop Selector
            CropSelector(t, selectedCropKey) { selectedCropKey = it }
            
            if (isLoading) {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else {
                marketData?.let { data ->
                    // Location & Current Price Card
                    LocationPriceCard(t, data)
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Price Alerts
                    PriceAlertCard(t = t, currentPrice = data.todayPrice) { enabled, target ->
                        coroutineScope.launch {
                            if (enabled) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    }
                                }
                                snackbarHostState.showSnackbar("Alert set for ₹${target.toInt()}/kg. You will be notified!")
                                priceAlertManager.simulatePriceHit(data.crop, target)
                            } else {
                                snackbarHostState.showSnackbar("Price alerts disabled")
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Chart Card
                    ChartCard(t, selectedTimeKey, { selectedTimeKey = it }, chartData)
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Three-day summary
                    ThreeDaySummary(t, data)
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Regional Market Comparison
                    if (regionalMarkets.isNotEmpty()) {
                        RegionalComparisonCard(regionalMarkets)
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    
                    // Insight Card
                    recommendation?.let { rec ->
                        InsightCard(t, rec)
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Button(
                            onClick = { 
                                val cropName = when(selectedCropKey) {
                                    "potato" -> "Potato"
                                    "onion" -> "Onion"
                                    else -> "Tomato"
                                }
                                onRecommendation(cropName) 
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp)
                                .defaultMinSize(minHeight = 56.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(28.dp)
                        ) {
                            Text("${t.viewSellingRecommendation} →", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary, fontSize = 16.sp)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun CropSelector(t: AppTranslations, selectedCropKey: String, onSelected: (String) -> Unit) {
    val crops = listOf(
        "tomato" to "🍅 ${t.tomato}",
        "potato" to "🥔 ${t.potato}",
        "onion" to "🧅 ${t.onion}"
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        crops.forEach { (key, label) ->
            val isSelected = selectedCropKey == key
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (isSelected) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { onSelected(key) }
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Text(
                    text = label,
                    color = if (isSelected) MaterialTheme.colorScheme.onTertiary else MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
            }
        }
    }
}

@Composable
fun LocationPriceCard(t: AppTranslations, marketData: MarketData) {
    val today = marketData.todayPrice
    val pctChange = abs(marketData.percentageChange)
    val isUp = marketData.isUp
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Column {
                    Text(marketData.marketName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(marketData.updatedAt, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(verticalAlignment = Alignment.Bottom) {
                Text(String.format(Locale.US, "₹%.2f", today), style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(4.dp))
                Text("/ kg", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 6.dp))
                
                Spacer(modifier = Modifier.weight(1f))
                
                Box(
                    modifier = Modifier
                        .background(if (isUp) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(16.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = String.format(Locale.US, "%s %.1f%% %s", if (isUp) "↑" else "↓", pctChange, t.vsYesterday),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isUp) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
}

@Composable
fun PriceAlertCard(t: AppTranslations, currentPrice: Double, onAlertChanged: (Boolean, Double) -> Unit) {
    var isAlertEnabled by remember { mutableStateOf(false) }
    var targetPrice by remember { mutableStateOf(currentPrice + 5.0) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.NotificationsActive,
                            contentDescription = t.watchPrice,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(t.watchPrice, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Text("Notify when target reached", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Switch(
                    checked = isAlertEnabled,
                    onCheckedChange = { 
                        isAlertEnabled = it
                        onAlertChanged(it, targetPrice)
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.surface,
                        checkedTrackColor = MaterialTheme.colorScheme.primary,
                        uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                        uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            }

            if (isAlertEnabled) {
                Spacer(modifier = Modifier.height(20.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(20.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(t.expectedPrice, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                    Text(
                        text = String.format(Locale.US, "₹%.0f / kg", targetPrice),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Slider(
                    value = targetPrice.toFloat(),
                    onValueChange = { targetPrice = it.toDouble() },
                    onValueChangeFinished = {
                        onAlertChanged(true, targetPrice)
                    },
                    valueRange = currentPrice.toFloat()..(currentPrice.toFloat() + 50f),
                    steps = 49, // Creates 1-rupee intervals up to +50
                    modifier = Modifier.fillMaxWidth(),
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary,
                        inactiveTrackColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }
        }
    }
}

@Composable
fun ChartCard(
    t: AppTranslations, 
    selectedTimeKey: String, 
    onTimeSelected: (String) -> Unit, 
    data: List<MarketPrice>
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(vertical = 24.dp)) {
            val times = listOf(
                "yesterday" to t.yesterday, 
                "today" to t.today, 
                "tomorrow" to t.tomorrow,
                "7days" to "7 Days"
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(20.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                times.forEach { (key, label) ->
                    val isSelected = key == selectedTimeKey
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) MaterialTheme.colorScheme.tertiary else Color.Transparent)
                            .clickable { onTimeSelected(key) }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            label, 
                            color = if (isSelected) MaterialTheme.colorScheme.onTertiary else MaterialTheme.colorScheme.onSurface, 
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, 
                            fontSize = 14.sp
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Recharts-style Bar Chart
            Box(modifier = Modifier.fillMaxWidth().height(220.dp).padding(horizontal = 24.dp), contentAlignment = Alignment.Center) {
                if (data.isNotEmpty()) {
                    val maxPrice = data.maxOf { it.pricePerKg }
                    val yMax = maxPrice * 1.25
                       
                    val tertiaryColor = MaterialTheme.colorScheme.tertiary
                    val primaryColor = MaterialTheme.colorScheme.primary
                    val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)

                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val width = size.width
                        val height = size.height
                           
                        // Draw minimal horizontal grid lines
                        val steps = 4
                        for (i in 0..steps) {
                            val y = height - (i * (height / steps))
                            drawLine(
                                color = gridColor, 
                                start = Offset(0f, y), 
                                end = Offset(width, y), 
                                strokeWidth = 1.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                            )
                        }
                           
                        val sectionWidth = width / data.size
                        val barWidth = sectionWidth * 0.45f
                           
                        data.forEachIndexed { index, point ->
                            val barHeight = ((point.pricePerKg / yMax).toFloat() * height)
                            val xCenter = (index * sectionWidth) + (sectionWidth / 2f)
                            val x = xCenter - (barWidth / 2f)
                            val y = height - barHeight
                               
                            val barColor = if (point.isForecast) tertiaryColor else primaryColor
                            val alpha = if (point.isForecast) 0.7f else 1.0f
                               
                            drawRoundRect(
                                color = barColor,
                                topLeft = Offset(x, y),
                                size = androidx.compose.ui.geometry.Size(barWidth, barHeight),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(16f, 16f),
                                alpha = alpha
                            )
                        }
                    }
                       
                    // Price labels above bars
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        data.forEach { point ->
                            BoxWithConstraints(
                                modifier = Modifier.weight(1f).fillMaxHeight(),
                                contentAlignment = Alignment.BottomCenter
                            ) {
                                val barHeightDp = maxHeight * (point.pricePerKg / yMax).toFloat()
                                val priceFormat = if (selectedTimeKey == "7days") "₹%.0f" else "₹%.2f"
                                val priceFontSize = if (selectedTimeKey == "7days") 9.sp else 11.sp
                                Text(
                                    text = String.format(Locale.US, priceFormat, point.pricePerKg),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = priceFontSize,
                                    modifier = Modifier.offset(y = -(barHeightDp + 6.dp))
                                )
                            }
                        }
                    }

                    // X-axis labels
                    Row(
                        modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter).offset(y = 24.dp), 
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        data.forEach { point ->
                            val labelText = if (selectedTimeKey == "7days") point.date else point.time
                            val labelFontSize = if (selectedTimeKey == "7days") 9.sp else 11.sp
                            Text(
                                labelText, 
                                style = MaterialTheme.typography.labelSmall, 
                                color = MaterialTheme.colorScheme.onSurfaceVariant, 
                                fontSize = labelFontSize,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                } else {
                    Text("No market data available right now.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
               
            Spacer(modifier = Modifier.height(48.dp))
            
            // Legend
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(10.dp).background(MaterialTheme.colorScheme.primary, CircleShape))
                Spacer(modifier = Modifier.width(6.dp))
                Text(t.marketDataLabel, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                
                Spacer(modifier = Modifier.width(24.dp))
                
                Box(modifier = Modifier.size(10.dp).background(MaterialTheme.colorScheme.tertiary, CircleShape))
                Spacer(modifier = Modifier.width(6.dp))
                Text(t.aiForecastLabel, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun ThreeDaySummary(t: AppTranslations, marketData: MarketData) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(24.dp))
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(t.yesterday, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(String.format(Locale.US, "₹%.2f/kg", marketData.yesterdayPrice), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        }
        
        HorizontalDivider(modifier = Modifier.height(32.dp).width(1.dp), color = MaterialTheme.colorScheme.outlineVariant)
        
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(t.today, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(String.format(Locale.US, "₹%.2f/kg", marketData.todayPrice), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        }
        
        HorizontalDivider(modifier = Modifier.height(32.dp).width(1.dp), color = MaterialTheme.colorScheme.outlineVariant)
        
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(t.tomorrow, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(String.format(Locale.US, "~₹%.2f/kg", marketData.tomorrowForecast), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
fun InsightCard(t: AppTranslations, recommendation: RecommendationResult) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🤖", fontSize = 24.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(t.kisanMitraInsight, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            Text(recommendation.reason, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Info, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "${t.recommendationLabel}: ${recommendation.action}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.tertiary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}

@Composable
fun MarketBottomNav(t: AppTranslations, onHome: () -> Unit, onNavigateToAuction: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        shadowElevation = 16.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars)
                .defaultMinSize(minHeight = 80.dp)
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavItem(t.navHome, rememberVectorPainter(Icons.Outlined.Home), false) { onHome() }
            BottomNavItem(t.navMarket, rememberVectorPainter(Icons.Outlined.ShoppingCart), true) { }
            BottomNavItem(t.navAuction, rememberVectorPainter(Icons.Outlined.LocalOffer), false) { onNavigateToAuction() }
            BottomNavItem(t.navFinances, rememberVectorPainter(Icons.Outlined.Info), false) { }
            BottomNavItem(t.navProfile, rememberVectorPainter(Icons.Outlined.Person), false) { }
        }
    }
}

@Composable
fun RegionalComparisonCard(markets: List<RegionalMarket>) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Regional Markets", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            
            // Header
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Market", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Price/kg", modifier = Modifier.weight(0.5f), textAlign = TextAlign.End, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Trend", modifier = Modifier.weight(0.5f), textAlign = TextAlign.End, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)
            
            // Rows
            markets.forEach { market ->
                val isLocal = market.distanceKm < 30
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = market.marketName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isLocal) FontWeight.Bold else FontWeight.Normal,
                            color = if (isLocal) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${market.distanceKm} km away",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Text(
                        text = String.format(Locale.US, "₹%.2f", market.pricePerKg),
                        modifier = Modifier.weight(0.5f),
                        textAlign = TextAlign.End,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = market.formattedTrend,
                        modifier = Modifier.weight(0.5f),
                        textAlign = TextAlign.End,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (market.isUp) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
                if (market != markets.last()) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), thickness = 0.5.dp)
                }
            }
        }
    }
}
