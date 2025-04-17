package com.example.neofichaje

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.neofichaje.databinding.ActivityPerfilEmpresarioBinding

class perfilEmpresario : AppCompatActivity(),OnClickListener {
    private lateinit var binding: ActivityPerfilEmpresarioBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
       binding=ActivityPerfilEmpresarioBinding.inflate(layoutInflater)
        setContentView(binding.root)


        toolbar()
        binding.btnEditarPerfilEmpresa.setOnClickListener(this)
        binding.btnContrasenia.setOnClickListener(this)

    }
    private fun toolbar() {
        val barraHerramientas = binding.includePerfil.toolbarComun
        setSupportActionBar(barraHerramientas)
        // Cambiar el título del Toolbar
        supportActionBar?.title = "MI PERFIL"
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_editarPerfilEmpresa -> {
                acciones()
            }

            R.id.btn_contrasenia -> {
                accionesContrasenia()
            }
        }
    }
    private fun acciones(){
        val intent= Intent(this, activity_empresario_perfil ::class.java)
        startActivity(intent)
    }
    private fun accionesContrasenia() {
        // Intent para el segundo botón (Cambiar Contraseña)
        val intentContrasenia = Intent(this, cambio_contrasenia::class.java)
        startActivity(intentContrasenia)
    }
}