package com.example.neofichaje

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.neofichaje.databinding.ActivityInicioEmpleadoBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class inicio_empleado : AppCompatActivity() {
    private lateinit var binding: ActivityInicioEmpleadoBinding
    private lateinit var menu: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityInicioEmpleadoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        crearSubcolecciones()
        configurarToolbar()
        configurarMenuLateral()
        manejarOpcionesMenu()
    }
    private fun configurarToolbar() {
        val barraHerramientas = binding.includeInicioEmpleado.toolbarComun
        setSupportActionBar(barraHerramientas)

        // Cambiar el título del Toolbar
        supportActionBar?.title = "INICIO"

        menu = ActionBarDrawerToggle(
            this,
            binding.inicioEmpleado, barraHerramientas,
            R.string.abrir_menu,
            R.string.cerrar_menu
        )

        binding.inicioEmpleado.addDrawerListener(menu)
        menu.syncState()
    }

    private fun configurarMenuLateral() {
        //  configurar el NavigationView
    }

    private fun manejarOpcionesMenu() {

        binding.navView.setNavigationItemSelectedListener { opcion ->
            when (opcion.itemId) {

                R.id.menu_fichaje -> {
                    val intent = Intent(this, empleado_control_horario::class.java)
                    startActivity(intent)
                }

                R.id.menu_vacaEmpleado -> {
                    val intent = Intent(this, empleado_solicitud_vacaciones::class.java)
                    startActivity(intent)
                }

                R.id.menu_permisoEmpleado -> {
                    val intent = Intent(this, permisoEmpleado::class.java)
                    startActivity(intent)
                }

                R.id.menu_nominaEmpleado -> {
                    val intent = Intent(this, nominas_empleado::class.java)
                    startActivity(intent)
                }

                R.id.menu_contratoEmpleado -> {
                    val intent = Intent(this, contratoEmpleado::class.java)
                    startActivity(intent)
                }

                R.id.menu_cerrarEmpleado -> {
                    finishAffinity()
                }
            }

            binding.inicioEmpleado.closeDrawer(GravityCompat.START)
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
                val rol = document.getString("puesto") // Asegúrate de tener el campo "rol"

                // Si el rol es "tecnico", creamos las subcolecciones
                if (rol == "tecnico") {
                    val subcolecciones = listOf("vacaciones", "controlHorario", "misNominas", "miContrato", "permisosBajas")

                    for (coleccion in subcolecciones) {
                        val docRef = db.collection("usuarios").document(uid).collection(coleccion)

                        // Verificamos si ya existe al menos un documento en la subcolección
                        docRef.limit(1).get().addOnSuccessListener { snapshot ->
                            if (snapshot.isEmpty) {
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
                    // Si el rol no es técnico, no hacemos nada
                    Toast.makeText(this, "El usuario no es técnico, no se crearon subcolecciones.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}