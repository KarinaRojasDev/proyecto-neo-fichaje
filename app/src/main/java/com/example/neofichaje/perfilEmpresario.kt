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
    private lateinit var editarPerfilLauncher: androidx.activity.result.ActivityResultLauncher<Intent>


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
        configurarEditarPerfilLauncher()

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
    private fun accionEditarPerfil() {
        val intentEditarPerfil = Intent(this, activity_empresario_perfil::class.java).apply {

            putExtra("nombreEmpresa", binding.tvNombreEmpresa.text.toString())
            putExtra("nif", binding.tvNif.text.toString())
            putExtra("emailEmpresa", binding.tvEmail.text.toString())
            putExtra("telefonoEmpresa", binding.tvFono.text.toString())
            putExtra("direccionEmpresa", binding.tvDir.text.toString())
            putExtra("web", binding.tvWww.text.toString())
            putExtra("nombre", binding.tvNombre.text.toString())
            putExtra("apellidos", binding.tvApellidos.text.toString())
            putExtra("email", binding.tvEmailAd.text.toString())
            putExtra("puesto", binding.tvPuesto.text.toString())
        }

        editarPerfilLauncher.launch(intentEditarPerfil)
    }
    private fun configurarEditarPerfilLauncher() {
        editarPerfilLauncher = registerForActivityResult(
            androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                val data = result.data
                val extras = data?.extras ?: return@registerForActivityResult

                binding.tvEmail.text = extras.getString("emailEmpresa", "")
                binding.tvFono.text = extras.getString("telefonoEmpresa", "")
                binding.tvDir.text = extras.getString("direccionEmpresa", "")
                binding.tvWww.text = extras.getString("web", "")
                binding.tvNombre.text = extras.getString("nombre", "")
                binding.tvApellidos.text = extras.getString("apellidos", "")
                binding.tvEmailAd.text = extras.getString("email", "")
                binding.tvPuesto.text = extras.getString("puesto", "")

            }
        }
    }

    private fun accionesContrasenia() {
        val intentContrasenia = Intent(this, cambio_contrasenia ::class.java)
        startActivity(intentContrasenia)
    }
    private fun cargarDatosPerfilDesdeFirebase() {
        val uid = auth.currentUser?.uid ?: return
        val usuarioRef = db.collection("usuarios").document(uid)

        usuarioRef.get().addOnSuccessListener { doc ->
            if (doc.exists()) {
                val empresaId = doc.getString("empresa_id") ?: return@addOnSuccessListener
                cargarDatosEmpresa(empresaId)

                // Datos del admin (puedes mostrarlos también)
                binding.tvNombre.text = doc.getString("nombre")
                binding.tvApellidos.text = doc.getString("apellidos")
                binding.tvEmailAd.text = doc.getString("email")
                binding.tvPuesto.text = doc.getString("puesto")
            }
        }
    }
    private fun cargarDatosEmpresa(empresaId: String) {
        val empresaRef = db.collection("empresas").document(empresaId)

        empresaRef.get().addOnSuccessListener { empresa ->
            if (empresa.exists()) {
                binding.tvNombreEmpresa.text = empresa.getString("nombreEmpresa")
                binding.tvNif.text = empresa.getString("nif")
                binding.tvEmail.text = empresa.getString("emailEmpresa")
                binding.tvFono.text = empresa.getString("telefonoEmpresa")
                binding.tvDir.text = empresa.getString("direccionEmpresa")
                binding.tvWww.text = empresa.getString("web")
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Error al cargar datos de empresa", Toast.LENGTH_SHORT).show()
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

