package com.example.neofichaje


import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.Manifest
import androidx.core.content.ContextCompat


import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.View
import android.view.View.OnClickListener
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.example.neofichaje.databinding.ActivityDocumentosEmpleadosBinding

class documentosEmpleados : AppCompatActivity(),OnClickListener {
    private lateinit var binding: ActivityDocumentosEmpleadosBinding

    private lateinit var menu: ActionBarDrawerToggle

    private lateinit var listaTipoDoc:ArrayList<CharSequence>
    private lateinit var listaSelectEmple:ArrayList<CharSequence>
    private lateinit var adapterDoc:ArrayAdapter<CharSequence>
    private lateinit var adapterSelect: ArrayAdapter<CharSequence>

    private lateinit var lanzadorArchivo: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityDocumentosEmpleadosBinding.inflate(layoutInflater)
        setContentView(binding.root)


        toolbar()
        manejarOpcionesMenu()
        inicializarSpinners()


        inicializarLanzadorDeArchivo()

        binding.btnSubirArchivo.setOnClickListener(this)

    }
    override fun onClick(v: View?) {
        when(v?.id){
            binding.btnSubirArchivo.id->{
                abrirSelectorDeArchivosPDF()
            }
        }
    }
    @SuppressLint("SetTextI18n")
    private fun inicializarLanzadorDeArchivo() {
        lanzadorArchivo = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { resultado ->
            if (resultado.resultCode == RESULT_OK) {
                val uri = resultado.data?.data
                uri?.let {
                    val nombre = obtenerNombreArchivo(it)
                    binding.etSubirArchivos.setText("Archivo seleccionado: $nombre")
                }
            }
        }
    }
    private fun abrirSelectorDeArchivosPDF() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "application/pdf"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        lanzadorArchivo.launch(Intent.createChooser(intent, "Selecciona un archivo PDF"))
    }

    private fun obtenerNombreArchivo(uri: Uri): String {
        var nombre = "Documento.pdf"
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nombreIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst()) {
                nombre = cursor.getString(nombreIndex)
            }
        }
        return nombre
    }



    private fun inicializarSpinners() {
        listaSelectEmple= arrayListOf("Karina Sol Vega","Empleado 2")
        adapterSelect=ArrayAdapter(applicationContext,android.R.layout.simple_spinner_item,listaSelectEmple)
        adapterSelect.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerLista.adapter=adapterSelect

        listaTipoDoc= arrayListOf("Nómina","Contrato")
        adapterDoc=ArrayAdapter(applicationContext,android.R.layout.simple_spinner_item,listaTipoDoc)
        adapterDoc.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerDoc.adapter=adapterDoc
    }

    private fun toolbar(){
        val barraHerramientas = binding.includeDocEmpleado.toolbarComun
        setSupportActionBar(barraHerramientas)

        // Cambiar el título del Toolbar
        supportActionBar?.title = "ADJUNTAR ARCHIVOS"
        menu = ActionBarDrawerToggle(
            this,
            binding.menuDocEmpleados,
            barraHerramientas,
            R.string.abrir_menu,
            R.string.cerrar_menu)
        binding.menuDocEmpleados.addDrawerListener(menu)
        menu.syncState()

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