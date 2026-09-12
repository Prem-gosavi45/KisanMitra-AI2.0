package com.example

import kotlinx.coroutines.delay
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.tween
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.Agriculture
import androidx.compose.material.icons.outlined.Eco
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material.icons.outlined.Store
import androidx.compose.material.icons.outlined.Warehouse
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import kotlinx.coroutines.launch
import com.example.ui.theme.MyApplicationTheme

const val SplashRoute = "splash"
const val LandingRoute = "landing"
const val LanguageSelectionRoute = "language_selection"
const val RoleSelectionRoute = "role_selection"
const val HowItWorksRoute = "how_it_works"
const val AuthRoute = "auth/{role}"
fun createAuthRoute(role: String) = "auth/$role"
const val OTPRoute = "otp"
const val ProfileSetupRoute = "profile_setup"
const val FarmerDashboardRoute = "farmer_dashboard"
const val BuyerDashboardRoute = "buyer_dashboard"
const val ConsumerDashboardRoute = "consumer_dashboard"
const val TransportDashboardRoute = "transport_dashboard"
const val StorageDashboardRoute = "storage_dashboard"
const val AdminDashboardRoute = "admin_dashboard"
const val MarketRoute = "market"
const val AuctionRoute = "auction"
const val StartAuctionRoute = "start_auction"
const val AuctionDetailRoute = "auction_detail/{auctionId}"
const val RecommendationRoute = "recommendation/{crop}"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = androidx.compose.ui.platform.LocalContext.current
            val appPrefs = remember(context) { AppPreferences(context) }
            val isDarkModePref by appPrefs.isDarkModeFlow.collectAsState(initial = null)
            val systemTheme = androidx.compose.foundation.isSystemInDarkTheme()
            val isDarkTheme = isDarkModePref ?: systemTheme
            
            MyApplicationTheme(darkTheme = isDarkTheme) {
                KisanMitraApp()
            }
        }
    }
}

