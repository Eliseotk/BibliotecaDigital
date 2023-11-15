package com.example.semana11

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.room.Room
import com.example.semana11.entidades.Persona
import com.example.semana11.retrofit.Retrofit
import com.example.semana11.roomDB.RoomDB
import com.example.semana11.services.PersonaService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EditarActivity : AppCompatActivity() {

    private lateinit var nombreEditText: EditText
    private lateinit var numeroEditText: EditText
    private lateinit var imagenesEditText: EditText
    private lateinit var btnGuardar: Button

    private var personaService: PersonaService? = null
    private var persona: Persona? = null
    private var databaseRoom: RoomDB? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editar)

        nombreEditText = findViewById(R.id.editNombre)
        numeroEditText = findViewById(R.id.editNumero)
        imagenesEditText = findViewById(R.id.editImagenes)
        btnGuardar = findViewById(R.id.btnGuardarEdicion)

        // Inicializar Room
        databaseRoom = Room.databaseBuilder(applicationContext, RoomDB::class.java, "app-database").build()

        //Inicializar Retrofit
        personaService = Retrofit.getRestEngine().create(PersonaService::class.java)

        // Obtener la persona a editar desde el intent
        persona = intent.getSerializableExtra(EXTRA_PERSONA) as? Persona

        // Verificar si se recibió una persona válida
        if (persona != null) {
            mostrarDetallesPersona(persona!!)
        } else {
            // Manejar el caso en el que no se reciba una persona válida
            finish()
        }

        btnGuardar.setOnClickListener {
            guardarEdicionPersona()
            guardarEdicionRetrofit()
        }

    }

    private fun mostrarDetallesPersona(persona: Persona) {
        nombreEditText.setText(persona.nombre)
        numeroEditText.setText(persona.numeros)
        imagenesEditText.setText(persona.imagenes)
    }

    private fun guardarEdicionPersona() {
        val nombre = nombreEditText.text.toString()
        val numero = numeroEditText.text.toString()
        val imagenes = imagenesEditText.text.toString()

        // Verificar si se recibió una persona válida
        if (persona != null) {
            // Actualizar los datos de la persona
            persona?.nombre = nombre
            persona?.numeros = numero
            persona?.imagenes = imagenes

            // Actualizar en Room en un hilo de fondo
            GlobalScope.launch(Dispatchers.IO) {
                databaseRoom?.personaDao()?.updatePersona(persona!!)
            }

            // Cerrar la actividad
            finish()
        }
    }

    private fun guardarEdicionRetrofit() {

        // actualizar
        persona?.let {
            personaService?.updatePersona(it.id, persona!!)
                ?.enqueue(object : Callback<Persona> {
                    override fun onResponse(call: Call<Persona>, response: Response<Persona>) {
                        Toast.makeText(this@EditarActivity, "Editado Correctamente", Toast.LENGTH_LONG).show()
                    }

                    override fun onFailure(call: Call<Persona>, t: Throwable) {
                        Toast.makeText(this@EditarActivity, "No se edito", Toast.LENGTH_LONG).show()
                    }
                })
        }

    }

    companion object {
        const val EXTRA_PERSONA = "extra_persona"
    }

}