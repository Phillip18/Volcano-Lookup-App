package com.example.volcanoes

import android.app.Application
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class Factory(
    val application: Application,
    val repository: Repository,
    val savedStateHandle: SavedStateHandle
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return modelClass.getConstructor(
            Application::class.java,
            Repository::class.java,
            SavedStateHandle::class.java
        ).newInstance(application, repository, savedStateHandle)
    }
}