@Composable
fun KisanMitraApp() {
    val context = LocalContext.current
    val appPrefs = remember { AppPreferences(context) }
    val languageCode by appPrefs.languageFlow.collectAsState(initial = "en")
    val translations = translationsMap[languageCode] ?: translationsMap["en"]!!

    val authViewModel: AuthViewModel = viewModel()
    
    CompositionLocalProvider(LocalAppTranslations provides translations) {
        val navController = rememberNavController()
        
        NavHost(
            navController = navController,
            startDestination = SplashRoute,
            enterTransition = {
                fadeIn(animationSpec = tween(300)) + 
                slideInHorizontally(initialOffsetX = { 50 }, animationSpec = tween(300))
            },
            exitTransition = {
                fadeOut(animationSpec = tween(300))
            },
            popEnterTransition = {
                fadeIn(animationSpec = tween(300))
            },
            popExitTransition = {
                fadeOut(animationSpec = tween(300)) + 
                slideOutHorizontally(targetOffsetX = { 50 }, animationSpec = tween(300))
            }
        ) {
            composable(route = SplashRoute) {
                SplashScreen(
                    viewModel = authViewModel,
                    onNavigate = { route -> 
                        navController.navigate(route) {
                            popUpTo(SplashRoute) { inclusive = true }
                        }
                    }
                )
            }
            composable(route = LandingRoute) {
                LandingScreen(
                    onGetStarted = { navController.navigate(LanguageSelectionRoute) },
                    onHowItWorks = { navController.navigate(HowItWorksRoute) }
                )
            }
            composable(route = LanguageSelectionRoute) {
                val coroutineScope = rememberCoroutineScope()
                LanguageSelectionScreen(
                    currentLanguageCode = languageCode,
                    onLanguageSelected = { code -> 
                        coroutineScope.launch {
                            appPrefs.saveLanguage(code)
                        }
                    },
                    onContinue = { navController.navigate(RoleSelectionRoute) }
                )
            }
            composable(route = RoleSelectionRoute) {
                val coroutineScope = rememberCoroutineScope()
                RoleSelectionScreen(onContinue = { route -> 
                    coroutineScope.launch {
                        appPrefs.saveRole(route)
                        navController.navigate(createAuthRoute(route))
                    }
                })
            }
            composable(route = AuthRoute, arguments = listOf(navArgument("role") { type = NavType.StringType })) { backStackEntry ->
                val role = backStackEntry.arguments?.getString("role") ?: "farmer"
                AuthScreen(
                    role = role, 
                    viewModel = authViewModel,
                    onNavigate = { route -> 
                        navController.navigate(route) {
                            if (route != OTPRoute) {
                                popUpTo(AuthRoute) { inclusive = true }
                            }
                        }
                    }
                )
            }
            composable(route = OTPRoute) {
                OtpVerificationScreen(
                    viewModel = authViewModel,
                    onNavigate = { route -> 
                        navController.navigate(route) {
                            popUpTo(AuthRoute) { inclusive = true }
                        }
                    }
                )
            }
            composable(route = ProfileSetupRoute) {
                ProfileSetupScreen(
                    viewModel = authViewModel,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(ProfileSetupRoute) { inclusive = true }
                        }
                    }
                )
            }
            composable(route = HowItWorksRoute) {
                PlaceholderScreen("How It Works") { navController.popBackStack() }
            }
            composable(route = FarmerDashboardRoute) { 
                FarmerDashboardScreen(
                    viewModel = authViewModel, 
                    onNavigateToMarket = { navController.navigate(MarketRoute) },
                    onNavigateToAuction = { navController.navigate(AuctionRoute) },
                    onLogout = { navController.navigate(LandingRoute) { popUpTo(0) } }
                ) 
            }
            composable(route = MarketRoute) {
                MarketScreen(
                    onBack = { navController.popBackStack() },
                    onRecommendation = { crop -> navController.navigate("recommendation/$crop") },
                    onNavigateToAuction = { navController.navigate(AuctionRoute) }
                )
            }
            composable(
                route = RecommendationRoute,
                arguments = listOf(navArgument("crop") { type = NavType.StringType })
            ) { backStackEntry ->
                val crop = backStackEntry.arguments?.getString("crop") ?: "Tomato"
                RecommendationScreen(
                    crop = crop,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(route = AuctionRoute) {
                AuctionScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToStartAuction = { navController.navigate(StartAuctionRoute) },
                    onNavigateToAuctionDetail = { id -> navController.navigate(AuctionDetailRoute.replace("{auctionId}", id)) }
                )
            }
            composable(route = StartAuctionRoute) {
                StartAuctionScreen(onNavigateBack = { navController.popBackStack() })
            }
            composable(
                route = AuctionDetailRoute,
                arguments = listOf(navArgument("auctionId") { type = NavType.StringType })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getString("auctionId") ?: ""
                AuctionDetailScreen(auctionId = id, onNavigateBack = { navController.popBackStack() })
            }
            composable(route = BuyerDashboardRoute) { DashboardPlaceholder(authViewModel, "Buyer Dashboard") { navController.navigate(LandingRoute) { popUpTo(0) } } }
            composable(route = ConsumerDashboardRoute) { DashboardPlaceholder(authViewModel, "Consumer Dashboard") { navController.navigate(LandingRoute) { popUpTo(0) } } }
            composable(route = TransportDashboardRoute) { DashboardPlaceholder(authViewModel, "Transport Provider Dashboard") { navController.navigate(LandingRoute) { popUpTo(0) } } }
            composable(route = StorageDashboardRoute) { DashboardPlaceholder(authViewModel, "Storage Owner Dashboard") { navController.navigate(LandingRoute) { popUpTo(0) } } }
            composable(route = AdminDashboardRoute) { DashboardPlaceholder(authViewModel, "Admin Dashboard") { navController.navigate(LandingRoute) { popUpTo(0) } } }
        }
    }
}

