package com.example.semana11.services

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.semana11.entidades.Persona

@Dao
interface PersonaDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPersona(persona: Persona)

    @Query("SELECT * FROM personas")
    suspend fun getAllPersonas(): List<Persona>

    @Query("SELECT * FROM personas WHERE sincronizado = 0")
    suspend fun getPersonasNoSincronizadas(): List<Persona>

    @Update
    suspend fun marcarComoSincronizado(persona: Persona)

    @Update
    suspend fun updatePersona(persona: Persona)

    @Query("DELETE FROM personas WHERE id = :personaId")
    suspend fun deletePersona(personaId: Long)
}
