package com.example.neofichaje

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.neofichaje.databinding.ActivityEmpleadoSolicitudVacacionesBinding

class empleado_solicitud_vacaciones : AppCompatActivity() {

    private lateinit var binding: ActivityEmpleadoSolicitudVacacionesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityEmpleadoSolicitudVacacionesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()

    }
}