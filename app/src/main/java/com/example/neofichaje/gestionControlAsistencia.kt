package com.example.neofichaje

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.neofichaje.databinding.ActivityGestionControlAsistenciaBinding

class gestionControlAsistencia : AppCompatActivity() {
    private lateinit var binding: ActivityGestionControlAsistenciaBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityGestionControlAsistenciaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        toolbar()
    }
    private fun toolbar(){
        val barraHerramientas = binding.includeGestionAsistencia.toolbarComun
        setSupportActionBar(barraHerramientas)

        // Cambiar el título del Toolbar
        supportActionBar?.title = "GESTIÓN CONTROL HORARIO"

    }
}