package com.example.volcanoes

import android.app.Application
import androidx.lifecycle.*

class DetailViewModel(
    application: Application,
    val repository: Repository,
    val savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    val text: LiveData<String>
        get() = _text
    private var _text = MutableLiveData<String>()

    val loading: LiveData<Boolean>
        get() = _loading
    private var _loading = MutableLiveData<Boolean>()

    fun getText(url: String) {
        val string = savedStateHandle.get<String>("text")
        if (string == null) {
            _loading.value = true
            repository.getDetails(url) { result ->
                _text.value = result
                _loading.value = false
            }
        } else {
            _text.value = text.value
        }
    }

    override fun onCleared() {
        super.onCleared()
        savedStateHandle.set("text", text.value)
    }
}