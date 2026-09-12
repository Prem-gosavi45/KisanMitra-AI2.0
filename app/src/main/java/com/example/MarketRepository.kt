package com.example

import java.util.Locale
import kotlin.math.abs

data class MarketPrice(
    val cropId: String,
    val marketName: String,
    val date: String,
    val time: String,
    val pricePerKg: Double,
    val source: String,
    val isForecast: Boolean,
    val confidence: Int?
)

data class MarketData(
    val crop: String,
    val marketName: String,
    val yesterdayPrice: Double,
    val todayPrice: Double,
    val tomorrowForecast: Double,
    val confidence: Int,
    val priceHistory: List<MarketPrice> = emptyList(),
    val sevenDayForecast: List<MarketPrice> = emptyList(),
    val updatedAt: String = "Today, 10:30 AM"
) {
    val priceChange: Double
        get() = todayPrice - yesterdayPrice

    val percentageChange: Double
        get() = if (yesterdayPrice > 0) ((todayPrice - yesterdayPrice) / yesterdayPrice) * 100.0 else 0.0

    val isUp: Boolean
        get() = priceChange >= 0

    val formattedPrice: String
        get() = String.format(Locale.US, "₹%.2f", todayPrice)

    val formattedPercentage: String
        get() = String.format(Locale.US, "%.1f%%", abs(percentageChange))

    val formattedTrend: String
        get() = String.format(Locale.US, "%s %.1f%%", if (isUp) "↑" else "↓", abs(percentageChange))
}

data class RegionalMarket(
    val marketName: String,
    val distanceKm: Int,
    val pricePerKg: Double,
    val trend: Double
) {
    val isUp: Boolean get() = trend >= 0
    val formattedTrend: String
        get() = String.format(Locale.US, "%s %.1f%%", if (isUp) "↑" else "↓", abs(trend))
}

interface MarketRepository {
    suspend fun getMarketData(crop: String): MarketData
    suspend fun getYesterdayPrices(crop: String): List<MarketPrice>
    suspend fun getTodayPrices(crop: String): List<MarketPrice>
    suspend fun getTomorrowForecast(crop: String): List<MarketPrice>
    suspend fun getSevenDayForecast(crop: String): List<MarketPrice>
    suspend fun getCurrentPrice(crop: String): MarketPrice
    suspend fun getMarketSummary(crop: String): Map<String, Double>
    suspend fun getRegionalMarkets(crop: String): List<RegionalMarket>
}

class MockMarketRepository : MarketRepository {
    override suspend fun getMarketData(crop: String): MarketData {
        val (yesterday, today, tomorrow) = getPricesForCrop(crop)
        val history = getYesterdayPrices(crop) + getTodayPrices(crop)
        val sevenDay = getSevenDayForecast(crop)
        return MarketData(
            crop = crop,
            marketName = "Nashik Mandi",
            yesterdayPrice = yesterday,
            todayPrice = today,
            tomorrowForecast = tomorrow,
            confidence = 78,
            priceHistory = history,
            sevenDayForecast = sevenDay,
            updatedAt = "Today, 10:30 AM"
        )
    }

    override suspend fun getYesterdayPrices(crop: String): List<MarketPrice> {
        val (yesterday, _, _) = getPricesForCrop(crop)
        return listOf(
            MarketPrice(crop, "Nashik Mandi", "Yesterday", "6 AM", yesterday - 0.5, "Mock", false, null),
            MarketPrice(crop, "Nashik Mandi", "Yesterday", "10 AM", yesterday, "Mock", false, null),
            MarketPrice(crop, "Nashik Mandi", "Yesterday", "2 PM", yesterday + 0.3, "Mock", false, null),
            MarketPrice(crop, "Nashik Mandi", "Yesterday", "6 PM", yesterday - 0.1, "Mock", false, null)
        )
    }

