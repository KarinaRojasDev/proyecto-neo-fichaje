package com.example.neofichaje

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.neofichaje.databinding.ActivityPerfilEmpresarioBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore

class perfilEmpresario : AppCompatActivity(),OnClickListener {
    private lateinit var binding: ActivityPerfilEmpresarioBinding
    private lateinit var menu: ActionBarDrawerToggle
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityPerfilEmpresarioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnEditarPerfilEmpresa.setOnClickListener(this)
        binding.btnCambioPass.setOnClickListener(this)
        toolbar()
        manejarOpcionesMenu()

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        cargarDatosPerfilDesdeFirebase()


    }
    private fun toolbar() {
        val barraHerramientas = binding.includePerfil.toolbarComun
        setSupportActionBar(barraHerramientas)
        // Cambiar el título del Toolbar
        supportActionBar?.title = "MI PERFIL"
        menu = ActionBarDrawerToggle(
            this,
            binding.menuPerfilEmpresario,
            barraHerramientas,
            R.string.abrir_menu,
            R.string.cerrar_menu)
        binding.menuPerfilEmpresario.addDrawerListener(menu)
        menu.syncState()
    }
    override fun onClick(v: View?) {
        when (v?.id) {
            binding.btnEditarPerfilEmpresa.id -> {
                accionEditarPerfil()
            }
            binding.btnCambioPass.id -> {
                accionesContrasenia()
            }
        }
    }
    private fun accionEditarPerfil(){
        val intentEditarPerfil= Intent(this, activity_empresario_perfil ::class.java)
        startActivity(intentEditarPerfil)
    }
    private fun accionesContrasenia() {
        val intentContrasenia = Intent(this, cambio_contrasenia ::class.java)
        startActivity(intentContrasenia)
    }
    private fun cargarDatosPerfilDesdeFirebase() {
        val uid = auth.currentUser?.uid ?: return
        val usuarioRef = db.collection("usuarios").document(uid)

        usuarioRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                val nombreEmpresa = document.getString("nombre_empresa")
                val nif = document.getString("nif")
                val email = document.getString("email_empresa")
                val fono = document.getString("fono")
                val direccion = document.getString("direccion")
                val sitioWeb = document.getString("sitio_web")
                val nombreAdmin = document.getString("nombre_admin")
                val apellidos = document.getString("apellidos")
                val emailAdmin = document.getString("email_admin")
                val puesto = document.getString("puesto") ?: ""


                // Asignar los datos al TextView correspondiente
                binding.tvNombreEmpresa.text = nombreEmpresa
                binding.tvNif.text = nif
                binding.tvEmail.text = email
                binding.tvFono.text = fono
                binding.tvDir.text = direccion
                binding.tvWww.text = sitioWeb
                binding.tvNombre.text = nombreAdmin
                binding.tvApellidos.text = apellidos
                binding.tvEmailAd.text = emailAdmin
                binding.tvPuesto.text = puesto

                verificarPermisoEdicion(puesto)
            }
        }.addOnFailureListener { exception ->
            Toast.makeText(this, "Error al cargar datos: ${exception.message}", Toast.LENGTH_SHORT).show()
        }
    }
    // Implementación de la función para verificar permisos
    private fun verificarPermisoEdicion(puesto: String) {
        // Suponiendo que solo los Gerentes pueden editar el perfil
        if (puesto == "Administrador" || puesto=="Gerente") {
            binding.btnEditarPerfilEmpresa.isEnabled = true
        } else {
            binding.btnEditarPerfilEmpresa.isEnabled = false
        }
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
            binding.menuPerfilEmpresario.closeDrawer(GravityCompat.START)
            true
        }
    }
}