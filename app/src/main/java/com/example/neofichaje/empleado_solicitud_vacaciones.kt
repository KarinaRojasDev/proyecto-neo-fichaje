package com.example.neofichaje

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.example.neofichaje.databinding.ActivityEmpleadoSolicitudVacacionesBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class empleado_solicitud_vacaciones : AppCompatActivity(),OnClickListener{

    private lateinit var binding: ActivityEmpleadoSolicitudVacacionesBinding
    private lateinit var menu: ActionBarDrawerToggle
    private var fechaInicio: String? = null
    private var fechaFin: String? = null
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityEmpleadoSolicitudVacacionesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        cargarDiasDisponibles()
        toolbar()
        manejarOpcionesMenu()
        binding.textofechaInicioVaca.setOnClickListener(this)
        binding.textofechaFinVaca.setOnClickListener(this)
        binding.btnEnviarSolicitud.setOnClickListener(this)

    }
    override fun onClick(v: View?) {
        when (v?.id) {
            binding.textofechaInicioVaca.id -> {
                abrirCalendario(true)
            }
            binding.textofechaFinVaca.id -> {
                if (fechaInicio != null) {
                    abrirCalendario(false)
                } else {
                    Toast.makeText(this, "Primero selecciona la fecha de inicio", Toast.LENGTH_SHORT).show()
                }
            }
            binding.btnEnviarSolicitud.id -> {
                enviarSolicitud()
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
                fechaInicio = fechaSeleccionada
                binding.textofechaInicioVaca.text = "Fecha inicio: $fechaInicio"
            } else {
                fechaFin = fechaSeleccionada
                binding.textofechaFinVaca.text = "Fecha fin: $fechaFin"
            }
        }, año, mes, dia)

        datePicker.show()
    }
    @SuppressLint("SetTextI18n")
    private fun cargarDiasDisponibles() {
        val uidEmpleado = auth.currentUser?.uid ?: return
        db.collection("usuarios").document(uidEmpleado).get()
            .addOnSuccessListener { documento ->
                val anuales = documento.getLong("totalVacacionesAnuales") ?: 0L
                val disfrutados = documento.getLong("totalVacacionesDisfrutadas") ?: 0L
                val disponibles = anuales - disfrutados
                binding.tvConteoVacacionesEmpleado.text = "Usted dispone de: $disponibles días de vacaciones"
            }
            .addOnFailureListener {
                binding.tvConteoVacacionesEmpleado.text = "No se pudieron cargar los días"
            }
    }



    private fun enviarSolicitud() {
        val comentario = binding.idComentarioVaca.text.toString()
        val uidEmpleado = FirebaseAuth.getInstance().currentUser?.uid ?: return

        if (fechaInicio.isNullOrEmpty() || fechaFin.isNullOrEmpty()) {
            Toast.makeText(this, "Selecciona las fechas de inicio y fin", Toast.LENGTH_SHORT).show()
            return
        }

        val db = FirebaseFirestore.getInstance()

        db.collection("usuarios").document(uidEmpleado).get().addOnSuccessListener { doc ->
            val nombre = doc.getString("nombre") ?: "Empleado"
            val apellidos = doc.getString("apellidos") ?: ""
            val empresaId = doc.getString("empresa_id") ?: return@addOnSuccessListener
            val calendar = Calendar.getInstance()
            val anoActual = calendar.get(Calendar.YEAR).toString()
            val mesActual = (calendar.get(Calendar.MONTH) + 1).toString().padStart(2, '0')
            val nombreDoc = "$anoActual $mesActual $nombre $apellidos"

            val datosVacaciones = hashMapOf(
                "fechaInicio" to fechaInicio,
                "fechaFin" to fechaFin,
                "comentario" to comentario,
                "estado" to "pendiente",
                "uidEmpleado" to uidEmpleado
            )

            // Guardar en usuarios
            db.collection("usuarios").document(uidEmpleado)
                .collection("vacaciones")
                .document(nombreDoc)
                .set(datosVacaciones)

            // Guardar en empresas
            db.collection("empresas").document(empresaId)
                .collection("gestionVacaciones")
                .document(nombreDoc)
                .set(datosVacaciones)

            // Notificación
            val mensajeNotificacion = "Ha solicitado vacaciones del $fechaInicio al $fechaFin"
            db.collection("usuarios").document(uidEmpleado)
                .update("tvVacacionesEmpleado", mensajeNotificacion)

            db.collection("empresas").document(empresaId)
                .update("tvVacacionesEmpresa", "$nombre $apellidos ha solicitado vacaciones del $fechaInicio al $fechaFin (Pendiente)")

            // Limpiar y redirigir
            binding.textofechaInicioVaca.text = getString(R.string.SeleccioneFechaInicio)
            binding.textofechaFinVaca.text = getString(R.string.SeleccioneFechaFin)
            binding.idComentarioVaca.text?.clear()
            fechaInicio = null
            fechaFin = null

            Toast.makeText(this, "Solicitud enviada correctamente", Toast.LENGTH_LONG).show()
            startActivity(Intent(this, inicio_empleado::class.java))
            finish()
        }
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
            binding.menuSolicitudVaca.closeDrawer(GravityCompat.START)
            true
        }
    }
}

