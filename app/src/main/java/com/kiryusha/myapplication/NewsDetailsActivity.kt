package com.kiryusha.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.bumptech.glide.Glide
import com.kiryusha.myapplication.service.Article
import androidx.core.net.toUri

class NewsDetailsActivity : AppCompatActivity(){
    companion object{
        const val EXTRA_ARTICLE = "extra_article"
    }

    private lateinit var article: Article

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_news_details)

        setupToolbar()

        article = intent.getParcelableExtra<Article>(EXTRA_ARTICLE) ?: run {
            Toast.makeText(this, "Error loading article", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        displayArticleDetails()
        setupButtons()
    }

    private fun setupToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Article Details"

        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun displayArticleDetails() {
        val titleTextView = findViewById<TextView>(R.id.titleTextView)
        val descriptionTextView = findViewById<TextView>(R.id.descriptionTextView)
        val sourceTextView = findViewById<TextView>(R.id.sourceTextView)
        val publishedAtTextView = findViewById<TextView>(R.id.publishedAtTextView)
        val imageView = findViewById<ImageView>(R.id.imageView)

        titleTextView.text = article.title ?: "No title"
        descriptionTextView.text = article.description ?: "No description available"
        sourceTextView.text = "Source: ${article.source?.name ?: "Unknown"}"
        publishedAtTextView.text = "Published: ${formatDate(article.publishedAt)}"

        // Load image with Glide
        Glide.with(this)
            .load(article.imageUrl)
            .placeholder(R.drawable.placeholder)
            .error(R.drawable.placeholder)
            .into(imageView)
    }

    private fun setupButtons() {
        val shareButton = findViewById<Button>(R.id.shareButton)
        val bookmarkButton = findViewById<Button>(R.id.bookmarkButton)
        val openButton = findViewById<Button>(R.id.openButton)

        shareButton.setOnClickListener {
            shareArticle()
        }

        bookmarkButton.setOnClickListener {
            bookmarkArticle()
        }

        openButton.setOnClickListener {
            openArticleInBrowser()
        }
    }

    private fun shareArticle() {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, article.title)
            putExtra(Intent.EXTRA_TEXT, "${article.title}\n\n${article.url}")
        }
        startActivity(Intent.createChooser(shareIntent, "Поделиться новостью с помощью..."))
    }

    private fun bookmarkArticle() {
        Toast.makeText(this, "Новость помещена в закладки!", Toast.LENGTH_SHORT).show()
    }

    private fun openArticleInBrowser() {
        article.url?.let { url ->
            val intent = Intent(Intent.ACTION_VIEW, url.toUri())
            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            } else {
                Toast.makeText(this, "Браузер не найден", Toast.LENGTH_SHORT).show()
            }
        } ?: Toast.makeText(this, "No URL available", Toast.LENGTH_SHORT).show()
    }

    private fun formatDate(dateString: String?): String {
        return dateString?.let {
            try {
                it.substring(0, 10)
            } catch (e: Exception) {
                "Unknown date"
            }
        } ?: "Unknown date"
    }

}