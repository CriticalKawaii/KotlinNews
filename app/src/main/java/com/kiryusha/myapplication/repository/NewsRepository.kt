package com.kiryusha.myapplication.repository

import android.util.Log
import com.kiryusha.myapplication.BuildConfig
import com.kiryusha.myapplication.exceptions.ApiException
import com.kiryusha.myapplication.service.Article
import com.kiryusha.myapplication.service.NewsApiService
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class NewsRepository {
    private val apiService: NewsApiService

    companion object {
        private const val TAG = "NewsRepository"
        private const val BASE_URL = "https://newsapi.org/v2/"
        private const val USER_AGENT = "NewsApp/1.0"
    }

    init {
        Log.d(TAG, "Initializing NewsRepository with server-like headers")
        Log.d(TAG, "Base URL: $BASE_URL")
        Log.d(TAG, "API Key: ${BuildConfig.NEWSAPI_API_KEY.take(10)}...")

        val loggingInterceptor = HttpLoggingInterceptor { message ->
            Log.d("$TAG-HTTP", message)
        }.apply { level = HttpLoggingInterceptor.Level.BODY }

        val headerInterceptor = Interceptor { chain ->
            val originalRequest = chain.request()
            val newRequest = originalRequest.newBuilder()
                .addHeader("User-Agent", USER_AGENT)
                .addHeader("Accept", "application/json")
                .build()

            Log.d(TAG, "Making request to: ${newRequest.url}")
            Log.d(TAG, "Request headers: ${newRequest.headers}")

            val response = chain.proceed(newRequest)
            Log.d(TAG, "Response code: ${response.code}")

            response
        }

        val responseInterceptor = Interceptor { chain ->
            val response = chain.proceed(chain.request())
            val responseBody = response.body
            val responseBodyString = responseBody?.string() ?: ""

            Log.d(TAG, "Response body length: ${responseBodyString.length}")
            Log.d(TAG, "Response body preview: ${responseBodyString.take(200)}...")

            if (responseBodyString.contains("corsNotAllowed")) {
                Log.e(TAG, "CORS ERROR: NewsAPI Developer plan blocks non-localhost requests")
            } else if (responseBodyString.startsWith("{") && responseBodyString.contains("articles")) {
                Log.d(TAG, "Got valid JSON response with articles")
            }

            response.newBuilder()
                .body(okhttp3.ResponseBody.create(responseBody?.contentType(), responseBodyString))
                .build()
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(headerInterceptor)
            .addInterceptor(responseInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .followRedirects(false)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiService = retrofit.create(NewsApiService::class.java)
    }

    suspend fun getTopHeadlines(): List<Article> {
        return try {
            Log.d(TAG, "Making server-like API call...")
            val response = apiService.getTopHeadlines()

            Log.d(TAG, "Response received - Code: ${response.code()}")

            if (response.isSuccessful) {
                val body = response.body()
                val articles = body?.articles?.filterNotNull() ?: emptyList()
                Log.d(TAG, "Successfully loaded ${articles.size} articles")

                articles.forEachIndexed { index, article ->
                    Log.d(TAG, "Article $index: ${article.title?.take(50)}...")
                }

                articles
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "API Error - Code: ${response.code()}")
                Log.e(TAG, "Error details: $errorBody")

                // Provide specific error message for CORS
                if (errorBody?.contains("corsNotAllowed") == true) {
                    throw ApiException(response.code(), "NewsAPI Developer plan only allows localhost requests. Upgrade plan or use alternative API.")
                } else {
                    throw ApiException(response.code(), "API Error: ${response.message()}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception: ${e.message}", e)
            throw ApiException(0, e.message ?: "Unknown error")
        }
    }

    suspend fun searchNews(query: String): List<Article> {
        return try {
            Log.d(TAG, "Searching with server-like headers: $query")
            val response = apiService.searchNews(query)

            if (response.isSuccessful) {
                val articles = response.body()?.articles?.filterNotNull() ?: emptyList()
                Log.d(TAG, "Search results: ${articles.size} articles")
                articles
            } else {
                val errorBody = response.errorBody()?.string()
                if (errorBody?.contains("corsNotAllowed") == true) {
                    throw ApiException(response.code(), "Search blocked by CORS policy")
                } else {
                    throw ApiException(response.code(), "Search error: ${response.message()}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Search exception: ${e.message}", e)
            throw ApiException(0, e.message ?: "Unknown error")
        }
    }
}