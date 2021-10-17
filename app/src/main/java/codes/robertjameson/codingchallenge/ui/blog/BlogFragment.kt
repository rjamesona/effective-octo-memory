package codes.robertjameson.codingchallenge.ui.blog

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import codes.robertjameson.codingchallenge.databinding.FragmentBlogBinding
import codes.robertjameson.codingchallenge.utils.ArticleAdapter


class BlogFragment : Fragment() {

    private lateinit var blogViewModel: BlogViewModel
    private var _binding: FragmentBlogBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        blogViewModel =
            ViewModelProvider(this).get(BlogViewModel::class.java)

        _binding = FragmentBlogBinding.inflate(inflater, container, false)
        val root: View = binding.root
        blogViewModel.articles.observe(viewLifecycleOwner, {
            val articles = binding.articleItems
            articles.adapter = ArticleAdapter(requireContext(), it)
            articles.layoutManager = LinearLayoutManager(context)
        })
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}