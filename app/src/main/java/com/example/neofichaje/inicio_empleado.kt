package com.example.neofichaje

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
        val barraHerramientas = binding.includeEmpleado.toolbarEmpleado
        setSupportActionBar(barraHerramientas)

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
                R.id.nav_item_inicioEmpleado -> mostrarMensaje("Fichar")
                R.id.nav_item_vacaEmpleado -> mostrarMensaje("Vacaciones")
                R.id.nav_item_permisoEmpleado -> mostrarMensaje("Permiso/Baja")
                R.id.nav_item_nominaEmpleado -> mostrarMensaje("Nóminas")
                R.id.nav_item_contratoEmpleado -> mostrarMensaje("Contrato")
                R.id.nav_item_cerrarEmpleado -> mostrarMensaje("Cerrar sesión")
            }

            binding.inicioEmpleado.closeDrawer(GravityCompat.START)
            true
        }
    }

    private fun mostrarMensaje(texto: String) {
        Toast.makeText(this, texto, Toast.LENGTH_SHORT).show()
    }

}