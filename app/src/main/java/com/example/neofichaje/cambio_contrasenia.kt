package com.example.neofichaje

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.view.View.OnTouchListener
import com.example.neofichaje.databinding.ActivityCambioContraseniaBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class cambio_contrasenia : AppCompatActivity(), View.OnClickListener, OnTouchListener {
    private lateinit var binding: ActivityCambioContraseniaBinding
    private lateinit var auth: FirebaseAuth
    private var esVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding= ActivityCambioContraseniaBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()

        binding.btnGuardarPass.setOnClickListener(this)
        binding.etPassActual.setOnTouchListener(this)
        binding.etNuevaContrasenia.setOnTouchListener(this)
        binding.etConfirmeContrasenia.setOnTouchListener(this)

    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.btnGuardarPass.id -> {
                cambiarContrasenia(v)
            }
        }
    }
    private fun cambiarContrasenia(vista: View) {
        val pass = binding.etPassActual.text.toString().trim()
        val nuevaContrasenia = binding.etNuevaContrasenia.text.toString().trim()
        val confirmarContrasenia = binding.etConfirmeContrasenia.text.toString().trim()

        if (pass.isEmpty() || nuevaContrasenia.isEmpty() || confirmarContrasenia.isEmpty()) {
            Snackbar.make(vista, "Por favor, completa todos los campos", Snackbar.LENGTH_LONG).show()
            return
        }

        if (nuevaContrasenia != confirmarContrasenia) {
            Snackbar.make(vista, "La nueva contraseña no coincide", Snackbar.LENGTH_LONG).show()
            return
        }

        //-------------------------------------------------
        // Intenta iniciar sesión con un usuario temporal
        FirebaseFirestore.getInstance()
            .collection("usuarios") // o "administradores" según tu colección
            .whereEqualTo("email_admin", pass)
            .get()
            .addOnSuccessListener { documentos ->
                if (!documentos.isEmpty) {
                    //  Si existe el email en Firestore
                    FirebaseAuth.getInstance().signInWithEmailAndPassword(pass, "098765a") // OJO AQUI TENGO LA CONTRASEÑA PERO PARA QUE FUNCIONE CORRECTAMENTE
                        // TENGO QUE HACER UN INTENT Y PASAR ESA CONTRASEÑA DE LA PANTALLA CONFIGURACION EMPRESARIO
                        .addOnSuccessListener { authResult ->
                            val usuario = authResult.user

                            usuario?.updatePassword(nuevaContrasenia)
                                ?.addOnSuccessListener {
                                    Snackbar.make(vista, "Contraseña actualizada correctamente", Snackbar.LENGTH_LONG).show()
                                    acciones()
                                }
                                ?.addOnFailureListener {
                                    Snackbar.make(vista, "Error al actualizar contraseña", Snackbar.LENGTH_LONG).show()
                                }
                        }
                        .addOnFailureListener {
                            Snackbar.make(vista, "No se pudo autenticar al usuario, verifica el correo", Snackbar.LENGTH_LONG).show()
                        }
                } else {
                    Snackbar.make(vista, "Este correo no está registrado", Snackbar.LENGTH_LONG).show()
                }
            }
            .addOnFailureListener {
                Snackbar.make(vista, "Error al buscar en la base de datos", Snackbar.LENGTH_LONG).show()
            }
    }
    //-----------------------------------------------
    private fun acciones(){
        val intent = Intent(this, activity_login_empresario::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        when (v?.id) {
            binding.etPassActual.id -> {
                if (event?.action == MotionEvent.ACTION_UP) {
                    val posicionIconoDerecha = 2
                    val iconoOjito = binding.etPassActual.compoundDrawables[posicionIconoDerecha]

                    if (iconoOjito != null) {
                        val anchoIcono = iconoOjito.bounds.width()
                        val inicioZonaToque = binding.etPassActual.width - binding.etPassActual.paddingEnd - anchoIcono

                        if (event.x >= inicioZonaToque) {
                            esVisible = !esVisible
                            cambiarVisibilidadContrasenia(binding.etPassActual)
                            return true
                        }
                    }
                }
            }
            binding.etNuevaContrasenia.id -> {
                if (event?.action == MotionEvent.ACTION_UP) {
                    val posicionIconoDerecha = 2
                    val iconoOjito = binding.etNuevaContrasenia.compoundDrawables[posicionIconoDerecha]

                    if (iconoOjito != null) {
                        val anchoIcono = iconoOjito.bounds.width()
                        val inicioZonaToque = binding.etNuevaContrasenia.width - binding.etNuevaContrasenia.paddingEnd - anchoIcono

                        if (event.x >= inicioZonaToque) {
                            esVisible = !esVisible
                            cambiarVisibilidadContrasenia(binding.etNuevaContrasenia)
                            return true
                        }
                    }
                }
            }
            binding.etConfirmeContrasenia.id -> {
                if (event?.action == MotionEvent.ACTION_UP) {
                    val posicionIconoDerecha = 2
                    val iconoOjito = binding.etConfirmeContrasenia.compoundDrawables[posicionIconoDerecha]

                    if (iconoOjito != null) {
                        val anchoIcono = iconoOjito.bounds.width()
                        val inicioZonaToque = binding.etConfirmeContrasenia.width - binding.etConfirmeContrasenia.paddingEnd - anchoIcono

                        if (event.x >= inicioZonaToque) {
                            esVisible = !esVisible
                            cambiarVisibilidadContrasenia(binding.etConfirmeContrasenia)
                            return true
                        }
                    }
                }
            }
        }
        return false
    }

    private fun cambiarVisibilidadContrasenia(view: View) {
        when (view.id) {
            binding.etPassActual.id -> {
                if (esVisible) {
                    binding.etPassActual.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                    binding.etPassActual.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_eye, 0)
                } else {
                    binding.etPassActual.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                    binding.etPassActual.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_eye, 0)
                }
                binding.etPassActual.setSelection(binding.etPassActual.text.length)
            }
            binding.etNuevaContrasenia.id -> {
                if (esVisible) {
                    binding.etNuevaContrasenia.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                    binding.etNuevaContrasenia.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_eye, 0)
                } else {
                    binding.etNuevaContrasenia.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                    binding.etNuevaContrasenia.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_eye, 0)
                }
                binding.etNuevaContrasenia.setSelection(binding.etNuevaContrasenia.text.length)
            }
            binding.etConfirmeContrasenia.id -> {
                if (esVisible) {
                    binding.etConfirmeContrasenia.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                    binding.etConfirmeContrasenia.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_eye, 0)
                } else {
                    binding.etConfirmeContrasenia.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                    binding.etConfirmeContrasenia.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_eye, 0)
                }
                binding.etConfirmeContrasenia.setSelection(binding.etConfirmeContrasenia.text.length)
            }
        }
    }

}