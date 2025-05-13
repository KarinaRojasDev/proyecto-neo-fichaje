package com.example.neofichaje

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.neofichaje.adapter.DocumentoAdapter
import com.example.neofichaje.databinding.ActivityNominasEmpleadoBinding
import com.example.neofichaje.model.Documento
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class nominas_empleado : AppCompatActivity() {
    private lateinit var binding: ActivityNominasEmpleadoBinding
    private lateinit var menu: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        binding = ActivityNominasEmpleadoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        configurarToolbar()
        manejarOpcionesMenu()
        cargarNominas()
    }

    private fun configurarToolbar() {
        val barraHerramientas = binding.includeNominasEmpleado.toolbarComun
        setSupportActionBar(barraHerramientas)
        supportActionBar?.title = "NÓMINAS"

        menu = ActionBarDrawerToggle(
            this,
            binding.menuNomina,
            barraHerramientas,
            R.string.abrir_menu,
            R.string.cerrar_menu
        )
        binding.menuNomina.addDrawerListener(menu)
        menu.syncState()
    }


    private fun manejarOpcionesMenu() {
        binding.navViewGestion.setNavigationItemSelectedListener { opcion ->
            when (opcion.itemId) {
                R.id.inicioEmpleado-> startActivity(Intent(this,inicio_empleado::class.java))
                R.id.menu_fichaje -> startActivity(Intent(this, empleado_control_horario::class.java))
                R.id.menu_vacaEmpleado -> startActivity(Intent(this, empleado_solicitud_vacaciones::class.java))
                R.id.menu_permisoEmpleado -> startActivity(Intent(this, permisoEmpleado::class.java))
                R.id.menu_nominaEmpleado -> startActivity(Intent(this, nominas_empleado::class.java))
                R.id.menu_contratoEmpleado -> startActivity(Intent(this, contratoEmpleado::class.java))
                R.id.menu_cerrarEmpleado -> finishAffinity()
            }

            binding.menuNomina.closeDrawer(androidx.core.view.GravityCompat.START)
            true
        }
    }

    private fun cargarNominas() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        db.collection("usuarios")
            .document(uid)
            .collection("nominas")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    Toast.makeText(this, "Error al cargar nóminas", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                val listaNominas = mutableListOf<Documento>()
                snapshot.documents.forEach { doc ->
                    val documento = Documento(
                        id = doc.id,
                        nombreArchivo = doc.getString("nombreArchivo") ?: "",
                        url = doc.getString("url") ?: "",
                        tituloDocumento = doc.getString("tituloDocumento") ?: ""
                    )
                    if (documento.url.isNotEmpty()) {
                        listaNominas.add(documento)
                    }
                }
                if (listaNominas.isEmpty()) {
                    binding.recyclerViewNominas.visibility = View.GONE
                    binding.tvMensajeVacio.visibility = View.VISIBLE
                } else {
                    binding.tvMensajeVacio.visibility = View.GONE
                    val adapter = DocumentoAdapter(this, listaNominas, "nominas")
                    binding.recyclerViewNominas.apply {
                        layoutManager = LinearLayoutManager(this@nominas_empleado)
                        this.adapter = adapter
                        visibility = View.VISIBLE
                    }
                }
            }
    }
}
