package com.example.neofichaje

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.MotionEvent
import android.view.View
import android.view.View.OnClickListener
import android.view.View.OnTouchListener
import androidx.appcompat.app.AppCompatActivity
import com.example.neofichaje.databinding.ActivityLoginEmpleadoBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class login_empleado : AppCompatActivity(), OnClickListener, OnTouchListener {
    private lateinit var binding: ActivityLoginEmpleadoBinding
    private lateinit var auth: FirebaseAuth
    private var esVisible = false

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginEmpleadoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        binding.btnIniSesionEmple.setOnClickListener(this)
        binding.etContrasenia.setOnTouchListener(this)
        binding.txtPuesto.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.btnIniSesionEmple.id -> iniciarSesion()
            binding.txtPuesto.id -> accionEmpresario()
        }
    }

    private fun iniciarSesion() {
        val correo = binding.etCorreo.text.toString().trim()
        val contrasenia = binding.etContrasenia.text.toString().trim()

        if (correo.isEmpty() || contrasenia.isEmpty()) {
            Snackbar.make(binding.root, "Por favor, completa todos los campos", Snackbar.LENGTH_LONG).show()
            return
        }

        auth.signInWithEmailAndPassword(correo, contrasenia)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    accionesEmpleado()
                } else {
                    Snackbar.make(binding.root, "El correo o la contraseña son incorrectos. " +
                            "Por favor, verifica e intenta nuevamente.",
                        Snackbar.LENGTH_LONG).show()
                }
            }
    }
    private fun accionesEmpleado() {
        val usuario = auth.currentUser

        if (usuario != null) {
            val db = FirebaseFirestore.getInstance()
            val uid = usuario.uid

            val usuarioRef = db.collection("usuarios").document(uid)

            usuarioRef.get().addOnSuccessListener { documento ->
                if (documento.exists()) {
                    val puesto = documento.getString("puesto") ?: ""

                    if (puesto.lowercase() == "tecnico") {
                        val intent = Intent(this, inicio_empleado::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Snackbar.make(binding.root, "El usuario no existe en la base de datos.",
                            Snackbar.LENGTH_LONG).show()
                    }
                } else {
                    Snackbar.make(binding.root, "No se encontró tu usuario en la base de datos.",
                        Snackbar.LENGTH_LONG).show()
                }
            }.addOnFailureListener {
                Snackbar.make(binding.root, "Error al buscar datos del usuario en Firestore.",
                    Snackbar.LENGTH_LONG).show()
            }
        } else {
            Snackbar.make(binding.root, "Error: Usuario no autenticado.",
                Snackbar.LENGTH_LONG).show()
        }
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        when (v?.id) {
            binding.etContrasenia.id -> {
                if (event?.action == MotionEvent.ACTION_UP) {
                    val posicionIconoDerecha = 2
                    val iconoOjito = binding.etContrasenia.compoundDrawables[posicionIconoDerecha]

                    if (iconoOjito != null) {
                        val anchoIcono = iconoOjito.bounds.width()
                        val inicioZonaToque = binding.etContrasenia.width - binding.etContrasenia.paddingEnd - anchoIcono

                        if (event.x >= inicioZonaToque) {
                            esVisible = !esVisible
                            cambiarVisibilidadContrasenia()
                            return true
                        }
                    }
                }
            }
        }
        return false
    }

    private fun cambiarVisibilidadContrasenia() {
        if (esVisible) {
            binding.etContrasenia.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            binding.etContrasenia.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_lock, 0, R.drawable.ic_eye, 0)
        } else {
            binding.etContrasenia.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            binding.etContrasenia.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_lock, 0, R.drawable.ic_eye, 0)
        }
        binding.etContrasenia.setSelection(binding.etContrasenia.text.length)
    }

    private fun accionEmpresario() {
        val intent = Intent(this, activity_login_empresario::class.java)
        startActivity(intent)
    }

    private fun limpiar() {
        binding.etCorreo.text.clear()
        binding.etContrasenia.text.clear()
    }

    override fun onRestart() {
        super.onRestart()
        limpiar()
    }
}
