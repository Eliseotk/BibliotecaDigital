package com.example.semana11.services

import com.example.semana11.entidades.Persona
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface PersonaService {
    @GET("personas")
    fun getAllPersona(): Call<List<Persona>>

    @POST("personas")
    fun createPersona(@Body persona: Persona): Call<Persona>

    @PUT("personas/{id}")
    fun updatePersona(@Path("id") id: Long, @Body persona: Persona): Call<Persona>

    @DELETE("personas/{id}")
    fun deletePersona(@Path("id") id: Long): Call<Void>
}