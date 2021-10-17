package codes.robertjameson.codingchallenge.ui.article

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ArticleViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is an article Fragment"
    }
    val text: LiveData<String> = _text
}