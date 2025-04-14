package com.example.neofichaje

import android.os.Bundle
import android.text.InputType
import androidx.appcompat.app.AppCompatActivity
import com.example.neofichaje.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import android.view.View
import android.widget.Toast


class MainActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private var esVisible: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializar el ViewBinding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializar Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Configurar los botones con el mismo listener (esta actividad)
        binding.btnIniSesionEmple.setOnClickListener(this)
        cambiarVisibilidadContrasenia()
    }
    // Método que alterna la visibilidad de la contraseña
    private fun cambiarVisibilidadContrasenia() {
        esVisible = !esVisible
        if (esVisible) {
            binding.etContrasenia.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        } else {
            binding.etContrasenia.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        // Mueve el cursor al final del texto
        binding.etContrasenia.setSelection(binding.etContrasenia.text.length)
    }
    // Implementamos el método onClick() de View.OnClickListener
    override fun onClick(v: View?) {
        when (v?.id) {
            // Aquí tratamos el clic del botón de inicio de sesión
            binding.btnIniSesionEmple.id -> {
                iniciarSesion()
            }
        }
    }

    private fun iniciarSesion() {
        val correo = binding.etNombre.text.toString().trim()
        val contrasenia = binding.etContrasenia.text.toString().trim()

        if (correo.isEmpty() || contrasenia.isEmpty()) {
            Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        // Realizar el inicio de sesión con Firebase Auth
        auth.signInWithEmailAndPassword(correo, contrasenia)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Si la autenticación es exitosa, navega a la siguiente pantalla (por ejemplo, Home)
                    Toast.makeText(this, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show()

                    // Aquí puedes redirigir a otra actividad si es necesario
                    // Ejemplo: startActivity(Intent(this, HomeActivity::class.java))

                } else {
                    // Si falla la autenticación, mostrar mensaje de error
                    Toast.makeText(this, "Error en la autenticación: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}