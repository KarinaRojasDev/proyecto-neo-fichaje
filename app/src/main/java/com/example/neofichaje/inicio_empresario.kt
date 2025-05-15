package com.example.neofichaje

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import com.example.neofichaje.databinding.ActivityInicioEmpresarioBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class inicio_empresario : AppCompatActivity() {
    private lateinit var binding: ActivityInicioEmpresarioBinding
    private lateinit var menu: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInicioEmpresarioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        configurarToolbar()
        manejarOpcionesMenu()
        crearSubcolecciones()
        mostrarNotificacionPermisos()
        mostrarNotificacionVacaciones()

    }
    private fun configurarToolbar() {
        val barraHerramientas = binding.includeInicioEmpresario.toolbarComun
        setSupportActionBar(barraHerramientas)

        // Cambiar el título del Toolbar
        supportActionBar?.title = "INICIO"

        menu = ActionBarDrawerToggle(
            this,
            binding.inicioEmpresario,
            barraHerramientas,
            R.string.abrir_menu,
            R.string.cerrar_menu
        )

        binding.inicioEmpresario.addDrawerListener(menu)
        menu.syncState()
    }
    private fun mostrarNotificacionPermisos() {
        val db = FirebaseFirestore.getInstance()
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        db.collection("usuarios").document(uid).get().addOnSuccessListener { document ->
            val empresaId = document.getString("empresa_id") ?: return@addOnSuccessListener

            db.collection("empresas").document(empresaId)
                .collection("gestionPermisos_bajas")
                .whereEqualTo("estadoLectura", "pendiente")
                .limit(1)
                .get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        val doc = documents.first()
                        val nombreEmpleado = doc.getString("nombreEmpleado") ?: "Empleado"
                        val tipo = doc.getString("tipo") ?: "Permiso"
                        val fechaInicio = doc.getString("fechaInicio") ?: ""
                        val fechaFin = doc.getString("fechaFin") ?: ""
                        val uidEmpleado = doc.getString("uidEmpleado") ?: return@addOnSuccessListener
                        val empresaDocId = doc.id

                        val mensaje = if (tipo == "Permiso") {
                            "$nombreEmpleado ha solicitado permiso del $fechaInicio al $fechaFin"
                        } else {
                            "$nombreEmpleado ha marcado baja del $fechaInicio al $fechaFin"
                        }

                        binding.solicitudPermiso.visibility = View.VISIBLE
                        binding.solicitudPermisoEmpleado.text = mensaje

                        binding.solicitudPermiso.setOnClickListener {
                            // Cambia estadoLectura en empresa
                            db.collection("empresas").document(empresaId)
                                .collection("gestionPermisos_bajas")
                                .document(empresaDocId)
                                .update("estadoLectura", "Leído")

                            // Cambia estadoLectura en usuario
                            db.collection("usuarios").document(uidEmpleado)
                                .collection("permisos_bajas")
                                .whereEqualTo("estadoLectura", "pendiente")
                                .limit(1)
                                .get()
                                .addOnSuccessListener { permisosDocs ->
                                    if (!permisosDocs.isEmpty) {
                                        val userPermisoDocId = permisosDocs.first().id
                                        db.collection("usuarios").document(uidEmpleado)
                                            .collection("permisos_bajas")
                                            .document(userPermisoDocId)
                                            .update("estadoLectura", "Leído")
                                    }
                                }

                            // 3️Envia notificación al empleado
                            db.collection("usuarios").document(uidEmpleado)
                                .update("tvSolicitudLeida", "Administrador ha aceptado tu solicitud de $tipo del $fechaInicio al $fechaFin.")

                            // Oculta notificación en la app empresario
                            binding.solicitudPermiso.visibility = View.GONE
                        }
                    } else {
                        binding.solicitudPermiso.visibility = View.GONE
                    }
                }
        }
    }
    private fun mostrarNotificacionVacaciones() {
        val db = FirebaseFirestore.getInstance()
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        db.collection("usuarios").document(uid).get().addOnSuccessListener { document ->
            val empresaId = document.getString("empresa_id") ?: return@addOnSuccessListener

            db.collection("empresas").document(empresaId).get().addOnSuccessListener { empresaDoc ->
                val notiVacaciones = empresaDoc.getString("tvVacacionesEmpresa")

                if (!notiVacaciones.isNullOrEmpty()) {
                    binding.notificationVacacionesEmpresa.visibility = View.VISIBLE
                    binding.tvVacacionesEmpresa.text = notiVacaciones
                    binding.notificationVacacionesEmpresa.setOnClickListener {
                    // Limpiar la notificación antes de abrir la pantalla
                        db.collection("empresas").document(empresaId)
                            .update("tvVacacionesEmpresa", "")
                            .addOnSuccessListener {
                                // Después de limpiar, abrir pantalla
                                startActivity(Intent(this, gestionVacaciones::class.java))
                            }
                    }

                } else {
                    binding.notificationVacacionesEmpresa.visibility = View.GONE
                }
            }
        }
    }

    private fun crearSubcolecciones() {
        val db = FirebaseFirestore.getInstance()
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        // Obtener el documento del usuario para verificar su rol
        val usuarioRef = db.collection("usuarios").document(uid)

        usuarioRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                val rol = document.getString("puesto") // Asegúrate de tener el campo "puesto"

                // Si el rol es "administrador" o "empresario", creamos las subcolecciones
                if (rol == "Administrador" ) {
                    val subcolecciones = listOf(
                        "gestionEmpleados",
                        "gestionVacaciones",
                        "gestionAsistencia",
                        "adjuntarArchivos",
                        "perfilEmpresa"
                    )

                    for (coleccion in subcolecciones) {
                        val docRef = db.collection("usuarios").document(uid).collection(coleccion)

                        // Verificamos si ya existe al menos un documento en la subcolección
                        docRef.limit(1).get().addOnSuccessListener { snapshot ->
                            if (snapshot.isEmpty) {
                                // Puedes inicializar los documentos con algún dato básico
                                val datoInicial = hashMapOf("init" to true)
                                docRef.add(datoInicial)
                                    .addOnSuccessListener {
                                        Toast.makeText(this, "Subcolección '$coleccion' creada", Toast.LENGTH_SHORT).show()
                                    }
                                    .addOnFailureListener { e ->
                                        Toast.makeText(this, "Error en '$coleccion': ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                            }
                        }
                    }
                } else {
                    // Si el rol no es administrador o empresario, no hacemos nada
                    Toast.makeText(this, "El usuario no tiene el rol adecuado para crear subcolecciones.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Usuario no encontrado.", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener { e ->
            Toast.makeText(this, "Error al obtener datos del usuario: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    private fun manejarOpcionesMenu() {
        binding.navViewEmpresario.setNavigationItemSelectedListener { opcion ->
            when (opcion.itemId) {
                R.id.inicioEmpresario -> {
                    val intent = Intent(this, inicio_empresario::class.java)
                    startActivity(intent)
                }
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

            binding.inicioEmpresario.closeDrawer(GravityCompat.START)
            true
        }
    }
}