@Composable
fun SplashScreen(viewModel: AuthViewModel, onNavigate: (String) -> Unit) {
    val t = LocalAppTranslations.current
    val context = LocalContext.current
    val appPrefs = remember { AppPreferences(context) }
    val authState by viewModel.authState.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.checkSession(context)
    }
    
    LaunchedEffect(authState) {
        when(val state = authState) {
            is AuthState.Initial -> onNavigate(LandingRoute)
            is AuthState.RequiresProfileSetup -> onNavigate(ProfileSetupRoute)
            is AuthState.Success -> {
                appPrefs.saveRole(state.role)
                appPrefs.saveLanguage(state.language)
                val route = when(state.role) {
                    "farmer" -> FarmerDashboardRoute
                    "buyer" -> BuyerDashboardRoute
                    "consumer" -> ConsumerDashboardRoute
                    "transport" -> TransportDashboardRoute
                    "storage" -> StorageDashboardRoute
                    "admin" -> AdminDashboardRoute
                    else -> FarmerDashboardRoute 
                }
                onNavigate(route)
            }
            is AuthState.Error -> onNavigate(LandingRoute)
            else -> {}
        }
    }
    
    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
fun DashboardPlaceholder(viewModel: AuthViewModel, title: String, onLogout: () -> Unit) {
    val t = LocalAppTranslations.current
    val context = LocalContext.current
    val authState by viewModel.authState.collectAsState()
    val role = (authState as? AuthState.Success)?.role ?: "unknown"

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
        ) {
            AuthenticatedDashboardHeader(
                title = title,
                role = role,
                onLogout = {
                    viewModel.logout(context)
                    onLogout()
                }
            )
            Column(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(title, style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        viewModel.logout(context)
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(t.logout, color = MaterialTheme.colorScheme.onPrimary)
                }
            }
        }
    }
}

@Composable
fun PlaceholderScreen(title: String, onBack: () -> Unit) {
    val t = LocalAppTranslations.current
    Scaffold(containerColor = MaterialTheme.colorScheme.background) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(title, style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = onBack,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(t.close, color = MaterialTheme.colorScheme.onPrimary)
                }
            }
        }
    }
}

@Composable
fun LandingScreen(
    onGetStarted: () -> Unit,
    onHowItWorks: () -> Unit
) {
    val t = LocalAppTranslations.current
    var page by remember { mutableIntStateOf(0) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val screenHeight = maxHeight
                val imageHeight = screenHeight * 0.55f

                val imageRes = when (page) {
                    0 -> R.drawable.onboarding_crop
                    1 -> R.drawable.onboarding_market
                    else -> R.drawable.onboarding_selling
                }
                
                // Layer 1: Photograph
                Crossfade(targetState = imageRes, label = "imageFade") { res ->
                    Image(
                        painter = painterResource(id = res),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(imageHeight)
                    )
                }

                // Layer 2: Soft Bottom Fade strictly applied to image
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(imageHeight)
                        .background(
                            Brush.verticalGradient(
                                0.0f to Color.Transparent,
                                0.55f to Color.Transparent,
                                0.85f to MaterialTheme.colorScheme.background.copy(alpha = 0.6f),
                                1.0f to MaterialTheme.colorScheme.background
                            )
                        )
                )

                // Layer 3: Solid background for content below image
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .height(screenHeight - imageHeight + 2.dp)
                        .background(MaterialTheme.colorScheme.background)
                )

                // Layer 4: Fully opaque Logo and Content
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomStart)
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 36.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Eco,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = t.kisanMitraAi,
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Crossfade(targetState = page, label = "textFade") { targetPage ->
                        val label = when (targetPage) {
                            0 -> "CROP INTELLIGENCE"
                            1 -> "MARKET INTELLIGENCE"
                            else -> "SMART SELLING"
                        }
                        val headline = when (targetPage) {
                            0 -> "Know your crop."
                            1 -> "Know your market."
                            else -> "Know your profit."
                        }
                        val desc = when (targetPage) {
                            0 -> "Track crop health, spoilage risk and remaining\nshelf life with AI-powered insights."
                            1 -> "See today's mandi price, market trends and the expected price for the next two days."
                            else -> "Know your break-even, compare buyer bids and choose the selling opportunity that can give you a better return."
                        }

                        Column {
                            Text(
                                text = label,
                                color = MaterialTheme.colorScheme.tertiary,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = headline,
                                color = MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.displayMedium,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = (-1).sp
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = desc,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyLarge,
                                lineHeight = 24.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(36.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Page indicator
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            for (i in 0..2) {
                                if (i == page) {
                                    Box(modifier = Modifier.size(24.dp, 8.dp).background(MaterialTheme.colorScheme.primary, RoundedCornerShape(4.dp)))
                                } else {
                                    Box(modifier = Modifier.size(8.dp).background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha=0.3f), CircleShape))
                                }
                            }
                        }

                        // Next Button
                        Button(
                            onClick = {
                                if (page < 2) {
                                    page++
                                } else {
                                    onGetStarted()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(24.dp),
                            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                        ) {
                            Text(if (page == 2) "Get Started" else "Next", color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }
}

@Composable
fun HeroSection() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = buildAnnotatedString {
                withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.onSurface)) {
                    append("Know your crop.\n")
                }
                withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                    append("Know your market.\n")
                }
                withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                    append("Know your profit.")
                }
            },
            style = MaterialTheme.typography.displayLarge.copy(
                fontSize = 44.sp,
                lineHeight = 48.sp,
                letterSpacing = (-1.5).sp,
                fontWeight = FontWeight.ExtraBold
            ),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "AI-powered decision support for smarter crop selling. Leverage real-time insights to maximize your agricultural returns.",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 15.sp,
                lineHeight = 22.sp
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 12.dp)
        )
    }
}

