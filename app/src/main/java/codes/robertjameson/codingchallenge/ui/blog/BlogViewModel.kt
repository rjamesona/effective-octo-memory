package codes.robertjameson.codingchallenge.ui.blog

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import codes.robertjameson.codingchallenge.model.Article
import codes.robertjameson.codingchallenge.utils.iterator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL

class BlogViewModel : ViewModel() {

    private val _articles = MutableLiveData<List<Article>>().apply {
        var articles: List<Article>
        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.IO) {
                articles = fetchArticles()
            }
            value = articles
        }
    }
    val articles: MutableLiveData<List<Article>> = _articles

    // Load articles from URL and into local data objects
    private fun fetchArticles(): List<Article> {
        val articleArray = mutableListOf<Article>()
        val url = URL("https://www.beenverified.com/articles/index.mobile-android.json")
        val jsonObject = JSONObject(url.readText())
        val jsonArray = jsonObject.getJSONArray("articles")
        for (article: JSONObject in jsonArray) {
            articleArray.add(
                Article(
                    article.get("article_date").toString(),
                    article.get("author").toString(),
                    article.get("description").toString(),
                    article.get("image").toString(),
                    article.get("link").toString(),
                    article.get("title").toString(),
                    article.get("uuid").toString()
                )
            )
        }
        return articleArray
    }
}