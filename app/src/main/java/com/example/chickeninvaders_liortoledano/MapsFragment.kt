package com.example.chickeninvaders_liortoledano

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.chickeninvaders_liortoledano.utilities.ScoreManager

class MapsFragment : Fragment(), OnMapReadyCallback {

    private lateinit var mapView: MapView
    private var googleMap: GoogleMap? = null
    private lateinit var scoreManager: ScoreManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_maps, container, false)

        mapView = view.findViewById(R.id.maps_MAP_view)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        return view
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        scoreManager = ScoreManager(requireContext())

        setupMap()
        loadAllScores()
    }

    private fun setupMap() {
        googleMap?.let { map ->
            // Default location (Or Yehuda)
            val defaultLocation = LatLng(31.8969, 34.8186)
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 10f))
        }
    }

    private fun loadAllScores() {
        val scores = scoreManager.getTopScores()
        scores.forEach { score ->
            addScoreMarker(score)
        }
    }

    private fun addScoreMarker(score: Score) {
        googleMap?.let { map ->
            val position = LatLng(score.latitude, score.longitude)
            map.addMarker(
                MarkerOptions()
                    .position(position)
                    .title(score.playerName)
                    .snippet("Score: ${score.score}")
            )
        }
    }

    fun showScoreLocation(score: Score) {
        googleMap?.let { map ->
            val position = LatLng(score.latitude, score.longitude)
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(position, 15f))
        }
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }
}