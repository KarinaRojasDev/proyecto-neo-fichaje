package com.example.neofichaje

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import com.example.neofichaje.databinding.ActivityInicioEmpresarioBinding

class inicio_empresario : AppCompatActivity() {
    private lateinit var binding: ActivityInicioEmpresarioBinding
    private lateinit var menu: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInicioEmpresarioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        configurarToolbar()
        configurarMenuLateral()
        manejarOpcionesMenu()

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

    private fun configurarMenuLateral() {
        // Puedes agregar más configuraciones aquí si deseas en el futuro
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

            binding.inicioEmpresario.closeDrawer(GravityCompat.START)
            true
        }
    }
}