package com.example

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.outlined.Eco
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.LocalOffer
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.ui.theme.*

data class FarmerDashboardState(
    val cropRisk: Int = 72,
    val cropHealth: String = "Excellent",
    val forecastPrice: Double = 17.0,
    val forecastConfidence: Int = 78,
    val expectedProfit: Int = 4300,
    val breakEvenPrice: Double = 16.0,
    val remainingShelfLife: Int = 4
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FarmerDashboardScreen(
    viewModel: AuthViewModel,
    onNavigateToMarket: () -> Unit,
    onNavigateToAuction: () -> Unit,
    onLogout: () -> Unit
) {
    val t = LocalAppTranslations.current

    var showSettings by remember { mutableStateOf(false) }
    var showChatbot by remember { mutableStateOf(false) }
    val context = androidx.compose.ui.platform.LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val appPrefs = remember { AppPreferences(context) }
    val currentLang by appPrefs.languageFlow.collectAsState(initial = "en")
    val isDarkModePref by appPrefs.isDarkModeFlow.collectAsState(initial = null)
    val isDarkMode = isDarkModePref ?: androidx.compose.foundation.isSystemInDarkTheme()
    
        if (showSettings) {
        SettingsDialog(
            t = t, 
            currentLang = currentLang,
            isDarkMode = isDarkMode,
            onThemeToggled = { dark ->
                coroutineScope.launch { appPrefs.saveIsDarkMode(dark) }
            }, 
            onLanguageSelected = { lang ->
                coroutineScope.launch { appPrefs.saveLanguage(lang) }
            }, 
            onDismiss = { showSettings = false }
        )
    }
    var selectedTabKey by remember { mutableStateOf("overview") }
    val scrollState = rememberScrollState()
    var isLoading by remember { mutableStateOf(true) }
    var isRefreshing by remember { mutableStateOf(false) }
    
    val marketRepository = remember { MockMarketRepository() }
    val weatherRepository = remember { MockWeatherRepository() }
    var marketData by remember { mutableStateOf<MarketData?>(null) }
    var weatherData by remember { mutableStateOf<WeatherData?>(null) }

if (showChatbot) {
        val currentPrice = marketData?.todayPrice ?: 0.0
        val tomorrowPrice = marketData?.tomorrowForecast ?: 0.0
        val marketName = marketData?.marketName ?: "Unknown"
        val contextData = """
            Crop: ${marketData?.crop ?: "Tomato"}
            Current Market: $marketName
            Current Price: ₹$currentPrice per kg
            Tomorrow's Forecasted Price: ₹$tomorrowPrice per kg
            Location: Nashik
        """.trimIndent()
        ChatbotOverlay(
            onDismiss = { showChatbot = false },
            contextData = contextData,
            language = currentLang
        )
    
    }

    
    LaunchedEffect(selectedTabKey) {
        val cropName = when(selectedTabKey) {
            "potato" -> "Potato"
            "onion" -> "Onion"
            else -> "Tomato"
        }
        marketData = marketRepository.getMarketData(cropName)
    }

    LaunchedEffect(Unit) {
        weatherData = weatherRepository.getWeatherForDistrict("Nashik District")
        delay(600)
        isLoading = false
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = { CustomBottomNav(t, onNavigateToMarket, onNavigateToAuction, onProfileClick = { showSettings = true }) },
        floatingActionButton = { AiFloatingButton(t, onClick = { showChatbot = true }) },
        floatingActionButtonPosition = FabPosition.End
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                coroutineScope.launch {
                    isRefreshing = true
                    isLoading = true
                    val cropName = when(selectedTabKey) {
                        "potato" -> "Potato"
                        "onion" -> "Onion"
                        else -> "Tomato"
                    }
                    marketData = marketRepository.getMarketData(cropName)
                    delay(500)
                    isLoading = false
                    isRefreshing = false
                }
            },
            modifier = Modifier.fillMaxSize().padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(bottom = 120.dp)
            ) {
                HeaderSection(t, onLogout)
                CropTabsSection(t, selectedTabKey) { selectedTabKey = it }
                
                AnimatedContent(
                    targetState = selectedTabKey,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(250)).togetherWith(
                            fadeOut(animationSpec = tween(250))
                        )
                    },
                    label = "farmer_tab_transition"
                ) { targetTabKey ->
                    if (targetTabKey == "overview") {
                        Column {
                            Spacer(modifier = Modifier.height(16.dp))
                            WeatherCard(weatherData = weatherData, modifier = Modifier.padding(horizontal = 24.dp))
                            Spacer(modifier = Modifier.height(24.dp))
                            SectionTitle(t.cropHealth, t.allCrops)
                            Spacer(modifier = Modifier.height(16.dp))
                            if (isLoading) {
                                ThreeStatCardsRowSkeleton()
                            } else {
                                ThreeStatCardsRow(t = t, marketData = marketData)
                            }
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            if (isLoading) {
                                SellingOpportunityCardSkeleton()
                            } else {
                                SellingOpportunityCard(t = t, marketData = marketData)
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            if (isLoading) {
                                ProfitOutlookCardSkeleton()
                            } else {
                                ProfitOutlookCard(t = t)
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            if (!isLoading) {
                                AuctionSummaryCard(t = t, onNavigateToAuction = onNavigateToAuction)
                            }

                            
                            Spacer(modifier = Modifier.height(40.dp))
                        }
                    } else {
                        val cropDisplayName = when(targetTabKey) {
                            "tomato" -> t.tomato
                            "onion" -> t.onion
                            "potato" -> t.potato
                            else -> t.addCrop
                        }
                        Box(modifier = Modifier.fillMaxWidth().height(360.dp), contentAlignment = Alignment.Center) {
                            Text("Content for $cropDisplayName", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HeaderSection(t: AppTranslations, onLogout: () -> Unit) {
    AuthenticatedDashboardHeader(
        title = "${t.goodMorning}, Ramesh!",
        location = "Nashik, MH",
        role = "farmer",
        onLogout = onLogout
    )
}

@Composable
fun CropTabsSection(t: AppTranslations, selectedTabKey: String, onTabSelected: (String) -> Unit) {
    val tabs = listOf(
        "overview" to t.overview,
        "tomato" to t.tomato,
        "onion" to t.onion,
        "potato" to t.potato,
        "addCrop" to t.addCrop
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        tabs.forEach { (key, label) ->
            val isSelected = key == selectedTabKey
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (isSelected) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { onTabSelected(key) }
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
fun SectionTitle(title: String, subtitle: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        Text(subtitle.uppercase(), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 1.sp)
    }
}

@Composable
fun ThreeStatCardsRow(
    t: AppTranslations,
    state: FarmerDashboardState = FarmerDashboardState(),
    marketData: MarketData? = null
) {
    var animateStarted by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        animateStarted = true
    }

    // Subtle Staggered Card Entry Animations
    val card1Alpha by animateFloatAsState(
        targetValue = if (animateStarted) 1f else 0f,
        animationSpec = tween(durationMillis = 400, delayMillis = 0, easing = FastOutSlowInEasing),
        label = "card1_alpha"
    )
    val card1OffsetY by animateFloatAsState(
        targetValue = if (animateStarted) 0f else 18f,
        animationSpec = tween(durationMillis = 400, delayMillis = 0, easing = FastOutSlowInEasing),
        label = "card1_offset"
    )

    val card2Alpha by animateFloatAsState(
        targetValue = if (animateStarted) 1f else 0f,
        animationSpec = tween(durationMillis = 400, delayMillis = 80, easing = FastOutSlowInEasing),
        label = "card2_alpha"
    )
    val card2OffsetY by animateFloatAsState(
        targetValue = if (animateStarted) 0f else 18f,
        animationSpec = tween(durationMillis = 400, delayMillis = 80, easing = FastOutSlowInEasing),
        label = "card2_offset"
    )

    val card3Alpha by animateFloatAsState(
        targetValue = if (animateStarted) 1f else 0f,
        animationSpec = tween(durationMillis = 400, delayMillis = 160, easing = FastOutSlowInEasing),
        label = "card3_alpha"
    )
    val card3OffsetY by animateFloatAsState(
        targetValue = if (animateStarted) 0f else 18f,
        animationSpec = tween(durationMillis = 400, delayMillis = 160, easing = FastOutSlowInEasing),
        label = "card3_offset"
    )

    // Animated count-up for Spoilage Risk
    val animatedRisk by animateIntAsState(
        targetValue = if (animateStarted) state.cropRisk else 0,
        animationSpec = tween(durationMillis = 500, delayMillis = 100, easing = FastOutSlowInEasing),
        label = "risk_count"
    )

    val todayPriceText = marketData?.formattedPrice ?: "₹15.20"
    val trendText = marketData?.formattedTrend ?: "↑ 16.9%"
    val isPriceUp = marketData?.isUp ?: true

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Card 1: Spoilage Risk (Green Card)
        Card(
            modifier = Modifier
                .weight(1f)
                .height(142.dp)
                .offset(y = card1OffsetY.dp)
                .alpha(card1Alpha),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Uniform Top Title
                Box(
                    modifier = Modifier.height(28.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = t.spoilageRiskLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        maxLines = 2
                    )
                }
                
                // Uniform Middle Value
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "$animatedRisk%",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 22.sp
                    )
                    Text(
                        text = t.optimal,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
                        fontSize = 10.sp
                    )
                }

                // Uniform Bottom Badge
                Box(
                    modifier = Modifier
                        .height(24.dp)
                        .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${state.remainingShelfLife} ${t.daysLeft}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Card 2: Leaf & Crop Health (White Card)
        Card(
            modifier = Modifier
                .weight(1f)
                .height(142.dp)
                .offset(y = card2OffsetY.dp)
                .alpha(card2Alpha),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Uniform Top Title
                Box(
                    modifier = Modifier.height(28.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = t.leafCropHealthLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        maxLines = 2
                    )
                }

                // Uniform Middle Value
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Eco,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = state.cropHealth,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 13.sp
                    )
                }

                // Uniform Bottom Badge
                Box(
                    modifier = Modifier
                        .height(24.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = t.safe,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Card 3: Today's Mandi Prices (White Card)
        Card(
            modifier = Modifier
                .weight(1f)
                .height(142.dp)
                .offset(y = card3OffsetY.dp)
                .alpha(card3Alpha),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Uniform Top Title
                Box(
                    modifier = Modifier.height(28.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = t.mandiPrices,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        maxLines = 2
                    )
                }

                // Uniform Middle Value
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.weight(1f)
                ) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = todayPriceText,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "/kg",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 1.dp),
                            fontSize = 10.sp
                        )
                    }
                }

                // Uniform Bottom Badge
                val badgeContainer = if (isPriceUp) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer
                val badgeContent = if (isPriceUp) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                Box(
                    modifier = Modifier
                        .height(24.dp)
                        .background(badgeContainer, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = trendText,
                        style = MaterialTheme.typography.labelSmall,
                        color = badgeContent,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp
                    )
                }
            }
        }
    }
}

@Composable
fun SellingOpportunityCard(
    t: AppTranslations,
    state: FarmerDashboardState = FarmerDashboardState(),
    marketData: MarketData? = null
) {
    var animateStarted by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        animateStarted = true
    }

    val forecastPrice = marketData?.tomorrowForecast ?: state.forecastPrice
    val confidence = marketData?.confidence ?: state.forecastConfidence

    // Subtle confidence ring progress animation
    val animatedSweepAngle by animateFloatAsState(
        targetValue = if (animateStarted) 360f * (confidence / 100f) else 0f,
        animationSpec = tween(durationMillis = 700, delayMillis = 200, easing = FastOutSlowInEasing),
        label = "confidence_sweep"
    )
    val animatedConfidence by animateIntAsState(
        targetValue = if (animateStarted) confidence else 0,
        animationSpec = tween(durationMillis = 700, delayMillis = 200, easing = FastOutSlowInEasing),
        label = "confidence_count"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(t.sellingOppTitle, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(t.basedOnMarketDesc, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(String.format(java.util.Locale.US, "₹%.2f/kg", forecastPrice), style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Text(t.expectedInDays, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(10.dp).background(MaterialTheme.colorScheme.primary, CircleShape))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(t.expectedPrice, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                
                val primaryColor = MaterialTheme.colorScheme.primary
                val trackColor = MaterialTheme.colorScheme.surfaceVariant
                Box(
                    modifier = Modifier.size(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawArc(
                            color = trackColor,
                            startAngle = 0f,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                        )
                        drawArc(
                            color = primaryColor,
                            startAngle = -90f,
                            sweepAngle = animatedSweepAngle,
                            useCenter = false,
                            style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("$animatedConfidence%", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Text(t.forecastConfidence, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 9.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun ProfitOutlookCard(t: AppTranslations, state: FarmerDashboardState = FarmerDashboardState()) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(t.profitOutlookTitle, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(4.dp))
            Text(t.basedOnCostsDesc, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            
            Spacer(modifier = Modifier.height(24.dp))
            Text("₹${state.expectedProfit}", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text(t.estProfitLabel, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            
            Spacer(modifier = Modifier.height(8.dp))
            Text(String.format(java.util.Locale.US, "%s ₹%.2f/kg", t.breakEven, state.breakEvenPrice), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun AiFloatingButton(t: AppTranslations, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .padding(bottom = 16.dp, end = 8.dp)
            .shadow(4.dp, RoundedCornerShape(28.dp))
            .background(MaterialTheme.colorScheme.tertiary, RoundedCornerShape(28.dp))
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_ai_assistant),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onTertiary,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(t.askKisanMitraBtn, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onTertiary)
    }
}

@Composable
fun CustomBottomNav(t: AppTranslations, onNavigateToMarket: () -> Unit, onNavigateToAuction: () -> Unit, onProfileClick: () -> Unit) {
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
            BottomNavItem(t.navHome, rememberVectorPainter(Icons.Filled.Home), true) {}
            BottomNavItem(t.navMarket, rememberVectorPainter(Icons.Outlined.ShoppingCart), false) { onNavigateToMarket() }
            BottomNavItem(t.navAuction, rememberVectorPainter(Icons.Outlined.LocalOffer), false) { onNavigateToAuction() }
            BottomNavItem(t.navFinances, rememberVectorPainter(Icons.Outlined.Info), false) {}
            BottomNavItem(t.navProfile, rememberVectorPainter(Icons.Outlined.Person), false) { onProfileClick() }
        }
    }
}

@Composable
fun BottomNavItem(label: String, painter: Painter, isSelected: Boolean, onClick: () -> Unit = {}) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .width(64.dp)
            .clickable { onClick() }
    ) {
        Icon(
            painter = painter, 
            contentDescription = label, 
            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, 
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label, 
            style = MaterialTheme.typography.labelSmall, 
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, 
            fontSize = 11.sp, 
            maxLines = 1
        )
        if (isSelected) {
            Spacer(modifier = Modifier.height(2.dp))
            Box(modifier = Modifier.size(4.dp).background(MaterialTheme.colorScheme.primary, CircleShape))
        }
    }
}

fun Modifier.shimmerEffect(): Modifier = composed {
    var size by remember { mutableStateOf(IntSize.Zero) }
    val transition = rememberInfiniteTransition(label = "shimmer_transition")
    val startOffsetX by transition.animateFloat(
        initialValue = -2 * size.width.toFloat(),
        targetValue = 2 * size.width.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1000)
        ),
        label = "shimmer_offset"
    )

    background(
        brush = Brush.linearGradient(
            colors = listOf(
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
            ),
            start = Offset(startOffsetX, 0f),
            end = Offset(startOffsetX + size.width.toFloat(), size.height.toFloat())
        )
    ).onGloballyPositioned { size = it.size }
}

@Composable
fun ThreeStatCardsRowSkeleton() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        for (i in 0..2) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(142.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .shimmerEffect()
            )
        }
    }
}

@Composable
fun SellingOpportunityCardSkeleton() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f)) {
                    Box(modifier = Modifier.size(120.dp, 24.dp).clip(RoundedCornerShape(4.dp)).shimmerEffect())
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(modifier = Modifier.size(180.dp, 16.dp).clip(RoundedCornerShape(4.dp)).shimmerEffect())
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    Box(modifier = Modifier.size(100.dp, 40.dp).clip(RoundedCornerShape(8.dp)).shimmerEffect())
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(modifier = Modifier.size(80.dp, 16.dp).clip(RoundedCornerShape(4.dp)).shimmerEffect())
                }
                
                Box(
                    modifier = Modifier.size(100.dp).clip(CircleShape).shimmerEffect()
                )
            }
        }
    }
}

