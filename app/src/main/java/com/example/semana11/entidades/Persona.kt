package com.example.semana11.entidades

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "personas")
data class Persona(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0, //Autoincrementable
    var nombre: String,
    var numeros: String,
    var imagenes: String,
    var sincronizado: Boolean,  // Campo para sincronización
    var ubicacion: String
) :Serializable

