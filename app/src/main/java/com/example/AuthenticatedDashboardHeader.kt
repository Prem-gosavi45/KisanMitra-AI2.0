package com.example

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch

@Composable
fun AuthenticatedDashboardHeader(
    title: String,
    location: String? = null,
    role: String,
    onLogout: () -> Unit // Kept for future or other avatar click
) {
    val context = LocalContext.current
    val appPrefs = remember(context) { AppPreferences(context) }
    val currentLang by appPrefs.languageFlow.collectAsState(initial = "en")
    val coroutineScope = rememberCoroutineScope()
    var showLanguageMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = title, 
                style = MaterialTheme.typography.titleLarge, 
                fontWeight = FontWeight.Bold, 
                color = MaterialTheme.colorScheme.onSurface
            )
            if (location != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.LocationOn, 
                        contentDescription = null, 
                        modifier = Modifier.size(16.dp), 
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        location, 
                        style = MaterialTheme.typography.bodyMedium, 
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box {
                IconButton(onClick = { showLanguageMenu = true }) {
                    Icon(
                        Icons.Outlined.Language,
                        contentDescription = "Change Language",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                DropdownMenu(
                    expanded = showLanguageMenu,
                    onDismissRequest = { showLanguageMenu = false }
                ) {
                    val languages = listOf("en" to "English", "hi" to "हिन्दी", "mr" to "मराठी", "kn" to "ಕನ್ನಡ")
                    languages.forEach { (code, name) ->
                        DropdownMenuItem(
                            text = { 
                                Text(
                                    text = name, 
                                    fontWeight = if (currentLang == code) FontWeight.Bold else FontWeight.Normal,
                                    color = if (currentLang == code) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                ) 
                            },
                            onClick = { 
                                coroutineScope.launch { appPrefs.saveLanguage(code) }
                                showLanguageMenu = false
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            NotificationBell(role = role)
        }
    }
}

@Composable
fun NotificationBell(role: String) {
    var showNotificationCenter by remember { mutableStateOf(false) }
    
    // Mock notifications based on role
    val notifications = getMockNotifications(role)
    val unreadCount = notifications.count { !it.isRead }

    Box(
        modifier = Modifier
            .size(44.dp)
            .background(Color(0xFFE2EBE5), CircleShape)
            .clickable { showNotificationCenter = true },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            Icons.Filled.Notifications, 
            contentDescription = "Notifications", 
            tint = MaterialTheme.colorScheme.onSurface, 
            modifier = Modifier.size(22.dp)
        )
        if (unreadCount > 0) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = (-10).dp, y = 10.dp)
                    .background(Color.Red, CircleShape)
            )
        }
    }
    
    if (showNotificationCenter) {
        NotificationCenter(
            notifications = notifications,
            onDismiss = { showNotificationCenter = false }
        )
    }
}

data class NotificationItem(
    val id: String,
    val title: String,
    val message: String,
    val isRead: Boolean,
    val timestamp: String,
    val category: String
)

fun getMockNotifications(role: String): List<NotificationItem> {
    return when (role) {
        "farmer" -> listOf(
            NotificationItem("1", "Crop spoilage alert", "High risk of spoilage for Tomato batch due to humidity.", false, "10 min ago", "Alert"),
            NotificationItem("2", "Price forecast update", "Onion prices expected to rise by 15% next week.", false, "1 hour ago", "Forecast"),
            NotificationItem("3", "New auction bid", "Ramesh received a new bid for Potato.", true, "2 hours ago", "Auction")
        )
        "buyer" -> listOf(
            NotificationItem("1", "New auction", "Fresh Tomato batch available in your area.", false, "5 min ago", "Auction"),
            NotificationItem("2", "Outbid alert", "You have been outbid on the Potato lot.", false, "30 min ago", "Alert")
        )
        "consumer" -> listOf(
            NotificationItem("1", "Order status", "Your produce is out for delivery.", false, "20 min ago", "Delivery"),
            NotificationItem("2", "Fresh produce updates", "New organic apples available nearby.", true, "1 day ago", "Update")
        )
        "transport" -> listOf(
            NotificationItem("1", "New delivery request", "Delivery request from Nashik to Mumbai.", false, "Just now", "Request"),
            NotificationItem("2", "Pickup reminder", "Pickup scheduled at 4:00 PM today.", true, "2 hours ago", "Reminder")
        )
        "storage" -> listOf(
            NotificationItem("1", "Storage booking", "New booking for 500kg of Potatoes.", false, "15 min ago", "Booking"),
            NotificationItem("2", "Booking reminder", "Capacity reaching 90%.", true, "5 hours ago", "Alert")
        )
        "admin" -> listOf(
            NotificationItem("1", "New user", "New farmer registration pending approval.", false, "2 mins ago", "Admin"),
            NotificationItem("2", "Platform alert", "System maintenance scheduled for midnight.", true, "1 day ago", "System")
        )
        else -> emptyList()
    }
}

@Composable
fun NotificationCenter(notifications: List<NotificationItem>, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Notifications", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Outlined.Close, contentDescription = "Close")
                    }
                }
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                if (notifications.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("No notifications", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyColumn(modifier = Modifier.weight(1f, fill = false)) {
                        items(notifications) { notif ->
                            NotificationCard(notif)
                            Divider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.surfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationCard(notification: NotificationItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .padding(top = 6.dp)
                .background(
                    if (notification.isRead) Color.Transparent else MaterialTheme.colorScheme.primary, 
                    CircleShape
                )
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(notification.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
            Text(notification.message, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(notification.category, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                Text(notification.timestamp, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
