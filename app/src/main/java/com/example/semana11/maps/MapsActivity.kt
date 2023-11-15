package com.example.semana11.maps

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.semana11.R

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.semana11.databinding.ActivityMapsBinding

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding

    private var lat = 0.0
    private var lng= 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Obtener coordenadas del Intent
        val ubicacionString = intent.getStringExtra("ubicacion")
        if (ubicacionString != null) {
            // Obtener coordenadas del string
            val coords: List<String>? = ubicacionString.split(",").map { it.trim() }
            val latString: String? = coords?.getOrNull(0)?.substringAfter("Lat: ")
            val lngString: String? = coords?.getOrNull(1)?.substringBefore(" Lon: ")

            // Convertir a doubles si las cadenas no son nulas ni vacías
            lat = latString?.toDoubleOrNull() ?: 0.0
            lng = lngString?.toDoubleOrNull() ?: 0.0
        } else {
            // Manejar el caso donde no se proporcionó ninguna ubicación
            Toast.makeText(this, "No se proporcionó ninguna ubicación", Toast.LENGTH_SHORT).show()
            // Puedes cerrar la actividad si lo deseas
            finish()
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Verificar si las coordenadas son válidas
        if (lat != 0.0 && lng != 0.0) {
            // Agregar marker
            val position = LatLng(lat, lng)
            mMap.addMarker(MarkerOptions().position(position))

            // Establecer un nivel de zoom personalizado (puedes ajustar el valor según tu preferencia)
            val zoomLevel = 15.0f
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, zoomLevel))
        } else {
            // Manejar el caso donde las coordenadas no son válidas
            Toast.makeText(
                this,
                "No se proporcionaron coordenadas válidas. Mostrando ubicación predeterminada.",
                Toast.LENGTH_SHORT
            ).show()

            // Puedes establecer una ubicación predeterminada o simplemente no agregar ningún marcador.
            // A continuación, se muestra un ejemplo de cómo centrar el mapa en una ubicación predeterminada.
            val defaultLocation = LatLng(-34.0, 151.0)

            // Establecer un nivel de zoom para la ubicación predeterminada
            val zoomLevel = 10.0f
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, zoomLevel))
        }
    }

}
