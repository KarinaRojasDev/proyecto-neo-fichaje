package com.example.neofichaje

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.neofichaje.databinding.ActivityEmpresarioPerfilBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class activity_empresario_perfil : AppCompatActivity(),OnClickListener {
    private lateinit var binding: ActivityEmpresarioPerfilBinding
    private lateinit var menu: ActionBarDrawerToggle
    private lateinit var puesto:ArrayList<CharSequence>
    private lateinit var adapterPuesto: ArrayAdapter<CharSequence>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityEmpresarioPerfilBinding.inflate(layoutInflater)
        setContentView(binding.root)

        toolbar()
        manejarOpcionesMenu()
        instancias()
        cargarDatosDesdeIntent()
        cargarDatosPerfilDesdeFirebase()
        binding.btnGuardarCambios.setOnClickListener(this)

    }

    private fun cargarDatosDesdeIntent() {
        val extras = intent.extras
        if (extras != null) {
            binding.tvNombreEmpresa.text = extras.getString("nombreEmpresa", "")
            binding.tvNif.text = extras.getString("nif", "")

            binding.etEmailEmpresa.setText(extras.getString("emailEmpresa", ""))
            binding.etFonoEmpresa.setText(extras.getString("telefonoEmpresa", ""))
            binding.etDirEmpresa.setText(extras.getString("direccionEmpresa", ""))
            binding.etWebEmpresa.setText(extras.getString("web", ""))
            binding.etNombreAdmin.setText(extras.getString("nombre", ""))
            binding.etApellidoAdmin.setText(extras.getString("apellidos", ""))
            binding.etEmailAdmin.setText(extras.getString("email", ""))

            // Para el Spinner
            val puestoRecibido = extras.getString("puesto", "")
            val posicionSeleccionada = puesto.indexOf(puestoRecibido)
            if (posicionSeleccionada >= 0) {
                binding.spinnerPuesto.setSelection(posicionSeleccionada)
            }
        }
    }


    private fun instancias() {
        puesto= arrayListOf("Seleccionar puesto","Gerente","Administrador")
        adapterPuesto=ArrayAdapter(applicationContext,android.R.layout.simple_spinner_item,puesto)
        adapterPuesto.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerPuesto.adapter=adapterPuesto
    }

    private fun toolbar() {
        val barraHerramientas = binding.includeEditarPerfil.toolbarComun
        setSupportActionBar(barraHerramientas)

        // Cambiar el título del Toolbar
        supportActionBar?.title = "EDITAR PERFIL"
        menu = ActionBarDrawerToggle(
            this,
            binding.menuEditarPerfil,
            barraHerramientas,
            R.string.abrir_menu,
            R.string.cerrar_menu
        )

        binding.menuEditarPerfil.addDrawerListener(menu)
        menu.syncState()

    }
    override fun onClick(v: View?) {
        when(v?.id){
            binding.btnGuardarCambios.id ->{
                Toast.makeText(this, "Botón pulsado", Toast.LENGTH_SHORT).show()
                clicBoton()
            }
        }
    }
    private fun clicBoton() {
        if (binding.etEmailEmpresa.text.isEmpty() || binding.etNombreAdmin.text.isEmpty() ||
            binding.spinnerPuesto.selectedItem == null ||
            binding.spinnerPuesto.selectedItem.toString() == "Seleccionar puesto") {
            Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val ref = com.google.firebase.firestore.FirebaseFirestore.getInstance().collection("usuarios").document(uid)

        val emailEmpresa = binding.etEmailEmpresa.text.toString()
        val fono = binding.etFonoEmpresa.text.toString()
        val direccion = binding.etDirEmpresa.text.toString()
        val sitioWeb = binding.etWebEmpresa.text.toString()
        val nombreAdmin = binding.etNombreAdmin.text.toString()
        val apellidos = binding.etApellidoAdmin.text.toString()
        val emailAdmin = binding.etEmailAdmin.text.toString()
        val puesto = binding.spinnerPuesto.selectedItem.toString()

        val updates = mapOf(
            "emailEmpresa" to emailEmpresa,
            "telefonoEmpresa" to fono,
            "direccionEmpresa" to direccion,
            "web" to sitioWeb,
            "nombre" to nombreAdmin,
            "apellidos" to apellidos,
            "email" to emailAdmin,
            "puesto" to puesto
        )

        ref.update(updates).addOnSuccessListener {
            Toast.makeText(this, "Perfil actualizado correctamente ", Toast.LENGTH_SHORT).show()

            val bundle = Bundle().apply {
                putString("emailEmpresa", emailEmpresa)
                putString("telefonoEmpresa", fono)
                putString("direccionEmpresa", direccion)
                putString("web", sitioWeb)
                putString("nombre", nombreAdmin)
                putString("apellidos", apellidos)
                putString("email", emailAdmin)
                putString("puesto", puesto)
            }

            val intent = Intent().apply {
                putExtras(bundle)
            }

            setResult(RESULT_OK, intent)
            finish()

        }.addOnFailureListener {
            Toast.makeText(this, "Error al actualizar perfil ", Toast.LENGTH_SHORT).show()
        }
    }


    private fun cargarDatosPerfilDesdeFirebase() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val ref = FirebaseDatabase.getInstance().getReference("usuarios").child(uid)

        ref.get().addOnSuccessListener { snapshot ->
            binding.etEmailEmpresa.setText(snapshot.child("emailEmpresa").value.toString())
            binding.etFonoEmpresa.setText(snapshot.child("telefonoEmpresa").value.toString())
            binding.etDirEmpresa.setText(snapshot.child("direccionEmpresa").value.toString())
            binding.etWebEmpresa.setText(snapshot.child("web").value.toString())
            binding.etNombreAdmin.setText(snapshot.child("nombre").value.toString())
            binding.etApellidoAdmin.setText(snapshot.child("apellidos").value.toString())
            binding.etEmailAdmin.setText(snapshot.child("email").value.toString())

            val puestoActual = snapshot.child("puesto").value.toString()
            val index = puesto.indexOf(puestoActual)
            if (index != -1) {
                binding.spinnerPuesto.setSelection(index)
            }
        }
    }

    private fun manejarOpcionesMenu() {
        binding.navViewEmpresario.setNavigationItemSelectedListener { opcion ->
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

            binding.menuEditarPerfil.closeDrawer(GravityCompat.START)
            true
        }
    }
}