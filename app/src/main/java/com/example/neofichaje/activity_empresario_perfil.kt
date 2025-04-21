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
        cargarDatosPerfilDesdeFirebase()
        binding.btnIniSesionEmpresa.setOnClickListener(this)

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
            binding.btnIniSesionEmpresa.id ->{
                clicBoton()
            }
        }
    }
    private fun clicBoton(){
        if (binding.etEmailEmpresa.text.isEmpty() || binding.etNombreEmpresario.text.isEmpty()||
            binding.spinnerPuesto.selectedItem == null ||
            binding.spinnerPuesto.selectedItem.toString() == "Seleccionar puesto") {
            Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val ref = FirebaseDatabase.getInstance().getReference("usuarios").child(uid)

        val updates = mapOf(
            "email_empresa" to binding.etEmailEmpresa.text.toString(),
            "fono_empresa" to binding.etFonoEmpresa.text.toString(), // si lo usas
            "direccion" to binding.etDirEmpresa.text.toString(),
            "sitio_web" to binding.etWebEmpresa.text.toString(),
            "nombre_admin" to binding.etNombreEmpresario.text.toString(),
            "apellidos" to binding.etApellidoEmpresario.text.toString(),
            "email_admin" to binding.etEmailEmpresario.text.toString(),
            "puesto" to binding.spinnerPuesto.selectedItem.toString()
        )


        ref.updateChildren(updates).addOnSuccessListener {
            Toast.makeText(this, "Perfil actualizado correctamente", Toast.LENGTH_SHORT).show()
            cargarDatosPerfilDesdeFirebase() // volvés a la pantalla anterior
        }.addOnFailureListener {
            Toast.makeText(this, "Error al actualizar perfil", Toast.LENGTH_SHORT).show()
        }
    }
    private fun cargarDatosPerfilDesdeFirebase() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val ref = FirebaseDatabase.getInstance().getReference("usuarios").child(uid)

        ref.get().addOnSuccessListener { snapshot ->
            binding.etEmailEmpresa.setText(snapshot.child("email_empresa").value.toString())
            binding.etFonoEmpresa.setText(snapshot.child("fono_empresa").value.toString())
            binding.etDirEmpresa.setText(snapshot.child("direccion").value.toString())
            binding.etWebEmpresa.setText(snapshot.child("sitio_web").value.toString())
            binding.etNombreEmpresario.setText(snapshot.child("nombre_admin").value.toString())
            binding.etApellidoEmpresario.setText(snapshot.child("apellidos").value.toString())
            binding.etEmailEmpresario.setText(snapshot.child("email_admin").value.toString())

            val puestoActual = snapshot.child("puesto").value.toString()
            val index = puesto.indexOf(puestoActual)
            if (index != -1) {
                binding.spinnerPuesto.setSelection(index)
            }

            verificarPermisoEdicion(puestoActual)
        }
    }
    private fun verificarPermisoEdicion(puesto: String) {
        val editable = (puesto == "Gerente" || puesto == "Administrador")

        binding.etEmailEmpresa.isEnabled = editable
        binding.etFonoEmpresa.isEnabled = editable
        binding.etDirEmpresa.isEnabled = editable
        binding.etWebEmpresa.isEnabled = editable
        binding.etNombreEmpresario.isEnabled = editable
        binding.etApellidoEmpresario.isEnabled = editable
        binding.etEmailEmpresario.isEnabled = editable
        binding.spinnerPuesto.isEnabled = editable
        binding.btnIniSesionEmpresa.isEnabled = editable

        if (!editable) {
            Toast.makeText(this, "Solo un Gerente o Administrador puede editar el perfil", Toast.LENGTH_LONG).show()
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