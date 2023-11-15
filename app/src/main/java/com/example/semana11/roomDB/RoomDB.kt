package com.example.semana11.roomDB

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.semana11.entidades.Persona
import com.example.semana11.services.PersonaDao
import java.util.concurrent.Executors

@Database(entities = [Persona::class], version = 1)
abstract class RoomDB : RoomDatabase() {
    abstract fun personaDao(): PersonaDao
}
