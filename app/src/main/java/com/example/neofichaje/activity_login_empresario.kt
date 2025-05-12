package com.example.neofichaje

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.View.OnClickListener
import androidx.appcompat.app.AppCompatActivity
import com.example.neofichaje.databinding.ActivityLoginEmpresarioBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore



class activity_login_empresario : AppCompatActivity(), OnClickListener, OnTouchListener {
    private lateinit var binding: ActivityLoginEmpresarioBinding
    private lateinit var auth: FirebaseAuth
    private var esVisible = false



    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginEmpresarioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        binding.btnIniSesionEmpresa.setOnClickListener(this)
        binding.etContraseniaEmpresa.setOnTouchListener(this)
        binding.tvRegistrarse.setOnClickListener(this)
        binding.btnGoogle.setOnClickListener(this)

    }

    override fun onClick(v: View?) {
        when(v?.id){
            binding.btnIniSesionEmpresa.id -> {
                iniciarSesion()
            }
            binding.tvRegistrarse.id->{
                accionesIntent()
            }
            binding.btnGoogle.id -> {

            }
        }
    }

    private fun accionesIntent() {
        val intent = Intent(this, registroUsuario::class.java)
        startActivity(intent)
    }

    private fun iniciarSesion() {
        val correo = binding.etCorreoEmpresa.text.toString().trim()
        val contrasenia = binding.etContraseniaEmpresa.text.toString().trim()

        if (correo.isEmpty() || contrasenia.isEmpty()) {
            Snackbar.make(binding.root, "Por favor, completa todos los campos", Snackbar.LENGTH_LONG).show()
            return
        }

        auth.signInWithEmailAndPassword(correo, contrasenia)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {

                    val userId = auth.currentUser?.uid ?: return@addOnCompleteListener
                    val db = FirebaseFirestore.getInstance()

                    db.collection("usuarios").document(userId)
                        .get()
                        .addOnSuccessListener { document ->
                            if (document.exists()) {
                                val puesto = document.getString("puesto") ?: ""
                                when (puesto) {
                                    "Administrador", "RRHH", "Gerente" -> {
                                        val intent = Intent(this, inicio_empresario::class.java)
                                        startActivity(intent)
                                    }
                                    "Tecnico" -> {
                                        val intent = Intent(this, inicio_empleado::class.java)
                                        startActivity(intent)
                                    }
                                    else -> {
                                        auth.signOut()
                                        Snackbar.make(binding.root, "Puesto no reconocido o no autorizado", Snackbar.LENGTH_LONG).show()
                                    }
                                }
                            } else {
                                auth.signOut()
                                Snackbar.make(binding.root, "No se encontró el usuario", Snackbar.LENGTH_LONG).show()
                            }
                        }
                        .addOnFailureListener {
                            auth.signOut()
                            Snackbar.make(binding.root, "Error al verificar el puesto del usuario.", Snackbar.LENGTH_LONG).show()
                        }

                } else {
                    Snackbar.make(binding.root, "Correo o contraseña incorrectos.", Snackbar.LENGTH_LONG).show()
                }
            }
    }

    private fun limpiar(){
        binding.etCorreoEmpresa.text.clear()
        binding.etContraseniaEmpresa.text.clear()
    }
    // EMPRESARIOPARA VOLVER A LOGIN Y ESTE LIMPIO ESTA PANTALLA
    override fun onRestart() {
        super.onRestart()
        limpiar()
    }
    private fun cambiarVisibilidadContrasenia() {
        val campoContrasenia = binding.etContraseniaEmpresa
        if (esVisible) {
            campoContrasenia.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        } else {
            campoContrasenia.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        campoContrasenia.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_lock, 0, R.drawable.ic_eye, 0)
        campoContrasenia.setSelection(campoContrasenia.text.length)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        when (v?.id) {
            binding.etContraseniaEmpresa.id -> {
                if (event?.action == MotionEvent.ACTION_UP) {
                    val iconoDerecho = 2
                    val iconoOjito = binding.etContraseniaEmpresa.compoundDrawables[iconoDerecho]

                    if (iconoOjito != null) {
                        val anchoIcono = iconoOjito.bounds.width()
                        val inicioZonaToque = binding.etContraseniaEmpresa.width -
                                binding.etContraseniaEmpresa.paddingEnd - anchoIcono

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
}