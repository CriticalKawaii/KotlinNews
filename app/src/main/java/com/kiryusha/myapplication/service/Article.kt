package com.kiryusha.myapplication.service

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Article(
    val title: String?,
    val description: String?,
    val url: String?,
    @SerializedName("urlToImage") val imageUrl: String?,
    val publishedAt: String?,
    val source: Source?
) : Parcelable