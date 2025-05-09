package com.example.neofichaje

import android.content.Intent
import android.os.Bundle
import android.view.View
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


        configurarToolbar()
        manejarOpcionesMenu()
        mostrarNotificacionesEmpleado()

    }
    private fun mostrarNotificacionesEmpleado() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        db.collection("usuarios").document(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null || !snapshot.exists()) {
                    Toast.makeText(this, "Error al cargar notificaciones", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                val notiNomina = snapshot.getString("tvNominas")
                val notiContrato = snapshot.getString("tvContrato")

                if (!notiNomina.isNullOrBlank()) {
                    binding.nomina.visibility = View.VISIBLE
                    binding.tvNominas.text = notiNomina
                    binding.nomina.setOnClickListener {
                        startActivity(Intent(this, nominas_empleado::class.java))
                        limpiarNotificacionNomina() // ← limpiar cuando hace clic
                    }
                } else {
                    binding.nomina.visibility = View.GONE
                }

                if (!notiContrato.isNullOrBlank()) {
                    binding.contrato.visibility = View.VISIBLE
                    binding.tvContrato.text = notiContrato
                    binding.contrato.setOnClickListener {
                        startActivity(Intent(this, contratoEmpleado::class.java))
                        limpiarNotificacionContrato()
                    }
                } else {
                    binding.contrato.visibility = View.GONE
                }
            }
    }
    private fun limpiarNotificacionNomina() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseFirestore.getInstance()
            .collection("usuarios")
            .document(uid)
            .update("tvNominas", "")
    }
    private fun limpiarNotificacionContrato() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseFirestore.getInstance()
            .collection("usuarios")
            .document(uid)
            .update("tvContrato", "")
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
}