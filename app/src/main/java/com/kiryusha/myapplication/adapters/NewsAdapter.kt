package com.kiryusha.myapplication.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kiryusha.myapplication.R
import com.kiryusha.myapplication.clicklisteners.NewsItemClickListener
import com.kiryusha.myapplication.service.Article

class NewsAdapter(private var articles: List<Article>,
                  private val clickListener: NewsItemClickListener
    ) : RecyclerView.Adapter<NewsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_article, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val article = articles[position]

        holder.titleTextView.text = article.title ?: "No title"
        holder.descriptionTextView.text = article.description ?: "No description"
        holder.sourceTextView.text = article.source?.name ?: "Unknown source"

        Glide.with(holder.itemView.context)
            .load(article.imageUrl)
            .placeholder(R.drawable.placeholder)
            .error(R.drawable.placeholder)
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(holder.imageView)

        holder.itemView.setOnClickListener {
            clickListener.onItemClick(article)
        }
    }

    override fun getItemCount(): Int = articles.size

    fun updateArticles(newArticles: List<Article>) {
        articles = newArticles
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
        val descriptionTextView: TextView = itemView.findViewById(R.id.descriptionTextView)
        val sourceTextView: TextView = itemView.findViewById(R.id.sourceTextView)
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
    }
}