package com.example.neofichaje

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import com.example.neofichaje.databinding.ActivityCambioContraseniaBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth

class cambio_contrasenia : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding: ActivityCambioContraseniaBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding= ActivityCambioContraseniaBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()

        binding.btnGuardarPass.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.btnGuardarPass.id -> {
                cambiarContrasenia(v)
            }
        }
    }
    private fun cambiarContrasenia(vista: View) {
        val contraseniaActual = binding.etContraseniaActual.text.toString().trim()
        val nuevaContrasenia = binding.etNuevaContrasenia.text.toString().trim()
        val confirmarContrasenia = binding.etConfirmeContrasenia.text.toString().trim()

        if (contraseniaActual.isEmpty() || nuevaContrasenia.isEmpty() || confirmarContrasenia.isEmpty()) {
            Snackbar.make(vista, "Por favor, completa todos los campos", Snackbar.LENGTH_LONG).show()
            return
        }

        if (nuevaContrasenia != confirmarContrasenia) {
            Snackbar.make(vista, "La nueva contraseña no coincide", Snackbar.LENGTH_LONG).show()
            return
        }

        val usuario = auth.currentUser
        val correo = usuario?.email

        if (usuario != null && correo != null) {
            val credencial = EmailAuthProvider.getCredential(correo, contraseniaActual)

            usuario.reauthenticate(credencial).addOnCompleteListener { reauth ->
                if (reauth.isSuccessful) {
                    usuario.updatePassword(nuevaContrasenia).addOnCompleteListener { update ->
                        if (update.isSuccessful) {
                            Snackbar.make(vista, "Contraseña actualizada con éxito", Snackbar.LENGTH_LONG).show()
                            acciones()

                        } else {
                            Snackbar.make(vista, "Error al actualizar la contraseña", Snackbar.LENGTH_LONG).show()
                        }
                    }
                } else {
                    Snackbar.make(vista, "La contraseña actual es incorrecta", Snackbar.LENGTH_LONG).show()
                }
            }
        } else {
            Snackbar.make(vista, "Usuario no autenticado", Snackbar.LENGTH_LONG).show()
        }
    }
    private fun acciones(){
        val intent = Intent(this, activity_login_empresario::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}