package com.example.neofichaje

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.neofichaje.databinding.ActivityPerfilEmpresarioBinding

class perfilEmpresario : AppCompatActivity(),OnClickListener {
    private lateinit var binding: ActivityPerfilEmpresarioBinding
    private lateinit var menu: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
       binding=ActivityPerfilEmpresarioBinding.inflate(layoutInflater)
        setContentView(binding.root)



        binding.btnEditarPerfilEmpresa.setOnClickListener(this)
        binding.btnCambioPass.setOnClickListener(this)
        toolbar()
        configurarMenuLateral()
        manejarOpcionesMenu()

    }
    private fun toolbar() {
        val barraHerramientas = binding.includePerfil.toolbarComun
        setSupportActionBar(barraHerramientas)
        // Cambiar el título del Toolbar
        supportActionBar?.title = "MI PERFIL"
        menu = ActionBarDrawerToggle(
            this,
            binding.menuPerfilEmpresario,  // Este es tu MENU ID
            barraHerramientas,  // Tu toolbar
            R.string.abrir_menu,
            R.string.cerrar_menu)
        binding.menuPerfilEmpresario.addDrawerListener(menu)
        menu.syncState()
    }
    override fun onClick(v: View?) {

        when (v?.id) {
            R.id.btn_editarPerfilEmpresa -> {
                acciones()
            }

            R.id.btnCambioPass -> {
                accionesContrasenia()
            }
        }
    }
    private fun acciones(){
        val intent= Intent(this, activity_empresario_perfil ::class.java)
        startActivity(intent)
    }
    private fun accionesContrasenia() {
        // Intent para el segundo botón (Cambiar Contraseña)
        val intentContrasenia = Intent(this, cambio_contrasenia ::class.java)
        startActivity(intentContrasenia)

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
                    finishAffinity()  // Cierra todas las actividades
                }
            }
            binding.menuPerfilEmpresario.closeDrawer(GravityCompat.START)  // Cerrar el menú lateral
            true
        }
    }
}