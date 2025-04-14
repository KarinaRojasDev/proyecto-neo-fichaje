package com.example.neofichaje

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import com.example.neofichaje.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import android.view.View
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore


class MainActivity : AppCompatActivity(), View.OnClickListener, View.OnTouchListener{
    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private var esVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()

        binding.btnIniSesionEmple.setOnClickListener(this)
        binding.etContrasenia.setOnTouchListener(this)
        binding.txtPuesto.setOnClickListener(this)

    }
    override fun onClick(v: View?) {
        when (v?.id) {
            binding.btnIniSesionEmple.id -> {
                iniciarSesion()
            }
            binding.txtPuesto.id -> {
                accionEmpresario()
            }
        }
    }
    private fun iniciarSesion() {
        val correo = binding.etCorreo.text.toString().trim()
        val contrasenia = binding.etContrasenia.text.toString().trim()

        if (correo.isEmpty() || contrasenia.isEmpty()) {
            Snackbar.make(binding.root, "Por favor, completa todos los campos", Snackbar.LENGTH_LONG).show()
            return
        }
        // Realizar el inicio de sesión con Firebase Auth
        auth.signInWithEmailAndPassword(correo, contrasenia)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    accionesEmpleado()
                } else {
                    Snackbar.make(binding.root, "El correo o la contraseña son incorrectos. Por favor, verifica e intenta nuevamente.",
                        Snackbar.LENGTH_LONG).show()
                }
            }
    }
    private fun accionesEmpleado() {
        val usuario = auth.currentUser
        if (usuario !=null) {
            val db = FirebaseFirestore.getInstance()
            val usuarioRef = db.collection("usuarios").document(usuario.uid)

            usuarioRef.get().addOnSuccessListener { document ->
                if (document.exists()) {
                    val rol = document.getString("puesto")
                    when (rol?.lowercase()) {
                        "tecnico" -> {
                            val intent = Intent(this, inicio_empleado::class.java)
                            startActivity(intent)
                        }
                        else -> {
                            Snackbar.make(binding.root, "Rol no reconocido", Snackbar.LENGTH_LONG).show()
                        }
                    }
                } else {
                    Snackbar.make(binding.root, "No se encontró el usuario en la base de datos", Snackbar.LENGTH_LONG).show()
                }
            }.addOnFailureListener {
                Snackbar.make(binding.root, "Error al obtener datos del usuario", Snackbar.LENGTH_LONG).show()
            }
        }

    }
    private fun accionEmpresario(){
        val intent = Intent(this, activity_login_empresario::class.java)
        startActivity(intent)
    }

    private fun limpiar(){
        binding.etCorreo.text.clear()
        binding.etContrasenia.text.clear()
    }
    // esto para EMPRESARIO
    override fun onRestart() {
        super.onRestart()
        limpiar()
    }
    // Método visibilidad de la contraseña
    private fun cambiarVisibilidadContrasenia() {
        if (esVisible) {
            binding.etContrasenia.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            // Cambia a ícono de ojo abierto
            binding.etContrasenia.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_lock, 0, R.drawable.ic_eye, 0)
        } else {
            binding.etContrasenia.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            // Cambia a ícono de ojo cerrado
            binding.etContrasenia.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_lock, 0, R.drawable.ic_eye, 0)
        }
        binding.etContrasenia.setSelection(binding.etContrasenia.text.length)
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        when (v?.id) {
            binding.etContrasenia.id -> {
                if (event?.action == MotionEvent.ACTION_UP) {
                    val posicionIconoDerecha = 2  // Posicion icono
                    val iconoOjito = binding.etContrasenia.compoundDrawables[posicionIconoDerecha]

                    if (iconoOjito != null) {
                        val anchoIcono = iconoOjito.bounds.width()
                        // El área donde el usuario puede tocar
                        val inicioZonaToque = binding.etContrasenia.width - binding.etContrasenia.paddingEnd - anchoIcono

                        // Verifica si el usuario tocó dentro del área del ícono
                        if (event.x >= inicioZonaToque) {
                            esVisible = !esVisible  // Alterna la visibilidad de la contraseña
                            cambiarVisibilidadContrasenia()
                            return true
                        }
                    }
                }
            }
        }
        return false
    }
    private fun loginEmpresario(){
        val intent = Intent(this, activity_login_empresario ::class.java)
        startActivity(intent)
    }
}