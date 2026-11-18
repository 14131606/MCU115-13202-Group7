//用來接收 API 回傳的 JSON 資料

package com.example.currencybudgetapp.api

data class ExchangeRateResponse(
    val result: String,
    val base_code: String,
    val time_last_update_utc: String,
    val rates: Map<String, Double>
)