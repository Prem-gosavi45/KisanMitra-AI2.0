import re

with open("app/src/main/java/com/example/FarmerDashboardScreen.kt", "r") as f:
    content = f.read()

# Add Dashboard model
dashboard_model = """
data class FarmerDashboardState(
    val cropRisk: Int = 72,
    val cropHealth: String = "Excellent",
    val currentPrice: Int = 21,
    val priceChange: Int = 12,
    val forecastPrice: Int = 15,
    val forecastConfidence: Int = 78,
    val expectedProfit: Int = 4300,
    val breakEvenPrice: Int = 16,
    val remainingShelfLife: Int = 4
)
"""

if "FarmerDashboardState" not in content:
    content = content.replace("package com.example\n", "package com.example\n" + dashboard_model)

# Add explicit colors
colors_def = """
val TextPrimary = Color(0xFF111111)
val TextSecondary = Color(0xFF6F756F)
val BrandGreen = Color(0xFF1B9C73)
val BrandWhite = Color(0xFFFFFFFF)
"""

if "val TextPrimary" not in content:
    content = content.replace("@OptIn(ExperimentalMaterial3Api::class)", colors_def + "\n@OptIn(ExperimentalMaterial3Api::class)")

# Pass state to cards
content = content.replace("fun ThreeStatCardsRow(t: AppTranslations)", "fun ThreeStatCardsRow(t: AppTranslations, state: FarmerDashboardState = FarmerDashboardState())")
content = content.replace("fun SellingOpportunityCard(t: AppTranslations)", "fun SellingOpportunityCard(t: AppTranslations, state: FarmerDashboardState = FarmerDashboardState())")
content = content.replace("fun ProfitOutlookCard(t: AppTranslations)", "fun ProfitOutlookCard(t: AppTranslations, state: FarmerDashboardState = FarmerDashboardState())")

content = content.replace("ThreeStatCardsRow(t)", "ThreeStatCardsRow(t)")
content = content.replace("SellingOpportunityCard(t)", "SellingOpportunityCard(t)")
content = content.replace("ProfitOutlookCard(t)", "ProfitOutlookCard(t)")

# Replace text colors in ThreeStatCardsRow
card1_old = """                Text("72%", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
                Text(t.optimal, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimary)
                Spacer(modifier = Modifier.height(12.dp))
                Text(t.spoilageRiskLabel.uppercase(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.surface.copy(alpha=0.7f), fontSize = 9.sp, textAlign = TextAlign.Center)"""
card1_new = """                Text("${state.cropRisk}%", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = BrandWhite)
                Text(t.optimal, style = MaterialTheme.typography.bodyMedium, color = BrandWhite)
                Spacer(modifier = Modifier.height(12.dp))
                Text(t.spoilageRiskLabel.uppercase(), style = MaterialTheme.typography.labelSmall, color = BrandWhite.copy(alpha=0.8f), fontSize = 9.sp, textAlign = TextAlign.Center)"""
content = content.replace(card1_old, card1_new)
content = content.replace("MaterialTheme.colorScheme.primary, RoundedCornerShape(20.dp)", "BrandGreen, RoundedCornerShape(20.dp)")

card2_old = """                Icon(Icons.Outlined.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp)) // Leaf stand-in
                Spacer(modifier = Modifier.height(8.dp))
                Text(t.excellent, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.height(4.dp))
                Text(t.leafCropHealthLabel.uppercase(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 9.sp, textAlign = TextAlign.Center)"""
card2_new = """                Icon(Icons.Outlined.Eco, contentDescription = null, tint = BrandGreen, modifier = Modifier.size(28.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text(state.cropHealth, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                Spacer(modifier = Modifier.height(4.dp))
                Text(t.leafCropHealthLabel.uppercase(), style = MaterialTheme.typography.labelSmall, color = TextSecondary, fontSize = 9.sp, textAlign = TextAlign.Center)"""
content = content.replace(card2_old, card2_new)
content = content.replace("MaterialTheme.colorScheme.surface, RoundedCornerShape(20.dp)", "BrandWhite, RoundedCornerShape(20.dp)")

card3_old = """                Text("₹21", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text("/kg", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha=0.8f))
                Spacer(modifier = Modifier.height(12.dp))
                Box(modifier = Modifier.background(MaterialTheme.colorScheme.onPrimary.copy(alpha=0.3f), RoundedCornerShape(10.dp)).padding(horizontal = 8.dp, vertical = 2.dp)) {
                    Text("↗ +12%", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                }"""
card3_new = """                Text("₹${state.currentPrice}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text("/kg", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                Spacer(modifier = Modifier.height(12.dp))
                Box(modifier = Modifier.background(Color(0xFFEAF5EF), RoundedCornerShape(10.dp)).padding(horizontal = 8.dp, vertical = 2.dp)) {
                    Text("↗ +${state.priceChange}%", style = MaterialTheme.typography.labelSmall, color = BrandGreen, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                }"""
content = content.replace(card3_old, card3_new)
content = content.replace("MaterialTheme.colorScheme.tertiary, RoundedCornerShape(20.dp)", "BrandWhite, RoundedCornerShape(20.dp)")

# Update SellingOpportunityCard
selling_opp_old = """                    Text(t.sellingOppTitle, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(t.basedOnMarketDesc, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("₹15/kg", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Text(t.expectedInDays, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(10.dp).background(MaterialTheme.colorScheme.primary, CircleShape))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(t.expectedPrice, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }"""
selling_opp_new = """                    Text(t.sellingOppTitle, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(t.basedOnMarketDesc, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("₹${state.forecastPrice}/kg", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text(t.expectedInDays, style = MaterialTheme.typography.titleMedium, color = TextSecondary)
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(10.dp).background(BrandGreen, CircleShape))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(t.expectedPrice, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                    }"""
content = content.replace(selling_opp_old, selling_opp_new)
content = content.replace("color = tertiaryColor", "color = BrandGreen")
content = content.replace("""                        Text("78%", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Text(t.forecastConfidence, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 9.sp)""", 
                        """                        Text("${state.forecastConfidence}%", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text(t.forecastConfidence, style = MaterialTheme.typography.labelSmall, color = TextSecondary, fontSize = 9.sp)""")

# Update ProfitOutlookCard
profit_outlook_old = """            Text(t.profitOutlookTitle, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(4.dp))
            Text(t.basedOnCostsDesc, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            
            Spacer(modifier = Modifier.height(24.dp))
            Text("₹4,300", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text(t.estProfitLabel, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            
            Spacer(modifier = Modifier.height(8.dp))
            Text("${t.breakEven} ₹16/kg", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)"""
profit_outlook_new = """            Text(t.profitOutlookTitle, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TextPrimary)
            Spacer(modifier = Modifier.height(4.dp))
            Text(t.basedOnCostsDesc, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
            
            Spacer(modifier = Modifier.height(24.dp))
            Text("₹${state.expectedProfit}", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text(t.estProfitLabel, style = MaterialTheme.typography.titleMedium, color = TextSecondary)
            
            Spacer(modifier = Modifier.height(8.dp))
            Text("${t.breakEven} ₹${state.breakEvenPrice}/kg", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)"""
content = content.replace(profit_outlook_old, profit_outlook_new)

# Add padding to scrollable Column for BottomNav and FAB
content = content.replace("""            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
            ) {""", """            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(bottom = 120.dp) // Avoid hiding content under BottomNav & FAB
            ) {""")
            
with open("app/src/main/java/com/example/FarmerDashboardScreen.kt", "w") as f:
    f.write(content)

