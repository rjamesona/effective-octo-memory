package codes.robertjameson.codingchallenge.ui.article

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import codes.robertjameson.codingchallenge.databinding.FragmentArticleBinding
import codes.robertjameson.codingchallenge.model.Article

class ArticleFragment : Fragment() {

    private lateinit var articleViewModel: ArticleViewModel
    private var _binding: FragmentArticleBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        articleViewModel =
            ViewModelProvider(this).get(ArticleViewModel::class.java)

        _binding = FragmentArticleBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Load the article into a webview fragment inside the app
        val webViewArticle: WebView = binding.webViewArticle
        articleViewModel.text.observe(viewLifecycleOwner, {
            val article = arguments?.get("article") as Article
            webViewArticle.webViewClient = WebViewClient()
            webViewArticle.loadUrl(article.link)
        })
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}