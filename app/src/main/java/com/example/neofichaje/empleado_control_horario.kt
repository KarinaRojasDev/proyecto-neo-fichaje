package com.example.neofichaje

import android.annotation.SuppressLint
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
import com.example.neofichaje.databinding.ActivityEmpleadoControlHorarioBinding
import com.prolificinteractive.materialcalendarview.MaterialCalendarView

class empleado_control_horario : AppCompatActivity(),OnClickListener {
    private  lateinit var binding: ActivityEmpleadoControlHorarioBinding
    private lateinit var menu: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding= ActivityEmpleadoControlHorarioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        toolbar()
        configurarMenuLateral()
        manejarOpcionesMenu()
        mostrarFechaActual()
        configurarCalendarios()

        binding.tvInicioFecha.setOnClickListener(this)
        binding.tvFinFecha.setOnClickListener(this)
    }
    override fun onClick(v: View?) {
        when(v?.id){
            binding.tvInicioFecha.id ->{
                toggleCalendario(binding.calendarioFecha)
            }
            binding.tvFinFecha.id->{
                toggleCalendario(binding.calendario2Fecha)
            }
        }
    }
    @SuppressLint("SetTextI18n")
    private fun configurarCalendarios() {
        binding.calendarioFecha.setOnDateChangedListener { _, date, _ ->
            binding.tvInicioFecha.text = "Fecha: ${date.day}/${date.month}/${date.year}"
            binding.calendarioFecha.visibility = View.GONE
        }

        binding.calendario2Fecha.setOnDateChangedListener { _, date, _ ->
            binding.tvFinFecha.text = "Fecha: ${date.day}/${date.month}/${date.year}"
            binding.calendario2Fecha.visibility = View.GONE
        }
    }

    private fun toggleCalendario(calendario: MaterialCalendarView) {
        calendario.visibility = if (calendario.visibility == View.GONE) View.VISIBLE else View.GONE
    }

    @SuppressLint("SetTextI18n")
    private fun mostrarFechaActual() {
        val hoy = com.prolificinteractive.materialcalendarview.CalendarDay.today()
        binding.tvInicioFecha.text = "Fecha: ${hoy.day}/${hoy.month}/${hoy.year}"
        binding.tvFinFecha.text = "Fecha: ${hoy.day}/${hoy.month}/${hoy.year}"
    }
    private fun toolbar(){
        val barraHerramientas = binding.includeFichaje.toolbarComun
        setSupportActionBar(barraHerramientas)

        // Cambiar el título del Toolbar
        supportActionBar?.title = "CONTROL HORARIO"
        menu = ActionBarDrawerToggle(
            this,
            binding.menuFichaje, barraHerramientas,
            R.string.abrir_menu,
            R.string.cerrar_menu
        )

        binding.menuFichaje.addDrawerListener(menu)
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
            binding.menuFichaje.closeDrawer(GravityCompat.START)
            true
        }
    }


}