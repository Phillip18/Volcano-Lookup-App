package com.example.volcanoes

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker

class Volcano(
    val name: String,
    val place: LatLng = LatLng(0.0, 0.0),
    val height: Int,
    val url: String,
    val imageUrl: String,
    var marker: Marker? = null
)