package com.example.neofichaje

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.neofichaje.databinding.ActivityLoginEmpresarioBinding

class activity_login_empresario : AppCompatActivity() {
    private lateinit var binding: ActivityLoginEmpresarioBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityLoginEmpresarioBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}