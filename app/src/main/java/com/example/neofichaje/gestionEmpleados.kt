package com.example.neofichaje

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.functions.FirebaseFunctions
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import com.example.neofichaje.databinding.ActivityGestionEmpleadosBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class gestionEmpleados : AppCompatActivity(),OnClickListener {
    private lateinit var binding: ActivityGestionEmpleadosBinding
    private lateinit var menu: ActionBarDrawerToggle
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityGestionEmpleadosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        toolbar()
        configurarMenuLateral()
        manejarOpcionesMenu()
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        binding.btnAgregar.setOnClickListener(this)
        binding.tvAgregarNuevos.setOnClickListener(this)
        binding.btnEliminar.setOnClickListener(this)
        cargarEmpleadosEnSpinner()

    }
    override fun onClick(v: View?) {
        when(v?.id){
            binding.tvAgregarNuevos.id->{
                binding.linearLayoutEditar.visibility = View.VISIBLE
            }
            binding.btnAgregar.id -> {
                registrarEmpleado()
            }
            binding.btnEliminar.id-> {
                eliminarEmpleadoSeleccionado()
            }
        }
    }
    private fun eliminarEmpleadoSeleccionado() {
        val seleccionado = binding.spinnerLista.selectedItem?.toString() ?: return

        if (seleccionado.isBlank()) {
            Toast.makeText(this, "Selecciona un empleado para eliminar", Toast.LENGTH_SHORT).show()
            return
        }
        db.collection("usuarios")
            .whereEqualTo("puesto", "Tecnico")
            .get()
            .addOnSuccessListener { docs ->
                for (doc in docs) {
                    val nombreCompleto = "${doc.getString("nombre_admin") ?: ""} ${doc.getString("apellidos") ?: ""}"
                    if (nombreCompleto == seleccionado) {
                        val idEmpleado = doc.id

                        // Llamada a función cloud para borrar por completo el empleado
                        FirebaseFunctions.getInstance()
                            .getHttpsCallable("borrarEmpleado")
                            .call(mapOf("uid" to idEmpleado))
                            .addOnSuccessListener {
                                Toast.makeText(this, "Empleado eliminado correctamente", Toast.LENGTH_SHORT).show()
                                cargarEmpleadosEnSpinner() // actualizar lista
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Error al eliminar el empleado", Toast.LENGTH_SHORT).show()
                            }

                        break
                    }
                }
            }
    }

    private fun eliminarSubcolecciones(empleadoId: String) {
        val subcolecciones = listOf("contrato", "controlHorario", "nominas", "permisos_bajas", "vacaciones")

        for (coleccion in subcolecciones) {
            db.collection("usuarios").document(empleadoId)
                .collection(coleccion)
                .get()
                .addOnSuccessListener { docs ->
                    for (doc in docs) {
                        doc.reference.delete()
                    }
                }
        }
    }


    private fun cargarEmpleadosEnSpinner() {
        val empleados = mutableListOf<String>()

        db.collection("usuarios")
            .whereEqualTo("puesto", "Tecnico")
            .get()
            .addOnSuccessListener { documentos ->
                for (doc in documentos) {
                    val nombre = doc.getString("nombre_admin") ?: ""
                    val apellidos = doc.getString("apellidos") ?: ""
                    empleados.add("$nombre $apellidos")
                }

                // ❤️ Spinner personalizado con color negro
                val adaptador = object : ArrayAdapter<String>(
                    this,
                    android.R.layout.simple_spinner_item,
                    empleados
                ) {
                    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                        val view = super.getView(position, convertView, parent)
                        (view as TextView).setTextColor(ContextCompat.getColor(context, android.R.color.black))
                        return view
                    }

                    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                        val view = super.getDropDownView(position, convertView, parent)
                        (view as TextView).setTextColor(ContextCompat.getColor(context, android.R.color.white))
                        return view
                    }
                }

                adaptador.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.spinnerLista.adapter = adaptador
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al cargar empleados", Toast.LENGTH_SHORT).show()
            }
    }

    private fun registrarEmpleado() {
        val nombre = binding.etNombreEmpleado.text.toString().trim()
        val apellidos = binding.etApellidosEmpleado.text.toString().trim()
        val email = binding.etEmailEmpleado.text.toString().trim()
        val telefono = binding.etNumTelefono.text.toString().trim()
        val direccion = binding.etDirEmpleado.text.toString().trim()
        val puesto = binding.etPuestoEmpleado.text.toString().trim()
        val numEmpleado = binding.etNumEmpleado.text.toString().trim()
        val password = binding.etPassEmpleado.text.toString().trim()

        if (nombre.isEmpty() || apellidos.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(
                this,
                "Por favor completa todos los campos obligatorios",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid ?: return@addOnSuccessListener
                val datosEmpleado = hashMapOf(
                    "nombre_admin" to nombre,
                    "apellidos" to apellidos,
                    "email_admin" to email,
                    "telefono" to telefono,
                    "direccion" to direccion,
                    "puesto" to puesto,
                    "numEmpleado" to numEmpleado
                )
                val numEmpleadoNuevo = binding.etNumEmpleado.text.toString()

                db.collection("usuarios")
                    .whereEqualTo("numEmpleado", numEmpleadoNuevo)
                    .get()
                    .addOnSuccessListener { resultado ->
                        if (!resultado.isEmpty) {
                            Toast.makeText(this, "Ya existe un empleado con ese número", Toast.LENGTH_LONG).show()
                            return@addOnSuccessListener
                        }

                        // Aquí va tu código original de crear al empleado:
                        db.collection("usuarios").document(uid).set(datosEmpleado)
                            .addOnSuccessListener {
                                val subcolecciones = listOf("contrato", "controlHorario", "nominas", "permisos_bajas", "vacaciones")

                                for (coleccion in subcolecciones) {
                                    db.collection("usuarios").document(uid)
                                        .collection(coleccion)
                                        .document("inicial")
                                        .set(mapOf("estado" to "pendiente"))
                                }

                                Toast.makeText(this, "Empleado agregado correctamente", Toast.LENGTH_LONG).show()
                                binding.linearLayoutEditar.visibility = View.GONE
                                limpiarCampos()
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Error al agregar empleado", Toast.LENGTH_LONG).show()
                            }
                    }

            }.addOnFailureListener {
                Toast.makeText(this, "Error al crear cuenta del empleado", Toast.LENGTH_SHORT)
                    .show()
            }

    }
    private fun limpiarCampos() {
        binding.etNombreEmpleado.text?.clear()
        binding.etApellidosEmpleado.text?.clear()
        binding.etEmailEmpleado.text?.clear()
        binding.etNumTelefono.text?.clear()
        binding.etDirEmpleado.text?.clear()
        binding.etPuestoEmpleado.text?.clear()
        binding.etNumEmpleado.text?.clear()
        binding.etPassEmpleado.text?.clear()
    }


    private fun toolbar() {
        val barraHerramientas = binding.includeGestionEmpleados.toolbarComun
        setSupportActionBar(barraHerramientas)
        //CAMBIA EL TITLE DEL TOOLBAR
        supportActionBar?.title = "GESTIÓN EMPLEADOS"
        menu = ActionBarDrawerToggle(
            this,
            binding.menuGestionEmpleados,
            barraHerramientas,
            R.string.abrir_menu,
            R.string.cerrar_menu)
        binding.menuGestionEmpleados.addDrawerListener(menu)
        menu.syncState()
    }
    // Configurar el menú lateral
    private fun configurarMenuLateral() {
        //  puede agregar más configuraciones
    }
    // Manejar las opciones seleccionadas en el menú lateral
    private fun manejarOpcionesMenu() {

        binding.navViewGestion.setNavigationItemSelectedListener { opcion ->
            when (opcion.itemId) {

                R.id.menu_perfilEmpresa -> {
                    val intent = Intent(this, perfilEmpresario::class.java)
                    startActivity(intent)
                }

                R.id.menu_gestionEmpleados -> {
                    val intent = Intent(this, gestionEmpleados::class.java)
                    startActivity(intent)
                }

                R.id.menu_asistenciaEmpleado -> {
                    val intent = Intent(this, gestionControlAsistencia::class.java)
                    startActivity(intent)
                }

                R.id.menu_gestionVacaiones -> {
                    val intent = Intent(this, gestionVacaciones::class.java)
                    startActivity(intent)
                }

                R.id.menu_adjuntarDocumento -> {
                    val intent = Intent(this, documentosEmpleados::class.java)
                    startActivity(intent)
                }

                R.id.menu_cerrarSesionEmpresa -> {
                    finishAffinity()
                }
            }

            binding.menuGestionEmpleados.closeDrawer(GravityCompat.START)
            true
        }
    }
}