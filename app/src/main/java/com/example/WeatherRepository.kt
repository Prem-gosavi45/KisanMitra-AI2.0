package com.example

import kotlinx.coroutines.delay

data class DailyForecast(
    val day: String,
    val highTemp: Int,
    val lowTemp: Int,
    val condition: String // "Sunny", "Rainy", "Cloudy"
)

data class WeatherData(
    val district: String,
    val currentTemp: Int,
    val condition: String,
    val humidity: Int,
    val windSpeed: Int, // km/h
    val actionableAdvice: String,
    val forecast: List<DailyForecast>
)

interface WeatherRepository {
    suspend fun getWeatherForDistrict(district: String): WeatherData
}

class MockWeatherRepository : WeatherRepository {
    override suspend fun getWeatherForDistrict(district: String): WeatherData {
        delay(800) // Simulate network delay
        return WeatherData(
            district = district,
            currentTemp = 28,
            condition = "Partly Cloudy",
            humidity = 65,
            windSpeed = 12,
            actionableAdvice = "Favorable conditions for harvesting today. Delay irrigation as light rain is expected tomorrow.",
            forecast = listOf(
                DailyForecast("Today", 30, 22, "Partly Cloudy"),
                DailyForecast("Tomorrow", 27, 20, "Rainy"),
                DailyForecast("Wed", 29, 21, "Sunny")
            )
        )
    }
}
