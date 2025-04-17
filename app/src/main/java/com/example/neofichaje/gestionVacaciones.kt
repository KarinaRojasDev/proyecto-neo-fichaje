package com.example.neofichaje

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.neofichaje.databinding.ActivityGestionVacacionesBinding

class gestionVacaciones : AppCompatActivity() {
    private lateinit var binding: ActivityGestionVacacionesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGestionVacacionesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        toolbar()
    }
    private fun toolbar() {
        val barraHerramientas = binding.includeGestionVacaciones.toolbarComun
        setSupportActionBar(barraHerramientas)
        // Cambiar el título del Toolbar
        supportActionBar?.title = "GESTIÓN VACACIONES"
    }
}