    override suspend fun getTodayPrices(crop: String): List<MarketPrice> {
        val (_, today, _) = getPricesForCrop(crop)
        return listOf(
            MarketPrice(crop, "Nashik Mandi", "Today", "6 AM", today - 0.7, "Mock", false, null),
            MarketPrice(crop, "Nashik Mandi", "Today", "10 AM", today - 0.2, "Mock", false, null),
            MarketPrice(crop, "Nashik Mandi", "Today", "2 PM", today, "Mock", false, null),
            MarketPrice(crop, "Nashik Mandi", "Today", "6 PM", today + 0.3, "Mock", false, null)
        )
    }

    override suspend fun getTomorrowForecast(crop: String): List<MarketPrice> {
        val (_, _, tomorrow) = getPricesForCrop(crop)
        return listOf(
            MarketPrice(crop, "Nashik Mandi", "Tomorrow", "6 AM", tomorrow - 0.4, "AI", true, 75),
            MarketPrice(crop, "Nashik Mandi", "Tomorrow", "10 AM", tomorrow, "AI", true, 78),
            MarketPrice(crop, "Nashik Mandi", "Tomorrow", "2 PM", tomorrow + 0.3, "AI", true, 80),
            MarketPrice(crop, "Nashik Mandi", "Tomorrow", "6 PM", tomorrow - 0.2, "AI", true, 72)
        )
    }

    override suspend fun getSevenDayForecast(crop: String): List<MarketPrice> {
        val (_, today, _) = getPricesForCrop(crop)
        return (1..7).map { day ->
            val trend = when (crop.lowercase()) {
                "onion", "कांदा", "प्याज़" -> 1.2
                "potato", "बटाटा", "आलू" -> 0.8
                else -> 0.5
            }
            val simulatedPrice = today + (day * trend) + (Math.random() * 1.5 - 0.75)
            
            // Format date string
            val calendar = java.util.Calendar.getInstance()
            calendar.add(java.util.Calendar.DAY_OF_YEAR, day)
            val sdf = java.text.SimpleDateFormat("MMM dd", Locale.US)
            val dateString = sdf.format(calendar.time)
            
            MarketPrice(
                cropId = crop,
                marketName = "Nashik Mandi",
                date = dateString,
                time = "10 AM",
                pricePerKg = simulatedPrice,
                source = "AI Forecast",
                isForecast = true,
                confidence = 85 - (day * 3)
            )
        }
    }

    override suspend fun getCurrentPrice(crop: String): MarketPrice {
        val (_, today, _) = getPricesForCrop(crop)
        return MarketPrice(crop, "Nashik Mandi", "Today", "2 PM", today, "Mock", false, null)
    }

    override suspend fun getMarketSummary(crop: String): Map<String, Double> {
        val (yesterday, today, tomorrow) = getPricesForCrop(crop)
        return mapOf(
            "Yesterday" to yesterday,
            "Today" to today,
            "Tomorrow" to tomorrow
        )
    }

    override suspend fun getRegionalMarkets(crop: String): List<RegionalMarket> {
        val (_, today, _) = getPricesForCrop(crop)
        return listOf(
            RegionalMarket("Nashik (Local)", 12, today, 2.5),
            RegionalMarket("Pune APMC", 165, today + 3.5, 4.1),
            RegionalMarket("Mumbai Vashi", 210, today + 8.0, 1.2),
            RegionalMarket("Lasalgaon", 85, today - 1.5, -0.8)
        ).sortedByDescending { it.pricePerKg }
    }

    private fun getPricesForCrop(crop: String): Triple<Double, Double, Double> {
        // Returns Triple(yesterday, today, tomorrowForecast)
        return when (crop.lowercase()) {
            "potato", "बटाटा", "आलू" -> Triple(16.50, 18.80, 20.50)
            "onion", "कांदा", "प्याज़" -> Triple(22.00, 25.80, 28.00)
            // Default: Tomato - yesterday = 13.00, today = 15.20 (+16.9%), tomorrow = 17.00
            else -> Triple(13.00, 15.20, 17.00)
        }
    }
}
