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
import com.example.neofichaje.databinding.ActivityDocumentosEmpleadosBinding

class documentosEmpleados : AppCompatActivity() {
    private lateinit var binding: ActivityDocumentosEmpleadosBinding
    private lateinit var menu: ActionBarDrawerToggle
    private lateinit var tipoDoc:ArrayList<CharSequence>//---------
    private lateinit var selectEmple:ArrayList<CharSequence>
    private lateinit var adapterDoc:ArrayAdapter<CharSequence>//-----------
    private lateinit var adapterSelect: ArrayAdapter<CharSequence>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityDocumentosEmpleadosBinding.inflate(layoutInflater)
        setContentView(binding.root)
        toolbar()
        configurarMenuLateral()
        manejarOpcionesMenu()
        instancias()
        intanciasSelect()

    }

    private fun intanciasSelect() {
        selectEmple= arrayListOf("Karina Sol Vega","Empleado 2")
        adapterSelect=ArrayAdapter(applicationContext,android.R.layout.simple_spinner_item,selectEmple)
        adapterSelect.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerLista.adapter=adapterSelect
    }

    private fun instancias() {
        tipoDoc= arrayListOf("Nómina","Contrato")
        //para crear el adaptador
        //necesitamos: CONTEXTO->getapplicationcontext,VISTA,LISTA DE DATOS QUE QUIERO MOSTRAR
        adapterDoc=ArrayAdapter(applicationContext,android.R.layout.simple_spinner_item,tipoDoc)
        adapterDoc.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        //juntamos los datos con lo grafico
        binding.spinnerDoc.adapter=adapterDoc
    }

    private fun toolbar(){
        val barraHerramientas = binding.includeDocEmpleado.toolbarComun
        setSupportActionBar(barraHerramientas)

        // Cambiar el título del Toolbar
        supportActionBar?.title = "ADJUNTAR ARCHIVOS"
        menu = ActionBarDrawerToggle(
            this,
            binding.menuDocEmpleados,  // Este es tu MENU ID
            barraHerramientas,  // Tu toolbar
            R.string.abrir_menu,
            R.string.cerrar_menu)
        binding.menuDocEmpleados.addDrawerListener(menu)
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

            binding.menuDocEmpleados.closeDrawer(GravityCompat.START)
            true
        }
    }
}