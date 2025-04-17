package com.example.neofichaje

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.neofichaje.databinding.ActivityGestionEmpleadosBinding

class gestionEmpleados : AppCompatActivity() {
    private lateinit var binding: ActivityGestionEmpleadosBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityGestionEmpleadosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        toolbar()
    }
    private fun toolbar() {
        val barraHerramientas = binding.includeGestionEmpleados.toolbarComun
        setSupportActionBar(barraHerramientas)
        // Cambiar el título del Toolbar
        supportActionBar?.title = "GESTIÓN EMPLEADOS"
    }
}