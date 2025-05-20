package com.example.neofichaje

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.neofichaje.databinding.ActivityRegistroGoogleBinding
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod


class registroGoogle : AppCompatActivity(), View.OnClickListener,OnTouchListener {

    private lateinit var binding: ActivityRegistroGoogleBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var esVisible = false

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistroGoogleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        binding.btnGuardarGoogle.setOnClickListener(this)
        instancias()
        binding.etContrasenaGoogle.setOnTouchListener(this)
    }

    override fun onClick(v: View?) {
        if (v?.id == binding.btnGuardarGoogle.id) {
            guardarDatosGoogle()
        }
    }

    private fun instancias() {
        val puestos = listOf("Seleccionar puesto", "Administrador", "Gerente", "RRHH")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, puestos)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerPuesto.adapter = adapter
    }

    private fun guardarDatosGoogle() {
        val nombreEmpresa = binding.etNombreEmpresa.text.toString().trim()
        val nif = binding.etNif.text.toString().trim()
        val emailEmpresa = binding.etEmailEmpresa.text.toString().trim()
        val telefonoEmpresa = binding.etTelefonoEmpresa.text.toString().trim()
        val direccionEmpresa = binding.etDireccionEmpresa.text.toString().trim()
        val webEmpresa = binding.etWeb.text.toString().trim()
        val empresaId = binding.etEmpresaId.text.toString().trim()

        val nombre = binding.etNombre.text.toString().trim()
        val apellido = binding.etApellido.text.toString().trim()
        val telefono = binding.etTelefono.text.toString().trim()
        val numEmpleado = binding.etNumEmpleado.text.toString().trim()
        val puesto = binding.spinnerPuesto.selectedItem.toString()
        val email = auth.currentUser?.email ?: ""
        val password = binding.etContrasenaGoogle.text.toString().trim()

        if (nombreEmpresa.isEmpty() || nif.isEmpty() || emailEmpresa.isEmpty() || telefonoEmpresa.isEmpty() ||
            direccionEmpresa.isEmpty() || webEmpresa.isEmpty() || empresaId.isEmpty() ||
            nombre.isEmpty() || apellido.isEmpty() || telefono.isEmpty() || numEmpleado.isEmpty() ||
            puesto == "Seleccionar puesto" || password.isEmpty()) {

            Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        val uid = auth.currentUser?.uid ?: return

        db.collection("usuarios").document(uid).get()
            .addOnSuccessListener { docUser ->
                if (docUser.exists()) {
                    Toast.makeText(this, "Este usuario ya está registrado.", Toast.LENGTH_LONG).show()
                    startActivity(Intent(this, inicio_empresario::class.java))
                    finish()
                    return@addOnSuccessListener
                }

                db.collection("empresas").document(empresaId).get()
                    .addOnSuccessListener { snapshot ->
                        if (snapshot.exists()) {
                            Toast.makeText(this, "Ya existe una empresa con este ID", Toast.LENGTH_LONG).show()
                            return@addOnSuccessListener
                        }

                        val empresaData = hashMapOf(
                            "nombreEmpresa" to nombreEmpresa,
                            "nif" to nif,
                            "emailEmpresa" to emailEmpresa,
                            "telefonoEmpresa" to telefonoEmpresa,
                            "direccionEmpresa" to direccionEmpresa,
                            "web" to webEmpresa
                        )

                        db.collection("empresas").document(empresaId).set(empresaData)

                        val subcolecciones = listOf("gestionVacaciones", "gestionContratos", "gestionNominas", "gestionControlHorario", "gestionPermisos_bajas")
                        subcolecciones.forEach {
                            db.collection("empresas").document(empresaId).collection(it).document("inicio")
                                .set(mapOf("estado" to "pendiente"))
                        }

                        val usuarioData = hashMapOf(
                            "nombre" to nombre,
                            "apellidos" to apellido,
                            "email" to email,
                            "telefono" to telefono,
                            "numEmpleado" to numEmpleado,
                            "puesto" to puesto,
                            "empresa_id" to empresaId
                        )

                        db.collection("usuarios").document(uid).set(usuarioData)
                            .addOnSuccessListener {
                                val credential = EmailAuthProvider.getCredential(email, password)
                                auth.currentUser?.linkWithCredential(credential)
                                    ?.addOnSuccessListener {
                                        Toast.makeText(this, "Registro completado con éxito. Puedes iniciar con Google o contraseña.", Toast.LENGTH_LONG).show()
                                        startActivity(Intent(this, inicio_empresario::class.java))
                                        finish()
                                    }
                                    ?.addOnFailureListener {
                                        Toast.makeText(this, "Datos guardados, pero no se pudo vincular la contraseña: ${it.message}", Toast.LENGTH_LONG).show()
                                    }
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Error al registrar el usuario", Toast.LENGTH_LONG).show()
                            }
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Error al verificar empresa", Toast.LENGTH_LONG).show()
                    }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al verificar usuario", Toast.LENGTH_LONG).show()
            }
    }
    private fun cambiarVisibilidadContrasenia() {
        val campo = binding.etContrasenaGoogle
        if (esVisible) {
            campo.transformationMethod = HideReturnsTransformationMethod.getInstance()
        } else {
            campo.transformationMethod = PasswordTransformationMethod.getInstance()
        }

        // Mantiene el cursor al final
        campo.setSelection(campo.text.length)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        when (v?.id) {
            binding.etContrasenaGoogle.id -> {
                if (event?.action == MotionEvent.ACTION_UP) {
                    val iconoDerecho = 2
                    val iconoOjito = binding.etContrasenaGoogle.compoundDrawables[iconoDerecho]

                    if (iconoOjito != null) {
                        val anchoIcono = iconoOjito.bounds.width()
                        val inicioZonaToque = binding.etContrasenaGoogle.width -
                                binding.etContrasenaGoogle.paddingEnd - anchoIcono

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
