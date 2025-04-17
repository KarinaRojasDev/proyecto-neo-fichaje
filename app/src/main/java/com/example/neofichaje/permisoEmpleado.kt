package com.example.neofichaje

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.neofichaje.databinding.ActivityPermisoEmpleadoBinding

class permisoEmpleado : AppCompatActivity() {
    private lateinit var binding: ActivityPermisoEmpleadoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityPermisoEmpleadoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        toolbar()
    }
    private fun toolbar() {
        val barraHerramientas = binding.includePermisos.toolbarComun
        setSupportActionBar(barraHerramientas)
        // Cambiar el título del Toolbar
        supportActionBar?.title = "PERMISOS/BAJAS"
    }
}