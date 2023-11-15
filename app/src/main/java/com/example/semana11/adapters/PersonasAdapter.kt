package com.example.semana11.adapters

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.example.semana11.EditarActivity
import com.example.semana11.entidades.Persona
import com.example.semana11.R
import com.example.semana11.maps.MapsActivity
import com.example.semana11.retrofit.Retrofit
import com.example.semana11.roomDB.RoomDB
import com.example.semana11.services.PersonaService
import com.squareup.picasso.Picasso
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// Define una constante para identificar la solicitud de permisos.
private val MY_PERMISSIONS_REQUEST_CALL_PHONE = 123

class PersonasAdapter(private val context: Context, private val items: MutableList<Persona>) :
    RecyclerView.Adapter<PersonasAdapter.PersonaViewHolder>() {

    private val adapterScope = CoroutineScope(Dispatchers.Default)
    private var databaseRoom: RoomDB? = null
    private var personaService: PersonaService? = null

    // Agrega un método para actualizar la lista de personas
    fun updateData(newItems: List<Persona>) {
        (context as Activity).runOnUiThread {
            items.clear()
            items.addAll(newItems)
            notifyDataSetChanged()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PersonaViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.activity_personas_adapter, parent, false)
        return PersonaViewHolder(view)
        databaseRoom = Room.databaseBuilder(context.applicationContext, RoomDB::class.java, "app-database").build()
        personaService = Retrofit.getRestEngine().create(PersonaService::class.java)
    }

    override fun onBindViewHolder(holder: PersonaViewHolder, position: Int) {
        val item = items[position]
        val view = holder.itemView

        val tvName = view.findViewById<TextView>(R.id.tvName)
        val tvCelular = view.findViewById<TextView>(R.id.tvCelular)
        val imageView = view.findViewById<ImageView>(R.id.imageView)
        tvName.text = item.nombre
        tvCelular.text = item.numeros
        Picasso.get().load(item.imagenes).into(imageView)

        // Llamada
        val btnLlamar = holder.itemView.findViewById<Button>(R.id.btnLlamar)
        btnLlamar.setOnClickListener {
            val numeroTelefono = tvCelular.text.toString()

            if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                // Si no tienes permiso, solicítalo al usuario.
                ActivityCompat.requestPermissions(
                    context as Activity,
                    arrayOf(android.Manifest.permission.CALL_PHONE),
                    MY_PERMISSIONS_REQUEST_CALL_PHONE
                )
            } else {
                // Si ya tienes permiso, realiza la llamada.
                val intent = Intent(Intent.ACTION_CALL)
                intent.data = Uri.parse("tel:$numeroTelefono")
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                view.context.startActivity(intent)
            }
        }

        // Editar
        val btnEditar = holder.itemView.findViewById<ImageButton>(R.id.btnEditar)
        btnEditar.setOnClickListener {
            val persona = items[position]
            val intent = Intent(context, EditarActivity::class.java)
            intent.putExtra(EditarActivity.EXTRA_PERSONA, persona)
            context.startActivity(intent)
        }

        // Eliminar
        val btnEliminar = holder.itemView.findViewById<ImageButton>(R.id.btnEliminar)
        btnEliminar.setOnClickListener {
            val persona = items[position]
            val personaId = persona.id
            val builder = AlertDialog.Builder(context)
            builder.setTitle("Eliminar")
                .setMessage("¿Deseas eliminar a ${persona.nombre}?")
                .setPositiveButton("Eliminar") { _, _ ->
                    // Llamamos al método de eliminación en Room
                    adapterScope.launch {
                        databaseRoom?.personaDao()?.deletePersona(personaId)
                    }

                    // Llamamos al método de eliminación en Retrofit
                    eliminarPersonaEnRetrofit(personaId)

                    items.remove(persona)
                    notifyItemRemoved(position)
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }

        // Ubicacion
        val btnUbicacion = holder.itemView.findViewById<ImageButton>(R.id.btnUbicacion)
        btnUbicacion.setOnClickListener {
            val persona = items[position]
            val ubicacion = persona.ubicacion

            if (ubicacion != null && ubicacion.isNotBlank()) {
                val coords: List<String>? = ubicacion.split(",").map { it.trim() }
                val latString: String? = coords?.get(0)?.substringAfter("Lat: ")
                val lngString: String? = coords?.get(1)?.substringAfter("Lon: ")

                val lat = latString?.toDoubleOrNull() ?: 0.0
                val lng = lngString?.toDoubleOrNull() ?: 0.0

                // Agrega mensajes de registro para depurar
                Log.d("PersonasAdapter", "Latitud: $lat, Longitud: $lng")

                val intent = Intent(context, MapsActivity::class.java)

                if (lat != 0.0 && lng != 0.0) {
                    intent.putExtra("ubicacion", "$lat, $lng")
                    context.startActivity(intent)
                } else {
                    // Manejar el caso donde las coordenadas no son válidas
                    Toast.makeText(context, "No se proporcionaron coordenadas válidas", Toast.LENGTH_SHORT).show()
                }
            } else {
                // Manejar el caso donde las coordenadas no están disponibles
                Toast.makeText(context, "No se proporcionaron coordenadas válidas", Toast.LENGTH_SHORT).show()
            }
        }



    }

    private fun eliminarPersonaEnRetrofit(personaId: Long) {
        val call = personaService?.deletePersona(personaId)
        if (call != null) {
            call?.enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        Toast.makeText(context, "Persona eliminada en Retrofit", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Error al eliminar en Retrofit", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Toast.makeText(context, "Error al eliminar en Retrofit", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    inner class PersonaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}
