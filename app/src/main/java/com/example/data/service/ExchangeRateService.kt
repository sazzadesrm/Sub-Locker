package com.example.data.service

import com.example.data.model.Currency
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object ExchangeRateService {
    private const val API_URL = "https://open.er-api.com/v6/latest/USD"

    suspend fun fetchLatestRates(): Map<String, Double> = withContext(Dispatchers.IO) {
        try {
            val url = URL(API_URL)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 5000
            connection.readTimeout = 5000

            if (connection.responseCode == 200) {
                val jsonString = connection.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(jsonString)
                val ratesObj = json.optJSONObject("rates")
                val updatedRates = mutableMapOf<String, Double>()

                if (ratesObj != null) {
                    val keys = ratesObj.keys()
                    while (keys.hasNext()) {
                        val key = keys.next()
                        val rate = ratesObj.optDouble(key, -1.0)
                        if (rate > 0) {
                            updatedRates[key] = rate
                            Currency.updateRate(key, rate)
                            if (key == "BDT") {
                                Currency.updateRate("TK", rate)
                            }
                        }
                    }
                }
                return@withContext updatedRates
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return@withContext emptyMap()
    }
}
