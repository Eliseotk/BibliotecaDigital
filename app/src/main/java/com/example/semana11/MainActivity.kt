package com.example.semana11

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.content.Intent

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnCalc = findViewById<Button>(R.id.btnRegistrar)

        btnCalc.setOnClickListener {
            val intent = Intent(applicationContext, ListaPersonasActivity::class.java)
            startActivity(intent)
        }
    }
}