package com.example.neofichaje

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.neofichaje.databinding.ActivityEmpresarioPerfilBinding

class activity_empresario_perfil : AppCompatActivity() {
    private lateinit var binding: ActivityEmpresarioPerfilBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityEmpresarioPerfilBinding.inflate(layoutInflater)

        setContentView(binding.root)
    }
}