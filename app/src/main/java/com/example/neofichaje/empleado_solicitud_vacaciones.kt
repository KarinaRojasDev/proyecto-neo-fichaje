package com.example.neofichaje

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.neofichaje.databinding.ActivityEmpleadoSolicitudVacacionesBinding

class empleado_solicitud_vacaciones : AppCompatActivity() {

    private lateinit var binding: ActivityEmpleadoSolicitudVacacionesBinding
    private lateinit var menu: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityEmpleadoSolicitudVacacionesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        toolbar()
        configurarMenuLateral()
        manejarOpcionesMenu()
    }
    private fun toolbar(){
        val barraHerramientas = binding.includeVacaciones.toolbarComun
        setSupportActionBar(barraHerramientas)

        // Cambiar el título del Toolbar
        supportActionBar?.title = "SOLICITUD DE VACACIONES"
        menu = ActionBarDrawerToggle(
            this,
            binding.menuSolicitudVaca, barraHerramientas,
            R.string.abrir_menu,
            R.string.cerrar_menu
        )

        binding.menuSolicitudVaca.addDrawerListener(menu)
        menu.syncState()

    }
    private fun configurarMenuLateral() {
        // Si necesitas algo adicional en el futuro para configurar el NavigationView, puedes usar este método.
        // Por ahora lo dejamos vacío o como placeholder si quieres.
    }

    private fun manejarOpcionesMenu() {

        binding.navView.setNavigationItemSelectedListener { opcion ->
            when (opcion.itemId) {

                R.id.menu_fichaje -> {
                    val intent = Intent(this, empleado_control_horario::class.java)
                    startActivity(intent)
                }

                R.id.menu_vacaEmpleado -> {
                    val intent = Intent(this, empleado_solicitud_vacaciones::class.java)
                    startActivity(intent)
                }

                R.id.menu_permisoEmpleado -> {
                    val intent = Intent(this, permisoEmpleado::class.java)
                    startActivity(intent)
                }

                R.id.menu_nominaEmpleado -> {
                    val intent = Intent(this, nominas_empleado::class.java)
                    startActivity(intent)
                }

                R.id.menu_contratoEmpleado -> {
                    val intent = Intent(this, contratoEmpleado::class.java)
                    startActivity(intent)
                }

                R.id.menu_cerrarEmpleado -> {
                    finishAffinity()
                }
            }
            binding.menuSolicitudVaca.closeDrawer(GravityCompat.START)
            true
        }
    }
}