@Composable
fun PrimaryButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 56.dp)
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(28.dp), spotColor = MaterialTheme.colorScheme.primary)
            .testTag("get_started_button"),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
        shape = RoundedCornerShape(28.dp)
    ) {
        Text(
            text = "Get Started",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimary
        )
    }
}

@Composable
fun SecondaryButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 56.dp)
            .testTag("how_it_works_button"),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shape = RoundedCornerShape(28.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
    ) {
        Icon(
            imageVector = Icons.Outlined.Info,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "How KisanMitra Works",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun FeaturePreview() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(32.dp),
                spotColor = Color.Black.copy(alpha = 0.05f)
            ),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FeatureCardHorizontal(
                icon = Icons.Outlined.Eco,
                iconTint = MaterialTheme.colorScheme.primary,
                title = "CROP ANALYSIS",
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                titleColor = MaterialTheme.colorScheme.primary
            )
            FeatureCardHorizontal(
                icon = Icons.Default.TrendingUp,
                iconTint = MaterialTheme.colorScheme.tertiary,
                title = "MARKET INTELLIGENCE",
                containerColor = MaterialTheme.colorScheme.surface,
                borderColor = MaterialTheme.colorScheme.surfaceVariant,
                titleColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
            FeatureCardHighlight(
                icon = Icons.Outlined.Psychology,
                title = "AI RECOMMENDATION",
                subtitle = "Synthesizing data for optimal timing.",
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                iconTint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun FeatureCardHorizontal(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    containerColor: Color,
    titleColor: Color,
    borderColor: Color = Color.Transparent
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(containerColor)
            .border(if (borderColor != Color.Transparent) 1.dp else 0.dp, borderColor, RoundedCornerShape(16.dp))
            .padding(horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .shadow(1.dp, RoundedCornerShape(12.dp), spotColor = Color.Black.copy(alpha = 0.1f))
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black),
            color = titleColor,
            letterSpacing = 2.sp
        )
    }
}

@Composable
fun FeatureCardHighlight(
    icon: ImageVector,
    title: String,
    subtitle: String,
    containerColor: Color,
    iconTint: Color
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(containerColor)
            .padding(vertical = 24.dp, horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                .shadow(1.dp, RoundedCornerShape(12.dp), spotColor = Color.Black.copy(alpha = 0.1f))
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black),
                color = iconTint,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = iconTint.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
        }
    }
}

data class LanguageOption(val nativeName: String, val englishName: String, val code: String)

