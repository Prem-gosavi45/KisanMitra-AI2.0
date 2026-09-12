package com.example

data class RecommendationResult(
    val action: String,
    val reason: String,
    val expectedProfitToday: Double,
    val expectedProfitTomorrow: Double,
    val expectedAdditionalReturn: Double
)

class DecisionEngine {
    fun getSellingRecommendation(
        currentPrice: Double,
        forecastPrice: Double,
        breakEvenPrice: Double,
        remainingShelfLifeDays: Int,
        bestBid: Double,
        forecastConfidence: Int,
        t: AppTranslations
    ): RecommendationResult {
        val qty = 800.0 // Mock quantity
        val profitToday = (currentPrice - breakEvenPrice) * qty
        val profitTomorrow = (forecastPrice - breakEvenPrice) * qty
        val additionalReturn = profitTomorrow - profitToday

        val action: String
        val reason: String

        if (remainingShelfLifeDays < 2) {
            action = t.sellToday
            reason = t.reasonLowShelfLife
        } else if (forecastConfidence < 60) {
             action = t.sellToday
             reason = t.reasonLowConfidence
        } else if (forecastPrice > currentPrice && forecastPrice >= breakEvenPrice) {
            action = t.waitForTomorrow
            reason = "Tomorrow's expected price is higher by ₹${String.format("%.2f", forecastPrice - currentPrice)}/kg and your crop has enough remaining shelf life to wait."
        } else {
            action = t.sellToday
            reason = t.reasonPricesDrop
        }

        return RecommendationResult(
            action = action,
            reason = reason,
            expectedProfitToday = profitToday,
            expectedProfitTomorrow = profitTomorrow,
            expectedAdditionalReturn = additionalReturn
        )
    }
}
