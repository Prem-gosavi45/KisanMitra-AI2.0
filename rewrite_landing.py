import re

filepath = 'app/src/main/java/com/example/MainActivity.kt'
with open(filepath, 'r') as file:
    content = file.read()

# First replace LandingScreen
landing_replacement = """@Composable
fun LandingScreen(
    onGetStarted: () -> Unit,
    onHowItWorks: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Image at the top
        AsyncImage(
            model = "https://images.unsplash.com/photo-1592982537447-6f23f5b721e2?auto=format&fit=crop&q=80&w=1000",
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.65f)
        )
        // Gradient overlay
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.66f)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            MaterialTheme.colorScheme.background.copy(alpha = 0.6f),
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
        )
        
        // Logo at the top center
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 48.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Outlined.Eco, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("KisanMitra AI", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }

        // Bottom Content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomStart)
                .padding(horizontal = 24.dp, vertical = 40.dp)
        ) {
            Text(
                "CROP INTELLIGENCE",
                color = MaterialTheme.colorScheme.tertiary,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                "Know your crop.",
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-1).sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Track crop health, spoilage risk and remaining\nshelf life with AI-powered insights.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyLarge,
                lineHeight = 24.sp
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Page indicator
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.size(24.dp, 8.dp).background(MaterialTheme.colorScheme.primary, RoundedCornerShape(4.dp)))
                    Box(modifier = Modifier.size(8.dp).background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha=0.3f), CircleShape))
                    Box(modifier = Modifier.size(8.dp).background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha=0.3f), CircleShape))
                }
                
                // Next Button
                Button(
                    onClick = onGetStarted,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(24.dp),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                ) {
                    Text("Next", color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}"""

content = re.sub(r'@Composable\s*fun LandingScreen\(.*?\)\s*\{.*?\n\}\n(?=@|\n|$)', landing_replacement + "\n", content, flags=re.DOTALL)

with open(filepath, 'w') as file:
    file.write(content)

