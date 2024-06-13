package com.example.volcanoes

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.IntentSender
import android.content.SharedPreferences
import android.os.Build
import androidx.fragment.app.Fragment

import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import org.koin.android.ext.android.inject

class MapFragment : Fragment() {

    companion object {
        const val REQUEST_CHECK_SETTINGS = 1
    }

    private lateinit var map: GoogleMap
    private lateinit var toolbar: Toolbar
    private lateinit var factory: Factory
    private lateinit var viewModel: MyViewModel
    private val savedStateHandle = SavedStateHandle()
    private lateinit var locationRequest: LocationRequest

    private var permissionCallback: ((Boolean) -> Unit)? = null

    @RequiresApi(Build.VERSION_CODES.N)
    private val locationPermissionRequest =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            when {
                permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                    permissionCallback!!.invoke(true)
                }
                permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                    permissionCallback!!.invoke(true)
                }
                else -> {
                    permissionCallback!!.invoke(false)
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_map2, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpToolbar()
        factory = Factory(requireActivity().application, Repository(requireActivity().application), savedStateHandle)
        viewModel = ViewModelProvider(this, factory).get(MyViewModel::class.java)
        getPosition(activity?.getPreferences(Context.MODE_PRIVATE)) { position ->
            val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
            mapFragment?.getMapAsync { map ->
                this.map = map
                viewModel.list.observe(viewLifecycleOwner) { list ->
                    map.moveCamera(CameraUpdateFactory.newLatLng(position))
                    setMarkers(map, list)
                    map.setOnMarkerClickListener { marker ->
                        viewModel.onMarkerClicked(marker)
                        true
                    }
                }
                viewModel.navigateToDetails.observe(viewLifecycleOwner) { navigation ->
                    if (navigation.navigate) {
                        viewModel._navigationToDetails.value!!.navigate = false
                        val bundle = Bundle()
                        bundle.putString("name", navigation.volcano.name)
                        bundle.putInt("height", navigation.volcano.height)
                        bundle.putString("url", navigation.volcano.url)
                        bundle.putString("imageUrl", navigation.volcano.imageUrl)
                        findNavController().navigate(R.id.action_map_to_details, bundle)
                    }
                }
            }
        }
    }

    private fun setUpToolbar() {
        setHasOptionsMenu(true)
        toolbar = requireView().findViewById(R.id.map_toolbar)
        toolbar.setupWithNavController(findNavController())
        toolbar.inflateMenu(R.menu.menu)
        toolbar.menu.findItem(R.id.switchBetweenMapAndList).title = "List"
        toolbar.setOnMenuItemClickListener {
            findNavController().navigate(R.id.action_map_to_list)
            true
        }
    }

    private fun setMarkers(map: GoogleMap, list: List<Volcano>) {
        for (volcano in list) {
            volcano.marker = map.addMarker(
                MarkerOptions()
                    .position(volcano.place)
                    .title(volcano.name)
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        savePosition(map.cameraPosition.target)
    }

    private fun savePosition(position: LatLng) {
        val sharedPreferences = activity?.getPreferences(Context.MODE_PRIVATE)
        sharedPreferences?.edit()?.apply {
            putFloat("latitude", position.latitude.toFloat())
            putFloat("longitude", position.longitude.toFloat())
            apply()
        }
    }

    private fun getPosition(sharedPreferences: SharedPreferences?, callback: (LatLng) -> Unit) {
        val latitude = sharedPreferences?.getFloat("latitude", 200f)!!.toDouble()
        val longitude = sharedPreferences.getFloat("longitude", 200f).toDouble()
        if (latitude != 200.0) {
            callback.invoke(LatLng(latitude, longitude))
        } else {
            getPermission { granted ->
                if (granted) {
                    checkSettings { on ->
                        if (on) {
                            getLocation(callback)
                        } else {
                            Snackbar.make(
                                requireView(),
                                getString(R.string.location_is_off),
                                Snackbar.LENGTH_LONG
                            ).show()
                            callback.invoke(LatLng(0.0, 0.0))
                        }
                    }
                } else {
                    Snackbar.make(
                        requireView(),
                        getString(R.string.permission_denied),
                        Snackbar.LENGTH_LONG
                    ).show()
                    callback.invoke(LatLng(0.0, 0.0))
                }
            }
        }
    }

    private fun getPermission(callback: (Boolean) -> Unit) {
        permissionCallback = callback
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            locationPermissionRequest.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    @SuppressLint("MissingPermission")
    private fun getLocation(callback: (LatLng) -> Unit) {
        val client = LocationServices.getFusedLocationProviderClient(requireActivity())
        client.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                callback.invoke(LatLng(location.latitude, location.longitude))
            } else {
                client.requestLocationUpdates(
                    locationRequest,
                    object: LocationCallback() {
                        override fun onLocationResult(locationResult: LocationResult) {
                            var theSingleUpdatedLocation = LatLng(0.0, 0.0)
                            for (updatedLocation in locationResult.locations) {
                                if (updatedLocation != null) {
                                        theSingleUpdatedLocation = LatLng(updatedLocation.latitude, updatedLocation.longitude)
                                    }
                                callback.invoke(LatLng(updatedLocation.latitude, updatedLocation.longitude))
                            }
                        }
                    },
                    Looper.getMainLooper()
                )
            }
        }
            .addOnFailureListener {
                Snackbar.make(
                    requireView(),
                    getString(R.string.location_technical_error),
                    Snackbar.LENGTH_LONG
                ).show()
                callback.invoke(LatLng(0.0, 0.0))
            }
    }

    private fun checkSettings(callback: (Boolean) -> Unit) {
        (requireActivity() as MainActivity).settingsCallback = callback
        locationRequest = LocationRequest.create()
        LocationServices.getSettingsClient(requireActivity())
            .checkLocationSettings(
                LocationSettingsRequest.Builder()
                    .addLocationRequest(locationRequest)
                    .build()
            )
            .addOnSuccessListener { status ->
                callback.invoke(status.locationSettingsStates?.isLocationUsable == true)
            }
            .addOnFailureListener { exception ->
                callback.invoke(false)
            }
    }
}