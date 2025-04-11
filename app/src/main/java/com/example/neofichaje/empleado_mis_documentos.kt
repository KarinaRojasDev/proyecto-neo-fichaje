package com.example.neofichaje

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.neofichaje.databinding.ActivityEmpleadoMisDocumentosBinding

class empleado_mis_documentos : AppCompatActivity() {
    private  lateinit var binding: ActivityEmpleadoMisDocumentosBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityEmpleadoMisDocumentosBinding.inflate(layoutInflater)

        setContentView(binding.root)
    }
}