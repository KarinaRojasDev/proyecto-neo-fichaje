package com.example.neofichaje

import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat

import android.annotation.SuppressLint
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
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.GravityCompat
import com.example.neofichaje.databinding.ActivityPermisoEmpleadoBinding
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import com.prolificinteractive.materialcalendarview.spans.DotSpan
import java.io.File

class permisoEmpleado : AppCompatActivity(),OnClickListener,OnItemSelectedListener {
    private lateinit var binding: ActivityPermisoEmpleadoBinding
    private lateinit var menu: ActionBarDrawerToggle
    private lateinit var tipoPermiso:ArrayList<CharSequence>
    private lateinit var motivo:ArrayList<CharSequence>
    private lateinit var adapterTipo:ArrayAdapter<CharSequence>
    private lateinit var adapterMotivo:ArrayAdapter<CharSequence>
    private lateinit var imagen: Uri
    private lateinit var tomarFotoLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityPermisoEmpleadoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializar el launcher
        configurarLauncherCamara()

        // Configurar la toolbar y el menú lateral
        toolbar()
        manejarOpcionesMenu()

        // Instanciar los valores de tipo y motivo
        instanciasTipo()
        instanciasMotivo()

        // Configurar los calendarios
        configurarCalendarios()

        // Establecer los listeners
        binding.tvFechaInicio.setOnClickListener(this)
        binding.tvFechaFin.setOnClickListener(this)
        binding.btnTomarFoto.setOnClickListener(this)
        binding.spinnerMotivo.onItemSelectedListener=this

    }
    override fun onClick(v: View?) {
        when (v?.id) {
            binding.tvFechaInicio.id -> {
                mostrarCalendario(binding.calendarioFechaInicio)
            }
            binding.tvFechaFin.id -> {
                mostrarCalendario(binding.calendarioFechaFin)
            }
            binding.btnTomarFoto.id->{
                if (tienePermisoCamara()) {
                    abrirCamara()
                } else {
                    pedirPermisoCamara()
                }
            }
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
                    binding.idComentarioVaca.text.clear() // Limpiar texto si no es "Otros"
                }
            }
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        // No hacemos nada por ahora
    }
    private fun instanciasMotivo() {
        motivo= arrayListOf("Baja por enfermedad",
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
        menu = ActionBarDrawerToggle(
            this,
            binding.menuPermiso, barraHerramientas,
            R.string.abrir_menu,
            R.string.cerrar_menu
        )

        binding.menuPermiso.addDrawerListener(menu)
        menu.syncState()
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

    private fun tienePermisoCamara(): Boolean {
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
    override fun onRequestPermissionsResult(solicitud: Int, permisos: Array<out String>, resultados: IntArray) {
        super.onRequestPermissionsResult(solicitud, permisos, resultados)
        if (solicitud == 100) {
            if (resultados.isNotEmpty() && resultados[0] == PackageManager.PERMISSION_GRANTED) {
                abrirCamara()  // Si se otorgan los permisos, abrir la cámara
            } else {
                Toast.makeText(this, "Se necesitan permisos para acceder a la cámara", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun mostrarCalendario(calendario: MaterialCalendarView) {
        calendario.visibility = if (calendario.visibility == View.GONE) View.VISIBLE else View.GONE
    }

    @SuppressLint("SetTextI18n")
    private fun configurarCalendarios() {
        val hoy = CalendarDay.today()
        // Marcamos el día actual con un punto visual (Decorator)
        binding.calendarioFechaInicio.addDecorator(object : DayViewDecorator {
            override fun shouldDecorate(dia: CalendarDay): Boolean {
                return dia == hoy
            }
            override fun decorate(vistaDelDia: DayViewFacade) {
                vistaDelDia.addSpan(DotSpan(14f, ContextCompat.getColor(this@permisoEmpleado, R.color.rosa)))
            }
        })

        binding.calendarioFechaFin.addDecorator(object : DayViewDecorator {
            override fun shouldDecorate(dia: CalendarDay): Boolean {
                return dia == hoy
            }

            override fun decorate(vistaDelDia: DayViewFacade) {
                vistaDelDia.addSpan(DotSpan(14f, ContextCompat.getColor(this@permisoEmpleado, R.color.rosa)))
            }
        })
        binding.calendarioFechaInicio.setOnDateChangedListener { _, date, _ ->
            binding.tvFechaInicio.text = "Fecha: ${date.day}/${date.month}/${date.year}"
            binding.calendarioFechaInicio.visibility = View.GONE
        }

        binding.calendarioFechaFin.setOnDateChangedListener { _, date, _ ->
            binding.tvFechaFin.text = "Fecha: ${date.day}/${date.month}/${date.year}"
            binding.calendarioFechaFin.visibility = View.GONE
        }
    }
}