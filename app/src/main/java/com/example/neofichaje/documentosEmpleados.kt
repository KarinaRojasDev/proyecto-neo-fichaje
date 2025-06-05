package com.example.neofichaje


import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager

import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import com.example.neofichaje.databinding.ActivityDocumentosEmpleadosBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage


class documentosEmpleados : AppCompatActivity(),OnClickListener {
    private lateinit var binding: ActivityDocumentosEmpleadosBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var empleadosMap: Map<String, String> = emptyMap()
    private lateinit var menu: ActionBarDrawerToggle
    private lateinit var listaTipoDoc:ArrayList<CharSequence>
    private lateinit var lanzadorArchivo: ActivityResultLauncher<Intent>
    private var archivoUriSeleccionado: Uri? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityDocumentosEmpleadosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        toolbar()
        manejarOpcionesMenu()
        inicializarLanzadorDeArchivo()
        acciones()
        cargarEmpleadosEnSpinner()
        binding.btnSubirArchivo.setOnClickListener(this)
        binding.btnGuardarDocumento.setOnClickListener(this)




    }
    override fun onClick(v: View?) {
        when(v?.id){
            binding.btnSubirArchivo.id->{
                abrirSelectorDeArchivosPDF()
            }
            binding.btnGuardarDocumento.id->{
                val uri = archivoUriSeleccionado
                if (uri != null) {
                    guardarDocumento(uri)
                } else {
                    Toast.makeText(this, "Selecciona un archivo PDF primero", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    @SuppressLint("SetTextI18n")
    private fun inicializarLanzadorDeArchivo() {
        lanzadorArchivo = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { resultado ->
            if (resultado.resultCode == RESULT_OK) {
                val uri = resultado.data?.data
                uri?.let {
                    archivoUriSeleccionado = it
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

    private fun cargarEmpleadosEnSpinner() {
        val empleados = mutableListOf("Selecciona un empleado")
        val listaEmpleados = mutableMapOf<String, String>()


        db.collection("usuarios")
            .whereEqualTo("puesto", "Tecnico")
            .get()
            .addOnSuccessListener { documentos ->
                for (doc in documentos) {
                    val nombre = doc.getString("nombre") ?: ""
                    val apellidos = doc.getString("apellidos") ?: ""
                    val nombreCompleto = "$nombre $apellidos"
                    empleados.add(nombreCompleto)
                    listaEmpleados[nombreCompleto] = doc.id
                }
                empleadosMap = listaEmpleados.toMap()

                val adaptador = object : ArrayAdapter<String>(
                    this,
                    android.R.layout.simple_spinner_item,
                    empleados
                ) {
                    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                        val view = super.getView(position, convertView, parent)
                        (view as TextView).setTextColor(ContextCompat.getColor(context, android.R.color.black))
                        return view
                    }

                    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                        val view = super.getDropDownView(position, convertView, parent)
                        (view as TextView).setTextColor(ContextCompat.getColor(context, android.R.color.black))
                        return view
                    }
                }

                adaptador.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.spinnerLista.adapter = adaptador

            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al cargar empleados", Toast.LENGTH_SHORT).show()
            }
    }
    private fun acciones() {
        listaTipoDoc = arrayListOf("Nominas", "Contrato")

        val adaptadorDoc = object : ArrayAdapter<CharSequence>(
            this,
            android.R.layout.simple_spinner_item,
            listaTipoDoc
        ) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                (view as TextView).setTextColor(ContextCompat.getColor(context, android.R.color.black))
                return view
            }

            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getDropDownView(position, convertView, parent)
                (view as TextView).setTextColor(ContextCompat.getColor(context, android.R.color.black))
                return view
            }
        }

        adaptadorDoc.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerDoc.adapter = adaptadorDoc
    }


    private fun guardarDocumento(uri: Uri) {
        val tipoDoc = binding.spinnerDoc.selectedItem.toString().lowercase()
        val nombreEmpleado = binding.spinnerLista.selectedItem.toString()
        var tituloDocumento = binding.etTituloDocumento.text.toString().trim()


        if (tipoDoc == "nominas" && tituloDocumento.isEmpty()) {
            Toast.makeText(this, "Debes introducir un título para la nómina", Toast.LENGTH_SHORT).show()
            return
        }


        if (tipoDoc == "nominas") {
            tituloDocumento = tituloDocumento.replace(Regex("\\bnomina de\\b", RegexOption.IGNORE_CASE), "Nómina de")
        }

        if (tipoDoc.isBlank() || nombreEmpleado == "Selecciona un empleado") {
            Toast.makeText(this, "Selecciona tipo de documento y empleado", Toast.LENGTH_SHORT).show()
            return
        }

        val uidEmpleado = empleadosMap[nombreEmpleado] ?: return
        val uidAdmin = auth.currentUser?.uid ?: return

        val usuarioRef = db.collection("usuarios").document(uidAdmin)

        usuarioRef.get().addOnSuccessListener { adminDoc ->
            if (!adminDoc.exists()) {
                Toast.makeText(this, "Administrador no encontrado", Toast.LENGTH_SHORT).show()
                return@addOnSuccessListener
            }


            val puesto = adminDoc.getString("puesto") ?: ""
            val empresaId = adminDoc.getString("empresa_id") ?: ""

            db.collection("usuarios").document(uidEmpleado).get()
                .addOnSuccessListener { empleadoDoc ->
                    val nombre = empleadoDoc.getString("nombre") ?: ""
                    val apellidos = empleadoDoc.getString("apellidos") ?: ""

                    val nombreArchivo = "${tipoDoc}_${System.currentTimeMillis()}.pdf"
                    val storageRef = FirebaseStorage.getInstance().reference
                        .child("documentos/$uidEmpleado/$tipoDoc/$nombreArchivo")

                    storageRef.putFile(uri).addOnSuccessListener {
                        storageRef.downloadUrl.addOnSuccessListener { url ->
                            val datos = hashMapOf(
                                "nombreArchivo" to nombreArchivo,
                                "url" to url.toString(),
                                "fecha" to FieldValue.serverTimestamp(),
                                "nombreEmpleado" to nombre,
                                "apellidosEmpleado" to apellidos,
                                "uidEmpleado" to uidEmpleado,
                                "tituloDocumento" to tituloDocumento
                            )
                            //guardamos en la bbdd  los dic. del empleado
                            db.collection("usuarios")
                                .document(uidEmpleado)
                                .collection(tipoDoc)
                                .add(datos) .addOnSuccessListener {

                                    Toast.makeText(this, "Documento y notificación enviados", Toast.LENGTH_LONG).show()
                                    limpiarCampos()

                                    val notificacion = when (tipoDoc) {
                                        "nominas" -> "Tienes la  ${tituloDocumento.ifEmpty {"este mes" }} disponible"

                                        "contrato" -> "Tienes tu contrato disponible"
                                        else -> null
                                    }
                                    notificacion?.let { mensaje ->
                                        val campo =
                                            if (tipoDoc == "nominas") "tvNominas" else "tvContrato"
                                        db.collection("usuarios")
                                            .document(uidEmpleado)
                                            .update(campo, mensaje)
                                            .addOnFailureListener { e ->
                                                Toast.makeText(
                                                    this,
                                                    "Error al enviar notificación: ${e.message}",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                    }
                                }
                                .addOnFailureListener {
                                    Toast.makeText(
                                        this,
                                        "Error guardando en usuario: ${it.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }

                            //guardamos los documentos en la empresa de cada empleado
                            if (puesto == "Administrador" && empresaId.isNotBlank()) {
                                val rutaEmpresa = when (tipoDoc) {
                                    "nominas" -> "gestionNominas"
                                    "contrato" -> "gestionContratos"
                                    else -> "otrosDocumentos"
                                }

                                db.collection("empresas")
                                    .document(empresaId)
                                    .collection(rutaEmpresa)
                                    .add(datos)
                            }
                        }
                    } .addOnFailureListener {
                        Toast.makeText(this, "Error al subir archivo: ${it.message}", Toast.LENGTH_LONG).show()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error cargando empleado: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }
            .addOnFailureListener {
                Toast.makeText(this, "Error cargando administrador: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
    private fun limpiarCampos() {
        binding.etTituloDocumento.setText("")
        binding.etSubirArchivos.setText("")
        archivoUriSeleccionado = null
        binding.spinnerLista.setSelection(0)
        binding.spinnerDoc.setSelection(0)
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
                R.id.inicioEmpresario -> {
                    val intent = Intent(this, inicio_empresario::class.java)
                    startActivity(intent)
                }
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


