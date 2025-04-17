package com.example.neofichaje

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.neofichaje.databinding.ActivityContratoEmpleadoBinding

class contratoEmpleado : AppCompatActivity() {
    private lateinit var binding: ActivityContratoEmpleadoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding=ActivityContratoEmpleadoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        toolbar()
    }
    private fun toolbar(){
        val barraHerramientas = binding.includeContratoEmpleado.toolbarComun
        setSupportActionBar(barraHerramientas)

        // Cambiar el título del Toolbar
        supportActionBar?.title = "CONTRATO DE TRABAJO"

    }
}