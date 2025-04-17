package com.example.neofichaje

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
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

        toolbar()

    }

    private fun toolbar() {
        val barraHerramientas = binding.includeEditarPerfil.toolbarComun
        setSupportActionBar(barraHerramientas)

        // Cambiar el título del Toolbar
        supportActionBar?.title = "CONTROL HORARIO"

    }
}