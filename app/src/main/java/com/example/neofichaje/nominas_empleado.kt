package com.example.neofichaje

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.neofichaje.databinding.ActivityNominasEmpleadoBinding

class nominas_empleado : AppCompatActivity() {
    private lateinit var binding: ActivityNominasEmpleadoBinding
    private lateinit var menu: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNominasEmpleadoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        toolbar()
        configurarMenuLateral()
        manejarOpcionesMenu()
    }
    private fun toolbar() {
        val barraHerramientas = binding.includeNominas.toolbarComun
        setSupportActionBar(barraHerramientas)
        // Cambiar el título del Toolbar
        supportActionBar?.title = "MIS NÓMINAS"
        menu = ActionBarDrawerToggle(
            this,
            binding.menuNominasEmpleado,  // Este es tu MENU ID
            barraHerramientas,  // Tu toolbar
            R.string.abrir_menu,
            R.string.cerrar_menu)
        binding.menuNominasEmpleado.addDrawerListener(menu)
        menu.syncState()
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

            binding.menuNominasEmpleado.closeDrawer(GravityCompat.START)  // Cerrar el menú lateral
            true
        }
    }
}