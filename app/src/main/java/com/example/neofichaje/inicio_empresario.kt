package com.example.neofichaje

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
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
}