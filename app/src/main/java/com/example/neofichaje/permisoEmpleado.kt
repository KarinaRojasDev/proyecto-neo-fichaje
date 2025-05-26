package com.example.neofichaje

import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.view.View.OnClickListener
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.GravityCompat
import com.example.neofichaje.databinding.ActivityPermisoEmpleadoBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.File
import java.util.Calendar
import java.util.Locale

class permisoEmpleado : AppCompatActivity(),OnClickListener,OnItemSelectedListener {
    private lateinit var binding: ActivityPermisoEmpleadoBinding
    private lateinit var menu: ActionBarDrawerToggle
    private lateinit var tipoPermiso:ArrayList<CharSequence>
    private lateinit var motivo:ArrayList<CharSequence>
    private lateinit var adapterTipo:ArrayAdapter<CharSequence>
    private lateinit var adapterMotivo:ArrayAdapter<CharSequence>
    private lateinit var imagen: Uri
    private lateinit var tomarFotoLauncher: ActivityResultLauncher<Intent>
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityPermisoEmpleadoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()

        configurarLauncherCamara()
        toolbar()
        manejarOpcionesMenu()
        instanciasTipo()
        instanciasMotivo()

        // Establecer los listeners
        binding.tvFechaInicio.setOnClickListener(this)
        binding.tvFechaFin.setOnClickListener(this)
        binding.btnTomarFoto.setOnClickListener(this)
        binding.btnEnviarSolicitud.setOnClickListener(this)
        binding.spinnerMotivo.onItemSelectedListener=this

    }
    override fun onClick(v: View?) {
        when (v?.id) {
            binding.tvFechaInicio.id -> {
                abrirCalendario(true)
            }
            binding.tvFechaFin.id -> {
                abrirCalendario(false)
            }
            binding.btnTomarFoto.id->{
                if (verificarPermisoCamara()) {
                    abrirCamara()
                } else {
                    pedirPermisoCamara()
                }
            }
            binding.btnEnviarSolicitud.id ->{
                enviarSolicitudPermiso()
            }
        }
    }
    @SuppressLint("SetTextI18n")
    private fun abrirCalendario(seleccionInicio: Boolean) {
        val calendario = Calendar.getInstance()
        val año = calendario.get(Calendar.YEAR)
        val mes = calendario.get(Calendar.MONTH)
        val dia = calendario.get(Calendar.DAY_OF_MONTH)

        val datePicker = DatePickerDialog(this, { _, year, month, dayOfMonth ->
            val fechaSeleccionada = "${dayOfMonth.toString().padStart(2, '0')}/${(month + 1).toString().padStart(2, '0')}/$year"
            if (seleccionInicio) {
                binding.tvFechaInicio.text = "Fecha: $fechaSeleccionada"
            } else {
                binding.tvFechaFin.text = "Fecha: $fechaSeleccionada"
            }
        }, año, mes, dia)

        datePicker.show()
    }

    private fun enviarSolicitudPermiso() {
        val tipo = binding.spinnerTipo.selectedItem.toString()
        val motivo = binding.spinnerMotivo.selectedItem.toString()
        val comentario = binding.idComentarioVaca.text.toString()
        val fechaInicio = binding.tvFechaInicio.text.toString().replace("Fecha: ", "").trim()
        val fechaFin = binding.tvFechaFin.text.toString().replace("Fecha: ", "").trim()

        if (fechaInicio.isBlank() || fechaFin.isBlank() || tipo.isBlank() || motivo.isBlank() || (motivo == "Otros" && comentario.isBlank())) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_LONG).show()
            return
        }

        val uidEmpleado = FirebaseAuth.getInstance().currentUser?.uid ?: return
        db.collection("usuarios").document(uidEmpleado).get().addOnSuccessListener { doc ->
            val nombre = doc.getString("nombre") ?: "Empleado"
            val apellidos = doc.getString("apellidos") ?: ""
            val empresaId = doc.getString("empresa_id") ?: return@addOnSuccessListener
            val nombreEmpleado = "$nombre $apellidos"
            val mesActual = Calendar.getInstance().getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault()) ?: "Mes"
            val datosPermiso = hashMapOf(
                "tipo" to tipo,
                "motivo" to motivo,
                "comentario" to comentario,
                "fechaInicio" to fechaInicio,
                "fechaFin" to fechaFin,
                "estado" to "pendiente",
                "nombreEmpleado" to nombreEmpleado,
                "estadoLectura" to "pendiente",
                "uidEmpleado" to uidEmpleado
            )

            db.collection("empresas").document(empresaId)
                .collection("gestionPermisos_bajas")
                .document("$nombreEmpleado - ${System.currentTimeMillis()}")
                .set(datosPermiso)

            db.collection("usuarios").document(uidEmpleado)
                .collection("permisos_bajas")
                .document("$mesActual - ${System.currentTimeMillis()}")
                .set(datosPermiso)

            db.collection("usuarios").document(uidEmpleado)
                .update("tvPermisos", "Has solicitado permisos del $fechaInicio al $fechaFin")

            Toast.makeText(this, "Solicitud enviada correctamente", Toast.LENGTH_LONG).show()
            startActivity(Intent(this, inicio_empleado::class.java))
            finish()
        }
    }


    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        when(parent?.id){
            binding.spinnerMotivo.id->{
                val seleccion:String= parent.adapter.getItem(position).toString()
                if (seleccion == "Otros") {
                    binding.idComentarioVaca.visibility = View.VISIBLE
                } else {
                    binding.idComentarioVaca.visibility = View.GONE
                    binding.idComentarioVaca.text.clear()
                }
            }
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {}

    private fun instanciasMotivo() {
        motivo= arrayListOf(
            "Seleccione una opción",
            "Baja por enfermedad",
            "Baja por accidente laboral",
            "Baja por maternidad/paternidad",
            "Permiso por fallecimiento de un familiar",
            "Permiso por matrimonio",
            "Permiso por mudanza",
            "Permiso por deberes cívicos",
            "Permiso por estudios",
            "Otros")
        adapterMotivo=ArrayAdapter(applicationContext,android.R.layout.simple_spinner_item,motivo)
        adapterMotivo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerMotivo.adapter=adapterMotivo
    }

    private fun instanciasTipo() {
        tipoPermiso= arrayListOf("Permiso","Baja")
        adapterTipo=ArrayAdapter(applicationContext,android.R.layout.simple_spinner_item,tipoPermiso)
        adapterTipo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerTipo.adapter=adapterTipo
    }

    private fun toolbar() {
        val barraHerramientas = binding.includePermisos.toolbarComun
        setSupportActionBar(barraHerramientas)
        // Cambiar el título del Toolbar
        supportActionBar?.title = "PERMISOS/BAJAS"
        menu = ActionBarDrawerToggle(this,
            binding.menuPermiso,
            barraHerramientas,
            R.string.abrir_menu,
            R.string.cerrar_menu
        )

        binding.menuPermiso.addDrawerListener(menu)
        menu.syncState()
    }

    private fun verificarPermisoCamara(): Boolean {
        // Verificar si ya tiene permisos para usar la cámara
        return ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    private fun pedirPermisoCamara() {
        // Solicitar permisos en tiempo de ejecución si no están concedidos
        ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.CAMERA), 100)
    }

    private fun abrirCamara() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        // Crear un archivo temporal para guardar la foto
        val archivo = File.createTempFile("foto_", ".jpg", cacheDir)
        imagen = FileProvider.getUriForFile(this, "$packageName.provider", archivo)
        // Enviar el URI del archivo donde se guardará la imagen
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imagen)
        // Lanzar la cámara
        tomarFotoLauncher.launch(intent)
    }

    private fun configurarLauncherCamara() {
        // Registrar el launcher para tomar una foto
        tomarFotoLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                // Si la foto fue tomada, mostrarla en un ImageView (o hacer lo que necesites con la foto)
                binding.imgPreview.setImageURI(imagen)
                binding.imgPreview.visibility = View.VISIBLE
            }
        }
    }
    // Manejar la respuesta de los permisos
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                abrirCamara()  // Si se otorgan los permisos, abrir la cámara
            } else {
                Toast.makeText(this, "Se necesitan permisos para acceder a la cámara", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun manejarOpcionesMenu() {

        binding.navView.setNavigationItemSelectedListener { opcion ->
            when (opcion.itemId) {
                R.id.inicioEmpleado->{
                    val intent= Intent(this, inicio_empleado::class.java)
                    startActivity(intent)
                }
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

            binding.menuPermiso.closeDrawer(GravityCompat.START)
            true
        }
    }
}