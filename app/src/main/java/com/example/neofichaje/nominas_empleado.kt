package com.example.neofichaje

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.neofichaje.databinding.ActivityNominasEmpleadoBinding

class nominas_empleado : AppCompatActivity() {
    private lateinit var binding: ActivityNominasEmpleadoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNominasEmpleadoBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}