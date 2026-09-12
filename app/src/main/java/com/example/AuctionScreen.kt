package com.example

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.util.*

data class Bid(val buyerName: String, val amount: Double)

data class Auction(
    val id: String,
    val cropName: String,
    val quantity: String,
    val quality: String,
    val location: String,
    val startingPrice: Double,
    val highestBid: Double,
    val bidsCount: Int,
    val endsIn: String,
    val status: String,
    val bidHistory: List<Bid> = emptyList()
)

val mockAuctions = listOf(
    Auction("1", "Tomato", "500 kg", "Grade A", "Nashik, MH", 2000.0, 2450.0, 12, "02h 35m", "Active", listOf(
        Bid("Buyer A", 2200.0),
        Bid("Buyer B", 2300.0),
        Bid("Buyer C", 2450.0)
    )),
    Auction("2", "Onion", "1000 kg", "Grade A", "Pune, MH", 1500.0, 1500.0, 0, "05h 10m", "Active"),
    Auction("3", "Potato", "800 kg", "Grade B", "Nashik, MH", 1200.0, 1350.0, 5, "00h 00m", "Completed", listOf(
        Bid("Buyer X", 1250.0),
        Bid("Buyer Y", 1300.0),
        Bid("Buyer Z", 1350.0)
    ))
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuctionScreen(
    onNavigateBack: () -> Unit,
    onNavigateToStartAuction: () -> Unit,
    onNavigateToAuctionDetail: (String) -> Unit
) {
    val t = LocalAppTranslations.current
    var selectedTab by remember { mutableStateOf(0) } // 0: Active, 1: Completed

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(t.navAuction, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToStartAuction,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text(t.startNewAuctionLabel) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Stats summary
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatCard(t.activeLabel, "2", MaterialTheme.colorScheme.primary, Modifier.weight(1f))
                Spacer(modifier = Modifier.width(16.dp))
                StatCard(t.completedLabel, "8", MaterialTheme.colorScheme.secondary, Modifier.weight(1f))
            }
            
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.background,
                modifier = Modifier.padding(horizontal = 24.dp)
            ) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                    Text(t.activeLabel, modifier = Modifier.padding(vertical = 12.dp), fontWeight = if(selectedTab == 0) FontWeight.Bold else FontWeight.Normal)
                }
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                    Text(t.completedLabel, modifier = Modifier.padding(vertical = 12.dp), fontWeight = if(selectedTab == 1) FontWeight.Bold else FontWeight.Normal)
                }
            }

            LazyColumn(
                contentPadding = PaddingValues(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val filtered = mockAuctions.filter { if (selectedTab == 0) it.status == "Active" else it.status == "Completed" }
                items(filtered) { auction ->
                    AuctionCard(t, auction) {
                        onNavigateToAuctionDetail(auction.id)
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = color)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
fun AuctionCard(t: AppTranslations, auction: Auction, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(auction.cropName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                if (auction.status == "Active") {
                    Badge(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.error) {
                        Text("${t.endsInLabel} ${auction.endsIn}", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                } else {
                    Badge(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.secondary) {
                        Text(t.completedLabel, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Scale, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.width(4.dp))
                Text("${auction.quantity} • ${auction.quality}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(t.minExpectedPriceLabel, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("₹${auction.startingPrice}", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(t.highestBidLabel, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("₹${auction.highestBid}", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StartAuctionScreen(onNavigateBack: () -> Unit) {
    val t = LocalAppTranslations.current
    var cropName by remember { mutableStateOf("") }
    var cropVariety by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("") }
    var cropQuality by remember { mutableStateOf("") }
    var harvestDate by remember { mutableStateOf("") }
    var minPrice by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var additionalDetails by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(t.startNewAuctionLabel, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        bottomBar = {
            Surface(shadowElevation = 8.dp) {
                Button(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(t.startNewAuctionLabel, fontSize = MaterialTheme.typography.titleMedium.fontSize)
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(24.dp)
        ) {
            OutlinedTextField(value = cropName, onValueChange = { cropName = it }, label = { Text(t.cropNameLabel) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(value = cropVariety, onValueChange = { cropVariety = it }, label = { Text(t.cropVarietyLabel) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(value = quantity, onValueChange = { quantity = it }, label = { Text(t.quantityLabel) }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp))
                OutlinedTextField(value = unit, onValueChange = { unit = it }, label = { Text(t.unitLabel) }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp))
            }
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(value = cropQuality, onValueChange = { cropQuality = it }, label = { Text(t.cropQualityLabel) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(value = harvestDate, onValueChange = { harvestDate = it }, label = { Text(t.harvestDateLabel) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(value = minPrice, onValueChange = { minPrice = it }, label = { Text(t.minExpectedPriceLabel) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(value = startDate, onValueChange = { startDate = it }, label = { Text(t.auctionStartLabel) }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp))
                OutlinedTextField(value = endDate, onValueChange = { endDate = it }, label = { Text(t.auctionEndLabel) }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp))
            }
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text(t.locationLabel) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(value = additionalDetails, onValueChange = { additionalDetails = it }, label = { Text(t.additionalDetailsLabel) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), minLines = 3)
            Spacer(modifier = Modifier.height(16.dp))
            
            // Image upload mock
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                    .clickable { },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Outlined.CloudUpload, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(t.uploadImageLabel, color = MaterialTheme.colorScheme.primary)
                }
            }
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuctionDetailScreen(auctionId: String, onNavigateBack: () -> Unit) {
    val t = LocalAppTranslations.current
    val auction = mockAuctions.find { it.id == auctionId } ?: mockAuctions.first()
    val scrollState = rememberScrollState()
    
    var bidAmount by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(t.auctionDetailsTitle, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(24.dp)
        ) {
            Text(auction.cropName, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(auction.location, style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.width(16.dp))
                Icon(Icons.Outlined.Scale, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(auction.quantity, style = MaterialTheme.typography.bodyMedium)
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    if (auction.status == "Active") {
                        Text(t.highestCurrentBidLabel, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("₹${auction.highestBid}", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text(t.minExpectedPriceLabel, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("₹${auction.startingPrice}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(t.endsInLabel, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(auction.endsIn, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.error)
                            }
                        }
                    } else {
                        Text(t.auctionCompletedLabel, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
                        Text("₹${auction.highestBid}", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("${t.winningBuyerLabel}: ${auction.bidHistory.lastOrNull()?.buyerName ?: "N/A"}", style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            Text(t.bidHistoryLabel, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            
            if (auction.bidHistory.isEmpty()) {
                Text("No bids yet.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                auction.bidHistory.reversed().forEachIndexed { index, bid ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Outlined.Person, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(bid.buyerName, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                        }
                        Text("₹${bid.amount}", style = MaterialTheme.typography.titleMedium, fontWeight = if(index == 0) FontWeight.Bold else FontWeight.Normal, color = if(index == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
                    }
                    if (index < auction.bidHistory.size - 1) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    }
                }
            }
            
            if (auction.status == "Active") {
                Spacer(modifier = Modifier.height(32.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = bidAmount,
                        onValueChange = { bidAmount = it },
                        label = { Text("Enter amount") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Button(
                        onClick = { /* Place Bid mock */ },
                        modifier = Modifier.height(56.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(t.placeBidBtn)
                    }
                }
            }
        }
    }
}
