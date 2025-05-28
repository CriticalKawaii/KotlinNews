package com.kiryusha.myapplication.clicklisteners

import com.kiryusha.myapplication.service.Article

interface NewsItemClickListener {
    fun onItemClick(article: Article)
}