package com.kiryusha.myapplication.service

data class NewsResponse (
    val status: String,
    val totalResults: Int,
    val articles: List<Article>
)