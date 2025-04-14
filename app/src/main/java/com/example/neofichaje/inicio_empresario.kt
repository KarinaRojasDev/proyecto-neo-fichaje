package com.example.neofichaje

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.neofichaje.databinding.ActivityInicioEmpresarioBinding

class inicio_empresario : AppCompatActivity() {
    private lateinit var binding: ActivityInicioEmpresarioBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInicioEmpresarioBinding.inflate(layoutInflater)
        setContentView(binding.root)

    }
}