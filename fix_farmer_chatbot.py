import re

with open("app/src/main/java/com/example/FarmerDashboardScreen.kt", "r") as f:
    code = f.read()

# Add a boolean state for chatbot
code = code.replace(
    'var showSettings by remember { mutableStateOf(false) }',
    'var showSettings by remember { mutableStateOf(false) }\n    var showChatbot by remember { mutableStateOf(false) }'
)

# Show ChatbotOverlay if showChatbot is true
chatbot_call = """
    if (showChatbot) {
        val contextData = "Crop: ${marketData?.cropName ?: "Tomato"}, Expected Profit: ₹4300, Break Even Price: ₹16.0"
        ChatbotOverlay(
            onDismiss = { showChatbot = false },
            contextData = contextData,
            language = currentLang
        )
    }
"""

code = code.replace(
    'if (showSettings) {',
    chatbot_call + '    if (showSettings) {'
)

# Update AiFloatingButton click handler
code = code.replace(
    'floatingActionButton = { AiFloatingButton(t) },',
    'floatingActionButton = { AiFloatingButton(t, onClick = { showChatbot = true }) },'
)

# Update AiFloatingButton signature
code = code.replace(
    'fun AiFloatingButton(t: AppTranslations) {',
    'fun AiFloatingButton(t: AppTranslations, onClick: () -> Unit) {'
)

# Update AiFloatingButton internal clickable
code = code.replace(
    '.clickable { }',
    '.clickable { onClick() }'
)

with open("app/src/main/java/com/example/FarmerDashboardScreen.kt", "w") as f:
    f.write(code)
