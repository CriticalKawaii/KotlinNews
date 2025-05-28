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
        val url = article.url
        if (url.isNullOrBlank()) {
            Toast.makeText(this, "Ссылка недоступна", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            if (url.contains("example.com") || url.contains("placeholder")) {
                Toast.makeText(this, "Это тестовая новость. Открываю поисковик с заголовком...", Toast.LENGTH_LONG).show()

                val searchQuery = article.title?.replace(" ", "+") ?: "news"
                val searchUrl = "https://www.google.com/search?q=$searchQuery"
                val searchIntent = Intent(Intent.ACTION_VIEW, searchUrl.toUri())

                if (searchIntent.resolveActivity(packageManager) != null) {
                    startActivity(searchIntent)
                } else {
                    Toast.makeText(this, "Браузер не найден", Toast.LENGTH_SHORT).show()
                }
                return
            }

            val uri = url.toUri()
            val intent = Intent(Intent.ACTION_VIEW, uri)

            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.addCategory(Intent.CATEGORY_BROWSABLE)

            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            } else {
                val browserIntent = Intent(Intent.ACTION_VIEW, uri).apply {
                    addCategory(Intent.CATEGORY_BROWSABLE)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }

                try {
                    startActivity(browserIntent)
                } catch (e: Exception) {
                    Toast.makeText(this, "Не удалось открыть ссылку. Попробуйте скопировать URL вручную.", Toast.LENGTH_LONG).show()
                }
            }

        } catch (e: Exception) {
            Toast.makeText(this, "Ошибка при открытии ссылки: ${e.message}", Toast.LENGTH_SHORT).show()
        }
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