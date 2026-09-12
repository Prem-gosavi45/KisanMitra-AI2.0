package com.example

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ChatMessage(
    val role: String, // "user" or "model"
    val text: String
)

class ChatbotViewModel : ViewModel() {
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun sendMessage(userText: String, contextData: String, language: String) {
        if (userText.isBlank()) return
        
        val newMessages = _messages.value + ChatMessage("user", userText)
        _messages.value = newMessages
        _isLoading.value = true
        _error.value = null

        viewModelScope.launch {
            try {
                val apiKey = BuildConfig.GEMINI_API_KEY
                
                val systemInstructionText = """
You are KisanMitra AI, an agricultural assistant designed to help farmers make better crop-selling and market decisions.
You can help with:
- Crop selling decisions
- Mandi/market prices
- Price trends
- Crop quality
- Spoilage risk
- Storage decisions
- Buyer selection
- Transportation
- Profit estimation
- Auction-related questions
- General agricultural questions

Give practical, simple and easy-to-understand answers.
Avoid unnecessarily technical language.
When relevant, explain the reasoning behind your recommendation.
Never claim that a price, market condition, weather condition, or other real-time information is live unless that data is actually available through an API or database.
If required information is missing, ask the farmer for the necessary details.

Current Farmer Dashboard Context:
$contextData

IMPORTANT: You MUST respond in the following language: $language
                """.trimIndent()

                val systemInstruction = Content(
                    role = "system",
                    parts = listOf(Part(text = systemInstructionText))
                )

                // Build history for API
                val contents = newMessages.map { 
                    Content(
                        role = if (it.role == "user") "user" else "model",
                        parts = listOf(Part(text = it.text))
                    )
                }

                val request = GenerateContentRequest(
                    contents = contents,
                    systemInstruction = systemInstruction
                )

                val response = RetrofitClient.service.generateContent(apiKey, request)
                val responseText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                
                if (responseText != null) {
                    _messages.value = _messages.value + ChatMessage("model", responseText)
                } else {
                    _error.value = "Sorry, I’m unable to respond right now. Please try again."
                }
            } catch (e: Exception) {
                _error.value = "Sorry, I’m unable to respond right now. Please try again."
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearChat() {
        _messages.value = emptyList()
        _error.value = null
    }
}