@Composable
fun LanguageSelectionScreen(
    currentLanguageCode: String,
    onLanguageSelected: (String) -> Unit,
    onContinue: () -> Unit
) {
    val t = LocalAppTranslations.current
    var selectedLanguageCode by remember(currentLanguageCode) { mutableStateOf(currentLanguageCode) }

    val languages = listOf(
        LanguageOption("मराठी", "Marathi", "mr"),
        LanguageOption("हिन्दी", "Hindi", "hi"),
        LanguageOption("English", "English", "en"),
        LanguageOption("ગુજરાતી", "Gujarati", "gu"),
        LanguageOption("اردو", "Urdu", "ur"),
        LanguageOption("বাংলা", "Bengali", "bn"),
        LanguageOption("தமிழ்", "Tamil", "ta"),
        LanguageOption("తెలుగు", "Telugu", "te")
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo
            Spacer(modifier = Modifier.height(16.dp))
            Icon(
                imageVector = Icons.Outlined.Eco,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = t.kisanMitraAi,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Headings
            Text(
                text = t.welcome,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    letterSpacing = (-0.5).sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = t.chooseLanguage,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(languages) { lang ->
                    LanguageCard(
                        language = lang,
                        isSelected = selectedLanguageCode == lang.code,
                        onClick = { 
                            selectedLanguageCode = lang.code
                            onLanguageSelected(lang.code)
                        }
                    )
                }
            }

            // Bottom Section
            Button(
                onClick = onContinue,
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 56.dp)
                    .shadow(elevation = 2.dp, shape = RoundedCornerShape(28.dp), spotColor = MaterialTheme.colorScheme.primary),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(28.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = t.continueText,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Continue",
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = t.youCanChangeLanguage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun LanguageCard(
    language: LanguageOption,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 96.dp)
            .then(
                if (!isSelected) Modifier.shadow(
                    elevation = 4.dp,
                    shape = RoundedCornerShape(20.dp),
                    spotColor = Color.Black.copy(alpha = 0.04f),
                    ambientColor = Color.Black.copy(alpha = 0.04f)
                ) else Modifier
            )
            .clip(RoundedCornerShape(20.dp))
            .background(containerColor)
            .border(if (isSelected) 2.dp else 0.dp, borderColor, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
    ) {
        if (isSelected) {
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = "Selected",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 10.dp, end = 10.dp)
                    .size(20.dp)
            )
        }
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = language.nativeName,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                ),
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = language.englishName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

data class RoleOption(val title: String, val description: String, val icon: ImageVector, val route: String)

@Composable
fun RoleSelectionScreen(onContinue: (String) -> Unit) {
    val t = LocalAppTranslations.current
    var selectedRoleRoute by remember { mutableStateOf("farmer") }

    val roles = listOf(
        RoleOption(t.farmer, "Sell crops smarter", Icons.Outlined.Agriculture, "farmer"),
        RoleOption(t.buyer, "Find and bid on crops", Icons.Outlined.Store, "buyer"),
        RoleOption(t.consumer, "Buy fresh produce", Icons.Outlined.ShoppingBag, "consumer"),
        RoleOption(t.transportProvider, "Deliver produce", Icons.Outlined.LocalShipping, "transport"),
        RoleOption(t.warehouseStorage, "Offer storage", Icons.Outlined.Warehouse, "storage"),
        RoleOption(t.admin, "Manage the platform", Icons.Outlined.AdminPanelSettings, "admin")
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            // Headings
            Text(
                text = t.howWillYouUse,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    letterSpacing = (-0.5).sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Choose your role to get a personalized experience.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Roles List
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                roles.forEach { role ->
                    RoleCard(
                        role = role,
                        isSelected = selectedRoleRoute == role.route,
                        onClick = { selectedRoleRoute = role.route }
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Continue Button
            Button(
                onClick = { onContinue(selectedRoleRoute) },
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 56.dp)
                    .shadow(
                        elevation = 2.dp,
                        shape = RoundedCornerShape(28.dp),
                        spotColor = MaterialTheme.colorScheme.tertiary
                    ),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.tertiary,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(28.dp)
            ) {
                Text(
                    text = t.continueText,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiary
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun RoleCard(
    role: RoleOption,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 88.dp)
            .then(
                if (!isSelected) Modifier.shadow(
                    elevation = 4.dp,
                    shape = RoundedCornerShape(20.dp),
                    spotColor = Color.Black.copy(alpha = 0.04f),
                    ambientColor = Color.Black.copy(alpha = 0.04f)
                ) else Modifier
            )
            .clip(RoundedCornerShape(20.dp))
            .background(containerColor)
            .border(if (isSelected) 2.dp else 0.dp, borderColor, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon container
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(24.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = role.icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Text content
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = role.title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = role.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Selection indicator
        if (isSelected) {
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = "Selected",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        } else {
            Spacer(modifier = Modifier.size(24.dp))
        }
    }
}

@Composable
fun AuthScreen(role: String, viewModel: AuthViewModel, onNavigate: (String) -> Unit) {
    val t = LocalAppTranslations.current
    var mobileNumber by remember { mutableStateOf("") }
    var isSendingOtp by remember { mutableStateOf(false) }
    var isSigningInGoogle by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val appPrefs = remember { AppPreferences(context) }
    
    val displayRole = when(role) {
        "farmer" -> t.farmer
        "buyer" -> t.buyer
        "consumer" -> t.consumer
        "transport" -> t.transportProvider
        "storage" -> t.warehouseStorage
        "admin" -> t.admin
        else -> role.replaceFirstChar { it.uppercase() }
    }
    val roleIcon = when(role) {
        "farmer" -> "👨‍🌾"
        "buyer" -> "🏪"
        "consumer" -> "🛒"
        "transport" -> "🚚"
        "storage" -> "🏢"
        "admin" -> "🛡️"
        else -> "👨‍🌾"
    }
    
    val welcomeText = when(role) {
        "farmer" -> t.welcomeFarmer
        "buyer" -> t.welcomeBuyer
        "consumer" -> t.welcomeConsumer
        "transport" -> t.welcomeTransport
        "storage" -> t.welcomeStorage
        "admin" -> t.welcomeAdmin
        else -> "${t.welcome} $displayRole"
    }

    val subtitle = if (role == "farmer") {
        "Sign in to manage your crops, expenses, and selling decisions."
    } else {
        "Sign in to access your $displayRole dashboard."
    }

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            
            // Top Brand Area
            Spacer(modifier = Modifier.height(16.dp))
            Icon(Icons.Outlined.Eco, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(t.kisanMitraAi, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            
            // Role Badge
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(16.dp))
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Text("$roleIcon $displayRole", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
            
            // Main Heading & Supporting Text
            Spacer(modifier = Modifier.height(32.dp))
            Text(welcomeText, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(12.dp))
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
            
            Spacer(modifier = Modifier.height(40.dp))
            
            // Mobile Input
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(t.mobileNumber, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 56.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(28.dp))
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Outlined.Phone, null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(12.dp))
                    Text("+91", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(Modifier.width(12.dp))
                    Box(modifier = Modifier.width(1.dp).height(24.dp).background(MaterialTheme.colorScheme.outlineVariant))
                    Spacer(Modifier.width(12.dp))
                    
                    BasicTextField(
                        value = mobileNumber,
                        onValueChange = { if (it.length <= 10) mobileNumber = it.filter { char -> char.isDigit() } },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                        decorationBox = { innerTextField ->
                            if (mobileNumber.isEmpty()) {
                                Text(t.enterMobileNumber, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyLarge)
                            }
                            innerTextField()
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Send OTP Button
            Button(
                onClick = { 
                    coroutineScope.launch {
                        try {
                            isSendingOtp = true
                            delay(1500)
                            onNavigate(OTPRoute)
                        } finally {
                            isSendingOtp = false
                        }
                    }
                },
                enabled = mobileNumber.length == 10 && !isSendingOtp && !isSigningInGoogle,
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.tertiary,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha=0.5f)
                ),
                shape = RoundedCornerShape(28.dp)
            ) {
                Text(
                    if (isSendingOtp) "Sending..." else t.sendOtp, 
                    style = MaterialTheme.typography.titleMedium, 
                    fontWeight = FontWeight.Bold, 
                    color = if(mobileNumber.length == 10) MaterialTheme.colorScheme.onTertiary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha=0.5f)
                )
                Spacer(Modifier.width(8.dp))
                if (isSendingOtp) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onTertiary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, null, modifier = Modifier.size(20.dp))
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // OR Divider
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant)
                Text(t.orText, modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium)
                HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant)
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Google Sign In
            OutlinedButton(
                onClick = { 
                    coroutineScope.launch {
                        try {
                            isSigningInGoogle = true
                            delay(1500)
                            onNavigate(ProfileSetupRoute)
                        } finally {
                            isSigningInGoogle = false
                        }
                    }
                },
                enabled = !isSendingOtp && !isSigningInGoogle,
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 56.dp),
                shape = RoundedCornerShape(28.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    disabledContainerColor = MaterialTheme.colorScheme.surface,
                    disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha=0.5f)
                )
            ) {
                if (isSigningInGoogle) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onSurface,
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(t.signingIn, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                } else {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_google),
                        contentDescription = "Google",
                        modifier = Modifier.size(24.dp),
                        tint = Color.Unspecified
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(t.continueWithGoogle, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }
            }
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Privacy Message
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Outlined.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Your phone number is used only for\nsecure account access.", 
                    style = MaterialTheme.typography.bodySmall, 
                    color = MaterialTheme.colorScheme.onSurfaceVariant, 
                    textAlign = TextAlign.Center
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun OtpVerificationScreen(viewModel: AuthViewModel, onNavigate: (String) -> Unit) {
    val t = LocalAppTranslations.current
    var otpText by remember { mutableStateOf("") }
    var isVerifying by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    Scaffold(containerColor = MaterialTheme.colorScheme.background) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(16.dp))
            Icon(Icons.Outlined.Eco, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
            Spacer(Modifier.height(8.dp))
            Text(t.kisanMitraAi, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            
            Spacer(modifier = Modifier.height(32.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = 8.dp, shape = RoundedCornerShape(32.dp), spotColor = Color.Black.copy(alpha=0.04f), ambientColor = Color.Black.copy(alpha=0.04f)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(32.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(t.enterVerificationCode, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(Modifier.height(8.dp))
                    Text(t.otpSent, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    
                    Spacer(Modifier.height(32.dp))
                    
                    // OTP Input
                    BasicTextField(
                        value = otpText,
                        onValueChange = { if (it.length <= 6) otpText = it.filter { char -> char.isDigit() } },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword)
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            repeat(6) { index ->
                                val isFocused = otpText.length == index
                                val isFilled = index < otpText.length
                                val char = if (isFilled) otpText[index].toString() else ""
                                
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(0.8f)
                                        .background(if (isFocused || isFilled) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                                        .border(if (isFocused) 2.dp else if(isFilled) 1.dp else 0.dp, if(isFocused) MaterialTheme.colorScheme.primary else if(isFilled) MaterialTheme.colorScheme.tertiary else Color.Transparent, RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(char, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                    if (isFocused && char.isEmpty()) {
                                        Box(modifier = Modifier.width(2.dp).height(24.dp).background(MaterialTheme.colorScheme.primary))
                                    }
                                }
                            }
                        }
                    }
                    
                    Spacer(Modifier.height(32.dp))
                    
                    Button(
                        onClick = { 
                            coroutineScope.launch {
                                try {
                                    isVerifying = true
                                    delay(1500)
                                    onNavigate(ProfileSetupRoute)
                                } finally {
                                    isVerifying = false
                                }
                            }
                        },
                        enabled = otpText.length == 6 && !isVerifying,
                        modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiary,
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha=0.5f)
                        ),
                        shape = RoundedCornerShape(28.dp)
                    ) {
                        Text(
                            if (isVerifying) "Verifying..." else t.verifyAndContinue, 
                            style = MaterialTheme.typography.bodyLarge, 
                            fontWeight = FontWeight.Bold, 
                            color = if(otpText.length == 6) MaterialTheme.colorScheme.onTertiary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha=0.5f)
                        )
                        Spacer(Modifier.width(8.dp))
                        if (isVerifying) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onTertiary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, null, modifier = Modifier.size(20.dp))
                        }
                    }
                    
                    Spacer(Modifier.height(24.dp))
                    Text(t.didNotReceiveOtp, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(4.dp))
                    Row {
                        Text(t.resendOtp + " ", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(t.inSeconds, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary)
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileSetupScreen(viewModel: AuthViewModel, onNavigate: (String) -> Unit) {
    val t = LocalAppTranslations.current
    var name by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var farmSize by remember { mutableStateOf("") }
    var crop by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    val appPrefs = remember { AppPreferences(context) }
    val coroutineScope = rememberCoroutineScope()
    
    val authState by viewModel.authState.collectAsState()
    val savedRole by appPrefs.roleFlow.collectAsState(initial = "farmer")
    val savedLanguage by appPrefs.languageFlow.collectAsState(initial = "en")
    
    LaunchedEffect(authState) {
        when (val state = authState) {
            is AuthState.Success -> {
                appPrefs.saveRole(state.role)
                appPrefs.saveLanguage(state.language)
                val route = when(state.role) {
                    "farmer" -> FarmerDashboardRoute
                    "buyer" -> BuyerDashboardRoute
                    "consumer" -> ConsumerDashboardRoute
                    "transport" -> TransportDashboardRoute
                    "storage" -> StorageDashboardRoute
                    "admin" -> AdminDashboardRoute
                    else -> FarmerDashboardRoute
                }
                onNavigate(route)
            }
            is AuthState.Error -> {
                android.widget.Toast.makeText(context, state.message, android.widget.Toast.LENGTH_LONG).show()
                viewModel.resetState()
            }
            else -> {}
        }
    }
    
    Scaffold(containerColor = MaterialTheme.colorScheme.background) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            Spacer(Modifier.height(16.dp))
            Text(t.setupFarm, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(8.dp))
            Text(t.personalizeRecs, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            
            Spacer(Modifier.height(32.dp))
            
            Text(t.fullName, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(8.dp))
            CustomTextField(value = name, onValueChange = { name = it }, placeholder = "Enter your name")
            
            Spacer(Modifier.height(20.dp))
            
            Text(t.location, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(8.dp))
            CustomTextField(value = location, onValueChange = { location = it }, placeholder = "Village / City")
            
            Spacer(Modifier.height(20.dp))
            
            Text(t.farmSize, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(8.dp))
            CustomTextField(value = farmSize, onValueChange = { farmSize = it }, placeholder = "e.g. 3 acres")
            
            Spacer(Modifier.height(20.dp))
            
            Text(t.primaryCrop, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(8.dp))
            CustomDropdown(value = crop, onValueChange = { crop = it })
            
            Spacer(Modifier.height(40.dp))
            
            val isEnabled = name.isNotBlank() && location.isNotBlank() && farmSize.isNotBlank() && !isSaving
            Button(
                onClick = { 
                    coroutineScope.launch {
                        try {
                            isSaving = true
                            delay(1500)
                            
                            val role = savedRole ?: "farmer"
                            val route = when(role) {
                                "farmer" -> FarmerDashboardRoute
                                "buyer" -> BuyerDashboardRoute
                                "consumer" -> ConsumerDashboardRoute
                                "transport" -> TransportDashboardRoute
                                "storage" -> StorageDashboardRoute
                                "admin" -> AdminDashboardRoute
                                else -> FarmerDashboardRoute
                            }
                            onNavigate(route)
                        } finally {
                            isSaving = false
                        }
                    }
                },
                enabled = isEnabled,
                modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.tertiary,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha=0.5f)
                ),
                shape = RoundedCornerShape(28.dp)
            ) {
                Text(
                    if (isSaving) "Saving..." else "Save & Continue", 
                    style = MaterialTheme.typography.bodyLarge, 
                    fontWeight = FontWeight.Bold, 
                    color = if(isEnabled) MaterialTheme.colorScheme.onTertiary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha=0.5f)
                )
                Spacer(Modifier.width(8.dp))
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onTertiary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, null, modifier = Modifier.size(20.dp))
                }
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
fun CustomTextField(value: String, onValueChange: (String) -> Unit, placeholder: String) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 56.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (value.isEmpty()) {
                    Text(placeholder, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                innerTextField()
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomDropdown(value: String, onValueChange: (String) -> Unit) {
    val t = LocalAppTranslations.current
    var expanded by remember { mutableStateOf(false) }
    val options = listOf("Tomato", "Onion", "Potato", "Soybean", "Other")
    
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        BasicTextField(
            value = value,
            readOnly = true,
            onValueChange = {},
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
            modifier = Modifier.menuAnchor(),
            decorationBox = { innerTextField ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 56.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        if (value.isEmpty()) {
                            Text(t.selectPrimaryCrop, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        } else {
                            innerTextField()
                        }
                    }
                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown", tint = MaterialTheme.colorScheme.onSurface)
                }
            }
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface) },
                    onClick = {
                        onValueChange(option)
                        expanded = false
                    }
                )
            }
        }
    }
}
