//負責抓取並暫存最新匯率（使用 SharedPreferences），同時提供公開的換算函式給組員 C 的記帳功能呼叫

package com.example.currencybudgetapp.api

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object CurrencyHelper {
    private const val PREF_NAME = "currency_rates_pref"
    private const val KEY_RATES_JSON = "cached_rates_json"
    private const val KEY_LAST_UPDATE = "last_update_time"

    // 預設離線備用匯率（以 TWD 為基準的常見幣別）
    private val defaultRatesToTwd = mapOf(
        "TWD" to 1.0,
        "USD" to 32.50,
        "JPY" to 0.215,
        "KRW" to 0.0235,
        "EUR" to 35.20,
        "CNY" to 4.50,
        "HKD" to 4.18
    )

    // 1. 向 API 抓取最新匯率並暫存（以 TWD 為基準）
    suspend fun fetchAndCacheRates(context: Context): Boolean {
        return try {
            val response = RetrofitClient.instance.getLatestRates("TWD")
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.result == "success") {
                    saveRatesToLocal(context, body.rates, body.time_last_update_utc)
                    true
                } else {
                    false
                }
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // 2. 將抓取到的匯率表存入 SharedPreferences
    private fun saveRatesToLocal(context: Context, rates: Map<String, Double>, updateTime: String) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val jsonString = Gson().toJson(rates)
        prefs.edit()
            .putString(KEY_RATES_JSON, jsonString)
            .putString(KEY_LAST_UPDATE, updateTime)
            .apply()
    }

    // 3. 讀取本地暫存的所有匯率（1 TWD 可換多少外幣）
    fun getCachedRates(context: Context): Map<String, Double> {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val jsonString = prefs.getString(KEY_RATES_JSON, null)
        return if (jsonString != null) {
            val type = object : TypeToken<Map<String, Double>>() {}.type
            Gson().fromJson(jsonString, type)
        } else {
            emptyMap()
        }
    }

    // 4. 取得「1 單位外幣 = 多少台幣 (TWD)」
    fun getRateToTwd(context: Context, currencyCode: String): Double {
        val upperCode = currencyCode.uppercase()
        if (upperCode == "TWD") return 1.0

        val cachedRates = getCachedRates(context)
        val rateFromTwd = cachedRates[upperCode]

        // API 回傳的是 1 TWD = X 外幣，因此反推 1 外幣 = 1 / X TWD
        return if (rateFromTwd != null && rateFromTwd > 0.0) {
            1.0 / rateFromTwd
        } else {
            defaultRatesToTwd[upperCode] ?: 1.0
        }
    }

    // 5. 核心換算函式（直接提供給組員 C 與匯率試算頁呼叫）
    fun convertCurrency(
        context: Context,
        amount: Double,
        fromCurrency: String,
        toCurrency: String
    ): Double {
        val fromRateToTwd = getRateToTwd(context, fromCurrency)
        val toRateToTwd = getRateToTwd(context, toCurrency)
        val amountInTwd = amount * fromRateToTwd
        return amountInTwd / toRateToTwd
    }

    // 6. 取得最後更新時間字串（可顯示在 UI 畫面上）
    fun getLastUpdateTime(context: Context): String {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_LAST_UPDATE, "尚未從網路更新（使用預設匯率）") ?: ""
    }
}