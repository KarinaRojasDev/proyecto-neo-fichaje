package com.example.neofichaje

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.neofichaje.databinding.ActivityLoginEmpleadoBinding

class activity_login_empleado : AppCompatActivity() {
    private lateinit var binding: ActivityLoginEmpleadoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityLoginEmpleadoBinding.inflate(layoutInflater)
        setContentView(binding.root)

    }
}