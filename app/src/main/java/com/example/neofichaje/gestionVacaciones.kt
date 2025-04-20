package com.example.neofichaje

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.neofichaje.databinding.ActivityGestionVacacionesBinding

class gestionVacaciones : AppCompatActivity() {
    private lateinit var binding: ActivityGestionVacacionesBinding
    private lateinit var menu: ActionBarDrawerToggle
    private lateinit var listaEmpleados:ArrayList<CharSequence>
    private lateinit var opciones:ArrayList<CharSequence>
    private lateinit var adapterLista:ArrayAdapter<CharSequence>
    private lateinit var adapterOpciones:ArrayAdapter<CharSequence>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGestionVacacionesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        toolbar()
        configurarMenuLateral()
        manejarOpcionesMenu()
        instancias()
        instanciasOpciones()
    }

    private fun instanciasOpciones() {
        opciones= arrayListOf("Pendiente","Aceptar","Rechazar")
        adapterOpciones=ArrayAdapter(applicationContext,android.R.layout.simple_spinner_item,opciones)
        adapterOpciones.setDropDownViewResource(android.R.layout.simple_spinner_item)
        binding.spinnerOpciones.adapter=adapterOpciones
    }

    private fun instancias() {
        listaEmpleados=arrayListOf("Karina Sol Vega","Empleado 2")
        adapterLista=ArrayAdapter(applicationContext,android.R.layout.simple_spinner_item,listaEmpleados)
        adapterLista.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerEmpleados.adapter=adapterLista
    }

    private fun toolbar() {
        val barraHerramientas = binding.includeGestionVacaciones.toolbarComun
        setSupportActionBar(barraHerramientas)
        // Cambiar el título del Toolbar
        supportActionBar?.title = "GESTIÓN VACACIONES"
        menu = ActionBarDrawerToggle(
            this,
            binding.menuGestionVacaciones,
            barraHerramientas,
            R.string.abrir_menu,
            R.string.cerrar_menu)
        binding.menuGestionVacaciones.addDrawerListener(menu)
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
                    finishAffinity()
                }
            }

            binding.menuGestionVacaciones.closeDrawer(GravityCompat.START)
            true
        }
    }
}