package com.kiryusha.myapplication

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.Toast
import android.widget.Toolbar
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupViews()
        setupRecyclerView()
        setupSearch()
        loadNews()
    }

    private fun setupViews() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar as androidx.appcompat.widget.Toolbar?)

        recyclerView = findViewById(R.id.recyclerView)
        searchEditText = findViewById(R.id.searchEditText)
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)

        newsRepository = NewsRepository()

        swipeRefreshLayout.setOnRefreshListener {
            loadNews()
        }
    }

    private fun setupRecyclerView() {
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
                    performSearch(query)
                }
                true
            } else {
                false
            }
        }
    }

    private fun loadNews() {
        lifecycleScope.launch {
            try {
                swipeRefreshLayout.isRefreshing = true
                val articles = newsRepository.getTopHeadlines()
                newsAdapter.updateArticles(articles)
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Error loading news: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                swipeRefreshLayout.isRefreshing = false
            }
        }
    }

    private fun performSearch(query: String) {
        lifecycleScope.launch {
            try {
                swipeRefreshLayout.isRefreshing = true
                val articles = newsRepository.searchNews(query)
                newsAdapter.updateArticles(articles)
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Error searching news: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                swipeRefreshLayout.isRefreshing = false
            }
        }
    }

    override fun onItemClick(article: Article) {
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