@Composable
fun ProfitOutlookCardSkeleton() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Box(modifier = Modifier.size(140.dp, 24.dp).clip(RoundedCornerShape(4.dp)).shimmerEffect())
            Spacer(modifier = Modifier.height(8.dp))
            Box(modifier = Modifier.size(200.dp, 16.dp).clip(RoundedCornerShape(4.dp)).shimmerEffect())
            
            Spacer(modifier = Modifier.height(24.dp))
            Box(modifier = Modifier.size(120.dp, 40.dp).clip(RoundedCornerShape(8.dp)).shimmerEffect())
            Spacer(modifier = Modifier.height(8.dp))
            Box(modifier = Modifier.size(90.dp, 16.dp).clip(RoundedCornerShape(4.dp)).shimmerEffect())
            
            Spacer(modifier = Modifier.height(8.dp))
            Box(modifier = Modifier.size(110.dp, 16.dp).clip(RoundedCornerShape(4.dp)).shimmerEffect())
        }
    }
}

@Composable
fun SettingsDialog(
    t: AppTranslations,
    currentLang: String,
    isDarkMode: Boolean,
    onLanguageSelected: (String) -> Unit,
    onThemeToggled: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(t.settingsTitle, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Dark Mode", style = MaterialTheme.typography.bodyLarge)
                    Switch(
                        checked = isDarkMode,
                        onCheckedChange = onThemeToggled
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(t.languageSelection, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(8.dp))
                
                val languages = listOf("en" to "English", "hi" to "हिन्दी", "mr" to "मराठी", "kn" to "ಕನ್ನಡ")
                languages.forEach { (code, name) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onLanguageSelected(code) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = currentLang == code,
                            onClick = { onLanguageSelected(code) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(name, style = MaterialTheme.typography.bodyLarge)
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                    Text(t.close)
                }
            }
        }
    }
}

@Composable
fun AuctionSummaryCard(t: AppTranslations, onNavigateToAuction: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onNavigateToAuction),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(t.myAuctionsTitle, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Icon(Icons.Filled.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(t.activeLabel, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("2", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
                Column {
                    Text(t.completedLabel, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("8", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(t.highestCurrentBidLabel, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("₹2,450", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}
