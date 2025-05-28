package com.kiryusha.myapplication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.kiryusha.myapplication.adapters.NewsAdapter
import com.kiryusha.myapplication.clicklisteners.NewsItemClickListener
import com.kiryusha.myapplication.repository.NewsRepository
import com.kiryusha.myapplication.service.Article
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity(), NewsItemClickListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var newsAdapter: NewsAdapter
    private lateinit var newsRepository: NewsRepository
    private lateinit var searchEditText: EditText
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private var isDarkModeEnabled = false

    companion object {
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate called")
        setContentView(R.layout.activity_main)

        setupViews()
        setupRecyclerView()
        setupSearch()
        loadNews()
    }

    private fun setupViews() {
        Log.d(TAG, "Setting up views")
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        recyclerView = findViewById(R.id.recyclerView)
        searchEditText = findViewById(R.id.searchEditText)
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)

        newsRepository = NewsRepository()

        swipeRefreshLayout.setOnRefreshListener {
            Log.d(TAG, "Swipe refresh triggered")
            loadNews()
        }
    }

    private fun setupRecyclerView() {
        Log.d(TAG, "Setting up RecyclerView")
        newsAdapter = NewsAdapter(emptyList(), this)
        recyclerView.apply {
            adapter = newsAdapter
            layoutManager = LinearLayoutManager(this@MainActivity)
        }
    }

    private fun setupSearch() {
        searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = searchEditText.text.toString().trim()
                if (query.isNotEmpty()) {
                    Log.d(TAG, "Search triggered for: $query")
                    performSearch(query)
                }
                true
            } else {
                false
            }
        }
    }

    private fun loadNews() {
        Log.d(TAG, "loadNews called")
        lifecycleScope.launch {
            try {
                Log.d(TAG, "Starting to load news...")
                swipeRefreshLayout.isRefreshing = true

                val articles = newsRepository.getTopHeadlines()
                Log.d(TAG, "Successfully loaded ${articles.size} articles")

                newsAdapter.updateArticles(articles)

                if (articles.isEmpty()) {
                    Toast.makeText(this@MainActivity, "No articles found", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@MainActivity, "Loaded ${articles.size} articles", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                val errorMsg = "Error loading news: ${e.message}"
                Log.e(TAG, errorMsg, e)
                Toast.makeText(this@MainActivity, errorMsg, Toast.LENGTH_LONG).show()
            } finally {
                swipeRefreshLayout.isRefreshing = false
                Log.d(TAG, "loadNews completed")
            }
        }
    }

    private fun performSearch(query: String) {
        Log.d(TAG, "performSearch called with query: $query")
        lifecycleScope.launch {
            try {
                swipeRefreshLayout.isRefreshing = true
                val articles = newsRepository.searchNews(query)
                Log.d(TAG, "Search completed with ${articles.size} results")
                newsAdapter.updateArticles(articles)

                if (articles.isEmpty()) {
                    Toast.makeText(this@MainActivity, "No results found for '$query'", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                val errorMsg = "Error searching news: ${e.message}"
                Log.e(TAG, errorMsg, e)
                Toast.makeText(this@MainActivity, errorMsg, Toast.LENGTH_LONG).show()
            } finally {
                swipeRefreshLayout.isRefreshing = false
            }
        }
    }

    override fun onItemClick(article: Article) {
        Log.d(TAG, "Article clicked: ${article.title}")
        val intent = Intent(this, NewsDetailsActivity::class.java)
        intent.putExtra(NewsDetailsActivity.EXTRA_ARTICLE, article)
        startActivity(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        val darkModeMenuItem = menu.findItem(R.id.action_dark_mode)
        darkModeMenuItem.isChecked = isDarkModeEnabled
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_dark_mode -> {
                isDarkModeEnabled = !isDarkModeEnabled
                item.isChecked = isDarkModeEnabled
                setDarkModeEnabled(isDarkModeEnabled)
                true
            }
            R.id.action_refresh -> {
                Log.d(TAG, "Manual refresh triggered")
                loadNews()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setDarkModeEnabled(enabled: Boolean) {
        val mode = if (enabled) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        AppCompatDelegate.setDefaultNightMode(mode)
        recreate()
    }
}