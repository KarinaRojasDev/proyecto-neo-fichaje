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

class inicio_empleado : AppCompatActivity() {
    private lateinit var binding: ActivityInicioEmpleadoBinding
    private lateinit var menu: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityInicioEmpleadoBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
        // Si necesitas algo adicional en el futuro para configurar el NavigationView, puedes usar este método.
        // Por ahora lo dejamos vacío o como placeholder si quieres.
    }

    private fun manejarOpcionesMenu() {
        binding.navView.setNavigationItemSelectedListener { opcion ->
            when (opcion.itemId) {
                R.id.menu_fichaje -> startActivity(Intent(this, empleado_control_horario::class.java))
                R.id.menu_vacaEmpleado -> startActivity(Intent(this, empleado_solicitud_vacaciones::class.java))
                R.id.menu_permisoEmpleado -> startActivity(Intent(this, permisoEmpleado::class.java))
                R.id.menu_nominaEmpleado -> startActivity(Intent(this, nominas_empleado::class.java))
                R.id.menu_contratoEmpleado -> startActivity(Intent(this, contratoEmpleado::class.java))
                R.id.menu_cerrarEmpleado -> finishAffinity()
            }

            binding.inicioEmpleado.closeDrawer(GravityCompat.START)
            true
        }
    }
}