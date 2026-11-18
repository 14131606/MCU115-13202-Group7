//定義向公開匯率 API 抓取最新匯率的連線路徑

package com.example.currencybudgetapp.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface ExchangeRateApiService {
    @GET("v6/latest/{base}")
    suspend fun getLatestRates(
        @Path("base") baseCurrency: String
    ): Response<ExchangeRateResponse>
}