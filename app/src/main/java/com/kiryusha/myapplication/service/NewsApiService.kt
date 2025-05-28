package com.kiryusha.myapplication.service

import com.kiryusha.myapplication.BuildConfig
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsApiService {
    @GET("top-headlines")
    suspend fun getTopHeadlines(
        @Query("country") country: String = "us",
        @Query("apiKey") apiKey: String = BuildConfig.NEWSAPI_API_KEY,
        ): Response<NewsResponse>

    @GET("everything")
    suspend fun searchNews(
        @Query("q") query: String,
        @Query("apiKey") apiKey: String = BuildConfig.NEWSAPI_API_KEY
    ): Response<NewsResponse>
}