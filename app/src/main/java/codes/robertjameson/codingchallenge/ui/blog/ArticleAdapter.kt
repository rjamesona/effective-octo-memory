package codes.robertjameson.codingchallenge.ui.blog

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.drawable.toBitmap
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import codes.robertjameson.codingchallenge.R
import codes.robertjameson.codingchallenge.model.Article
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL

@Suppress("BlockingMethodInNonBlockingContext")
class ArticleAdapter(private val context: Context, private val articles: List<Article>) :
    RecyclerView.Adapter<ArticleAdapter.ViewHolder>() {

    // Going to use a recycler view to reuse the same list items in the UI to save resources
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val articleTitle: TextView = itemView.findViewById(R.id.articleTitle)
        val imageView: ImageView = itemView.findViewById(R.id.articleImage)
        val articleDescription: TextView = itemView.findViewById(R.id.articleDescription)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val context = parent.context
        val inflater = LayoutInflater.from(context)
        val articleView = inflater.inflate(R.layout.fragment_article_item, parent, false)
        val holder = ViewHolder(articleView)

        holder.itemView.setOnClickListener { view ->
            if (holder.adapterPosition != RecyclerView.NO_POSITION) {

                val bundle = Bundle()
                bundle.putSerializable("article", articles[holder.adapterPosition])

                view.findNavController().navigate(
                    R.id.action_navigation_blog_to_navigation_article,
                    bundle
                )
            }

        }
        return holder
    }

    // Load new articles while scrolling thorough the list
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val article: Article = articles[position]
        val titleTextView = viewHolder.articleTitle
        titleTextView.text = article.title
        val descriptionTextView = viewHolder.articleDescription
        descriptionTextView.text = article.description
        // Fetch the article image
        CoroutineScope(Dispatchers.Main).launch {
            val imageView = viewHolder.imageView
            imageView.setImageBitmap(getThumbnail(article.image))
        }
    }

    // Get thumbnail version of article image
    private suspend fun getThumbnail(url: String): Bitmap {
        var bitmap: Bitmap
        try {
            withContext(Dispatchers.IO) {
                val newUrl = URL(
                    url.replace(
                        context.getString(
                            R.string.newValue
                        ),
                        context.getString(R.string.oldValue)
                    )
                )
                bitmap = BitmapFactory.decodeStream(newUrl.openConnection().getInputStream())
            }
        } catch (e: Exception) {
            // Get default image in case online image is not available
            Log.e("TAG", e.stackTraceToString())
            val drawable = AppCompatResources.getDrawable(
                context,
                R.drawable.ic_launcher_background
            )
            //
            bitmap = drawable!!.toBitmap(width = 60, height = 60, config = null)
        }
        return bitmap
    }

    // Returns the total count of items in the list
    override fun getItemCount(): Int {
        return articles.size
    }
}