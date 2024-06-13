package com.example.volcanoes

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import com.google.android.gms.maps.model.Marker

class MyViewModel(
    application: Application,
    val repository: Repository,
    val savedStateHandle: SavedStateHandle
) :
    AndroidViewModel(application) {

    val list: LiveData<List<Volcano>>
        get() = listOfVolcanoes
    private var listOfVolcanoes = MutableLiveData<List<Volcano>>()

    val navigateToDetails: LiveData<Navigation>
        get() = _navigationToDetails
    var _navigationToDetails = MutableLiveData<Navigation>()

    val loading: LiveData<Boolean>
        get() = _loading
    var _loading = MutableLiveData<Boolean>()

    val currentDescription: LiveData<String>
        get() = _currentDescription
    var _currentDescription = MutableLiveData<String>()
    var superVolcanoUrl = "superVolcano has just been made."

    init {
        listOfVolcanoes.value = repository.list
    }

    private var details = ""
    private var navigatingBackToText = false

    fun onMarkerClicked(marker: Marker) {
        val volcano = getVolcano(marker)!!
        _navigationToDetails.value = Navigation(
            volcano,
            true
        )
        _loading.value = true
        repository.getDetails(volcano.url) { result ->
            _currentDescription.postValue(result)
        }
    }

    private fun getVolcano(marker: Marker): Volcano? {
        for (volcano in list.value!!) {
            if (volcano.name == marker.title)
                return volcano
        }
        return null
    }

    fun getVolcanoInList(index: Int): Volcano {
        val volcano = list.value!![index]
        _loading.value = true
        superVolcanoUrl = volcano.url
        repository.getDetails(volcano.url) { result ->
            _currentDescription.value = result
            _loading.postValue(false)
        }
        return volcano
    }
}
