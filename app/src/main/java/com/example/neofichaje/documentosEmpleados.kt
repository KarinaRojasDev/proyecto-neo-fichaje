package com.example.neofichaje

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.neofichaje.databinding.ActivityDocumentosEmpleadosBinding

class documentosEmpleados : AppCompatActivity() {
    private lateinit var binding: ActivityDocumentosEmpleadosBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityDocumentosEmpleadosBinding.inflate(layoutInflater)
        setContentView(binding.root)
        toolbar()
    }
    private fun toolbar(){
        val barraHerramientas = binding.includeDocEmpleado.toolbarComun
        setSupportActionBar(barraHerramientas)

        // Cambiar el título del Toolbar
        supportActionBar?.title = "ADJUNTAR ARCHIVOS"

    }
}