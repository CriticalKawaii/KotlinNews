package com.kiryusha.myapplication.repository

import com.kiryusha.myapplication.BuildConfig
import com.kiryusha.myapplication.exceptions.ApiException
import com.kiryusha.myapplication.service.Article
import com.kiryusha.myapplication.service.NewsApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class NewsRepository {
    private val apiService: NewsApiService

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://newsapi.org/v2/top-headlines?country=us&apiKey=${BuildConfig.NEWSAPI_API_KEY}")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiService = retrofit.create(NewsApiService::class.java)
    }

    suspend fun getTopHeadlines(): List<Article> {
        return try {
            val response = apiService.getTopHeadlines()
            if (response.isSuccessful) {
                response.body()?.articles?.filterNotNull() ?: emptyList()
            } else {
                throw ApiException(response.code(), response.message())
            }
        } catch (e: Exception) {
            throw ApiException(0, e.message ?: "Unknown error")
        }
    }

    suspend fun searchNews(query: String): List<Article> {
        return try {
            val response = apiService.searchNews(query)
            if (response.isSuccessful) {
                response.body()?.articles?.filterNotNull() ?: emptyList()
            } else {
                throw ApiException(response.code(), response.message())
            }
        } catch (e: Exception) {
            throw ApiException(0, e.message ?: "Unknown error")
        }
    }

}