package com.example.semana11

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.example.semana11.adapters.PersonasAdapter
import com.example.semana11.entidades.Persona
import com.example.semana11.retrofit.Retrofit
import com.example.semana11.roomDB.RoomDB
import com.example.semana11.services.PersonaService
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.example.semana11.verificarRed.NetworkUtils
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices

private val REQUEST_CODE = 123

class ListaPersonasActivity : AppCompatActivity() {

    private var personasList: MutableList<Persona> = mutableListOf()
    private lateinit var adapter: PersonasAdapter
    private lateinit var rvLista: RecyclerView
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var databaseRoom: RoomDB? = null
    private var personaService: PersonaService? = null
    private val handler = Handler()

    //
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lista_personas)

        adapter = PersonasAdapter(this, personasList)
        rvLista = findViewById<RecyclerView>(R.id.rvListaSimple)
        rvLista.layoutManager = LinearLayoutManager(this)
        rvLista.adapter = adapter

        databaseRoom = Room.databaseBuilder(applicationContext, RoomDB::class.java, "app-database").build()
        personaService = Retrofit.getRestEngine().create(PersonaService::class.java)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        loadPersonas()

        val nombreEditText = findViewById<EditText>(R.id.nombreEditText)
        val numeroEditText = findViewById<EditText>(R.id.numeroEditText)
        val imagenesEditText = findViewById<EditText>(R.id.imagenesEditText)
        val btnObtenerUbicacion = findViewById<Button>(R.id.btnObtenerUbicacion)
        val editUbicacion = findViewById<EditText>(R.id.editUbicacion)
        val btnAgregar = findViewById<Button>(R.id.btnAgregar)

        btnObtenerUbicacion.setOnClickListener {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                // Solicitar actualizaciones de ubicación en tiempo real
                locationRequest = LocationRequest.create().apply {
                    priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                }

                // Inicializar el callback de ubicación
                locationCallback = object : LocationCallback() {
                    override fun onLocationResult(locationResult: LocationResult) {
                        val location = locationResult.lastLocation
                        // Actualizar la ubicación en los EditText
                        val latitude = location?.latitude
                        val longitude = location?.longitude
                        // Mostrar la latitud y longitud en los EditText
                        editUbicacion.setText("Lat: $latitude, Lon: $longitude")
                        // Detener las actualizaciones después de obtener la ubicación
                        fusedLocationClient.removeLocationUpdates(locationCallback)
                    }
                }

                // Solicitar actualizaciones de ubicación en tiempo real
                fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
            } else {
                // Si no se han otorgado permisos, solicítalos al usuario
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_CODE)
            }
        }

        btnAgregar.setOnClickListener {
            val nombre = nombreEditText.text.toString()
            val numero = numeroEditText.text.toString()
            val imagen = imagenesEditText.text.toString()
            val ubicacion = editUbicacion.text.toString()

            val nuevoContacto = Persona(nombre = nombre, numeros = numero, imagenes = imagen, sincronizado = false, ubicacion = ubicacion)


            personasList.add(nuevoContacto)
            adapter.notifyDataSetChanged()

            nombreEditText.text.clear()
            numeroEditText.text.clear()
            imagenesEditText.text.clear()
            editUbicacion.text.clear()

            insertPersona(databaseRoom, nuevoContacto)
        }

    }

    override fun onResume() {
        super.onResume()
        handler.post(syncRunnable)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(syncRunnable)
    }

    private val syncRunnable: Runnable = object : Runnable {
        override fun run() {
            if (NetworkUtils.isNetworkAvailable(this@ListaPersonasActivity)) {
                lifecycleScope.launch {
                    val personasNoSincronizadas = databaseRoom?.personaDao()?.getPersonasNoSincronizadas() ?: emptyList()
                    if (personasNoSincronizadas.isNotEmpty()) {
                        for (persona in personasNoSincronizadas.orEmpty()) {
                            persona.sincronizado = true
                            val call = personaService?.createPersona(persona)
                            if (call != null) {
                                call.enqueue(object : Callback<Persona> {
                                    override fun onResponse(call: Call<Persona>, response: Response<Persona>) {
                                        if (response.isSuccessful) {
                                            persona.sincronizado = true
                                            lifecycleScope.launch {
                                                databaseRoom?.personaDao()?.marcarComoSincronizado(persona)
                                            }
                                        }
                                    }

                                    override fun onFailure(call: Call<Persona>, t: Throwable) {
                                        // Manejar errores
                                    }
                                })
                            }
                        }
                    }
                }
            }
            handler.postDelayed(this, 15 * 60 * 1000) // Verificar cada 15 minutos
        }
    }

    fun getAllPersonas(room: RoomDB?) {
        room?.let {
            lifecycleScope.launch {
                val personas: List<Persona> = it.personaDao().getAllPersonas()
                val personasMutable: MutableList<Persona> = personas.toMutableList()
                adapter.updateData(personasMutable)
                rvLista.adapter = adapter
            }
        }
    }

    fun insertPersona(room: RoomDB?, persona: Persona) {
        room?.let {
            lifecycleScope.launch {
                it.personaDao().insertPersona(persona)
                getAllPersonas(it)
            }
        }
    }

    fun Context.loadPersonas() {
        if (NetworkUtils.isNetworkAvailable(this)) {
            // Cargar lista de Retrofit
            loadRetrofitPersonas()
        } else {
            // Cargar lista de Room
            loadRoomPersonas()
        }
    }

    private fun loadRetrofitPersonas() {
        val callGet = personaService?.getAllPersona()
        callGet?.enqueue(object : Callback<List<Persona>> {
            override fun onFailure(call: Call<List<Persona>>, t: Throwable) {
                Toast.makeText(this@ListaPersonasActivity, "Error Get All", Toast.LENGTH_LONG).show()
            }

            override fun onResponse(call: Call<List<Persona>>, response: Response<List<Persona>>) {
                if (response.isSuccessful) {
                    val personas = response.body()
                    if (personas != null) {
                        // Actualiza la lista de personas en el adaptador
                        adapter.updateData(personas)
                        Toast.makeText(this@ListaPersonasActivity, "OK Get All", Toast.LENGTH_LONG).show()
                    }
                }
            }
        })
    }

    private fun loadRoomPersonas() {
        getAllPersonas(databaseRoom)
    }

}