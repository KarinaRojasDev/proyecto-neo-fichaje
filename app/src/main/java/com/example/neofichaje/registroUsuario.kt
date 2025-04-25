package com.example.neofichaje

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.MotionEvent
import android.view.View
import android.view.View.OnClickListener
import android.view.View.OnTouchListener
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.neofichaje.databinding.ActivityRegistroUsuarioBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Suppress("LABEL_NAME_CLASH")
class registroUsuario : AppCompatActivity(),OnClickListener, OnTouchListener {
    private lateinit var binding: ActivityRegistroUsuarioBinding
    private lateinit var puesto:ArrayList<CharSequence>
    private lateinit var adapterPuesto: ArrayAdapter<CharSequence>
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var esVisible = false

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
       binding=ActivityRegistroUsuarioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        instancias()
        binding.etPass.setOnTouchListener(this)
        binding.btnGuardarCambios.setOnClickListener(this)
    }
    private fun instancias() {
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        puesto= arrayListOf("Seleccionar puesto","Gerente","Administrador","RRHH")
        adapterPuesto=ArrayAdapter(applicationContext,android.R.layout.simple_spinner_item,puesto)
        adapterPuesto.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinneriIdPuesto.adapter=adapterPuesto
    }

    override fun onClick(v: View?) {
        registrarEmpresaYUsuario()
    }
    private fun registrarEmpresaYUsuario() {

        val nombreEmpresa = binding.etNombreEmpresa.text.toString().trim()
        val nif = binding.etNif.text.toString().trim()
        val emailEmpresa = binding.etEmailEmpresa.text.toString().trim()
        val telefono = binding.etFonoEmpresa.text.toString().trim()
        val direccion = binding.etDirEmpresa.text.toString().trim()
        val web = binding.etWebEmpresa.text.toString().trim()
        val empresaId = binding.etEmpresaId.text.toString().trim()

        val nombreAdmin = binding.etNombreAdmin.text.toString().trim()
        val apellidoAdmin = binding.etApellidoAdmin.text.toString().trim()
        val emailAdmin = binding.etEmailAdmin.text.toString().trim()
        val telefonoAdmin = binding.etTelefono.text.toString().trim()
        val numEmpleado = binding.etNumEmpleado.text.toString().trim()
        val password = binding.etPass.text.toString().trim()

        val puestoSeleccionado = binding.spinneriIdPuesto.selectedItem.toString()
        if (nombreEmpresa.isEmpty() || nif.isEmpty() || emailEmpresa.isEmpty() ||
            nombreAdmin.isEmpty() || apellidoAdmin.isEmpty() || emailAdmin.isEmpty() ||
            telefonoAdmin.isEmpty() || password.isEmpty() || empresaId.isEmpty()) {

            Toast.makeText(this, "Por favor, completa todos los campos obligatorios", Toast.LENGTH_SHORT).show()
            return
        }


        if (puestoSeleccionado == "Seleccionar puesto") {
            Toast.makeText(this, "Selecciona un puesto válido", Toast.LENGTH_SHORT).show()
            return
        }



        auth.createUserWithEmailAndPassword(emailAdmin, password)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid ?: return@addOnSuccessListener

                val empresaData = hashMapOf(
                    "nombreEmpresa" to nombreEmpresa,
                    "nif" to nif,
                    "emailEmpresa" to emailEmpresa,
                    "telefonoEmpresa" to telefono,
                    "direccionEmpresa" to direccion,
                    "web" to web
                )

                db.collection("empresas").document(empresaId).get()
                    .addOnSuccessListener { snapshot ->
                        if (snapshot.exists()) {
                            Toast.makeText(this, "Ya existe una empresa con este ID", Toast.LENGTH_LONG).show()
                            return@addOnSuccessListener
                        }

                        // Guardar empresa
                        db.collection("empresas").document(empresaId).set(empresaData)

                        //Subcolecciones
                        val subcolecciones = listOf("gestionVacaciones", "gestionContratos", "gestionNominas", "gestionControlHorario", "gestionPermisos_bajas")
                        subcolecciones.forEach {
                            db.collection("empresas").document(empresaId).collection(it).document("inicio")
                                .set(mapOf("estado" to "pendiente"))
                        }

                        // administrador
                        val usuarioData = hashMapOf(
                            "nombre" to nombreAdmin,
                            "apellidos" to apellidoAdmin,
                            "email" to emailAdmin,
                            "telefono" to telefonoAdmin,
                            "numEmpleado" to numEmpleado,
                            "puesto" to puestoSeleccionado,
                            "empresa_id" to empresaId
                        )
                        db.collection("usuarios").document(uid).set(usuarioData)

                        Toast.makeText(this, "Registrado correctamente", Toast.LENGTH_LONG).show()
                        startActivity(Intent(this, activity_login_empresario::class.java))
                        finish()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Error al comprobar empresa: ${it.message}", Toast.LENGTH_LONG).show()
                    }

            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al crear cuenta: ${it.message}", Toast.LENGTH_LONG).show()
            }

    }

    private fun cambiarVisibilidadContrasenia() {
        val campoContrasenia = binding.etPass
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
            binding.etPass.id -> {
                if (event?.action == MotionEvent.ACTION_UP) {
                    val iconoDerecho = 2
                    val iconoOjito = binding.etPass.compoundDrawables[iconoDerecho]

                    if (iconoOjito != null) {
                        val anchoIcono = iconoOjito.bounds.width()
                        val inicioZonaToque = binding.etPass.width -
                                binding.etPass.paddingEnd - anchoIcono

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