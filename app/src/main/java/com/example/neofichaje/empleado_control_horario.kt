package com.example.neofichaje

import android.Manifest
import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.View
import android.widget.Toast
import android.view.View.OnClickListener
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import com.example.neofichaje.databinding.ActivityEmpleadoControlHorarioBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
//import android.widget.TimePicker
//import androidx.core.view.GravityCompat

@Suppress("LABEL_NAME_CLASH")
class empleado_control_horario : AppCompatActivity(),OnClickListener {
    private lateinit var binding: ActivityEmpleadoControlHorarioBinding
    private lateinit var menu: ActionBarDrawerToggle
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEmpleadoControlHorarioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        toolbar()
        manejarOpcionesMenu()
        mostrarFechaActual()

        binding.tvInicioFecha.setOnClickListener(this)
        binding.tvFinFecha.setOnClickListener(this)
        binding.tvInicioHora.setOnClickListener(this)
        binding.tvFinHora.setOnClickListener(this)
        binding.btnFichajeEntrada.setOnClickListener(this)
        binding.btnFichajeFin.setOnClickListener(this)

        resetearCamposEntrada()

    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.tvInicioFecha.id -> {
                abrirCalendario(true)
            }

            binding.tvFinFecha.id -> {
                abrirCalendario(false)
            }


            binding.tvInicioHora.id -> {
                mostrarTimePicker("entrada")
            }

            binding.tvFinHora.id -> {
                mostrarTimePicker("salida")
            }

            binding.btnFichajeEntrada.id -> {
                checkLocationPermissionAndSave("entrada")
            }

            binding.btnFichajeFin.id -> {
                checkLocationPermissionAndSave("salida")
            }
        }
    }
    @SuppressLint("SetTextI18n")
    private fun mostrarHoraActual() {
        val formatoHora = SimpleDateFormat("HH:mm", Locale.getDefault())
        val horaActual = formatoHora.format(Calendar.getInstance().time)
        binding.tvInicioHora.text = "Hora: $horaActual"
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
                binding.tvInicioFecha.text = "Fecha: $fechaSeleccionada"
            } else {
                binding.tvFinFecha.text = "Fecha: $fechaSeleccionada"
            }
        }, año, mes, dia)

        datePicker.show()
    }


    @SuppressLint("SetTextI18n")
    private fun mostrarTimePicker(tipo: String) {
        val calendar = Calendar.getInstance()
        val horaActual = calendar.get(Calendar.HOUR_OF_DAY)
        val minutoActual = calendar.get(Calendar.MINUTE)

        val timePickerDialog = android.app.TimePickerDialog(
            this,
            { _, hourOfDay, minute ->
                val horaFormateada = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute)
                if (tipo == "entrada") {
                    binding.tvInicioHora.text = "Hora: $horaFormateada"
                } else {
                    binding.tvFinHora.text = "Hora: $horaFormateada"
                }
            },
            horaActual,
            minutoActual,
            true
        )
        timePickerDialog.show()
    }


    @SuppressLint("SetTextI18n")
    private fun mostrarFechaActual() {

        binding.tvInicioFecha.text = "Seleccione fecha inicio"
        binding.tvFinFecha.text = "Seleccione fecha final"
    }
    private fun checkLocationPermissionAndSave(tipo: String) {
        if (!validarDatos(tipo)) return
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            guardarFichaje(tipo)
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
        }
    }
    private fun validarDatos(tipo: String): Boolean {
        val fecha = if (tipo == "entrada") binding.tvInicioFecha.text.toString() else binding.tvFinFecha.text.toString()
        val hora = if (tipo == "entrada") binding.tvInicioHora.text.toString() else binding.tvFinHora.text.toString()

        if (fecha.isBlank() || hora.contains("Seleccione")) {
            Toast.makeText(this, "Selecciona primero la fecha y hora antes de fichar", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }
    @SuppressLint("MissingPermission")
    private fun guardarFichaje(tipo: String) {
        val uidEmpleado = auth.currentUser?.uid ?: return

        binding.btnFichajeEntrada.isEnabled = false
        binding.btnFichajeFin.isEnabled = false

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            db.collection("usuarios").document(uidEmpleado).get().addOnSuccessListener { userDoc ->
                val empresaId = userDoc.getString("empresa_id") ?: ""
                val nombre = userDoc.getString("nombre") ?: ""
                val apellidos = userDoc.getString("apellidos") ?: ""

                if (empresaId.isEmpty()) {
                    Toast.makeText(this, "Error: El empleado no tiene asignada una empresa", Toast.LENGTH_LONG).show()
                    binding.btnFichajeEntrada.isEnabled = true
                    binding.btnFichajeFin.isEnabled = true
                    return@addOnSuccessListener
                }

                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val fechaActual = sdf.format(System.currentTimeMillis())
                val nombreDocUsuarios = "controlHorario_$fechaActual"
                val nombreLimpio = nombre.replace(" ", "_")
                val apellidosLimpio = apellidos.replace(" ", "_")
                val nombreDocEmpresas = "controlHorario_${fechaActual}_${nombreLimpio}_${apellidosLimpio}"

                val docRefUsuarios = db.collection("usuarios").document(uidEmpleado)
                    .collection("controlHorario").document(nombreDocUsuarios)

                val docRefEmpresas = db.collection("empresas").document(empresaId)
                    .collection("gestionControlHorario").document(nombreDocEmpresas)

                docRefUsuarios.get().addOnSuccessListener { document ->
                    if (document.exists()) {
                        val horaSalida = document.getString("horaSalida")
                        if (tipo == "entrada" && horaSalida.isNullOrEmpty()) {
                            Toast.makeText(this, "Ya has fichado entrada hoy. Debes fichar salida primero.", Toast.LENGTH_LONG).show()
                        } else if (tipo == "salida") {
                            val datosSalida = mapOf(
                                "horaSalida" to binding.tvFinHora.text.toString().replace("Hora: ", ""),
                                "latitudSalida" to (location?.latitude ?: 0.0),
                                "longitudSalida" to (location?.longitude ?: 0.0),
                                "timestampSalida" to FieldValue.serverTimestamp(),
                                "estado" to "cerrado"
                            )
                            docRefUsuarios.update(datosSalida)
                            docRefEmpresas.update(datosSalida)

                            Toast.makeText(this, "Fichaje de salida registrado", Toast.LENGTH_SHORT).show()
                            resetearCamposEntrada()
                        } else if (tipo == "entrada" && !horaSalida.isNullOrEmpty()) {
                            val fechaSeleccionada = binding.tvInicioFecha.text.toString().replace("Fecha: ", "").trim()
                            val fechaISO = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(
                                SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(fechaSeleccionada)!!
                            )

                            val datosEntrada = hashMapOf(
                                "fecha" to fechaISO,
                                "uidEmpleado" to uidEmpleado,
                                "nombreEmpleado" to nombre,
                                "apellidosEmpleado" to apellidos,
                                "horaEntrada" to binding.tvInicioHora.text.toString().replace("Hora: ", ""),
                                "latitudEntrada" to (location?.latitude ?: 0.0),
                                "longitudEntrada" to (location?.longitude ?: 0.0),
                                "timestampEntrada" to FieldValue.serverTimestamp(),
                                "estado" to "abierto"
                            )
                            docRefUsuarios.set(datosEntrada)
                            docRefEmpresas.set(datosEntrada)

                            Toast.makeText(this, "Nuevo fichaje de entrada registrado", Toast.LENGTH_SHORT).show()
                            resetearCamposEntrada()
                        }
                    } else {
                        if (tipo == "entrada") {
                            val fechaSeleccionada = binding.tvInicioFecha.text.toString().replace("Fecha: ", "").trim()
                            val fechaISO = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(
                                SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(fechaSeleccionada)!!
                            )

                            val datosEntrada = hashMapOf(
                                "fecha" to fechaISO,
                                "uidEmpleado" to uidEmpleado,
                                "nombreEmpleado" to nombre,
                                "apellidosEmpleado" to apellidos,
                                "horaEntrada" to binding.tvInicioHora.text.toString().replace("Hora: ", ""),
                                "latitudEntrada" to (location?.latitude ?: 0.0),
                                "longitudEntrada" to (location?.longitude ?: 0.0),
                                "timestampEntrada" to FieldValue.serverTimestamp(),
                                "estado" to "abierto"
                            )
                            docRefUsuarios.set(datosEntrada)
                            docRefEmpresas.set(datosEntrada)

                            Toast.makeText(this, "Fichaje de entrada registrado", Toast.LENGTH_SHORT).show()
                            resetearCamposEntrada()
                        } else {
                            Toast.makeText(this, "Primero debes fichar entrada.", Toast.LENGTH_LONG).show()
                        }
                    }

                    binding.btnFichajeEntrada.isEnabled = true
                    binding.btnFichajeFin.isEnabled = true
                }
            }
        }
    }


    @SuppressLint("SetTextI18n")
    private fun cargarHoraFichajeEntrada() {
        val uidEmpleado = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val nombreDoc = "controlHorario_${sdf.format(System.currentTimeMillis())}"

        FirebaseFirestore.getInstance()
            .collection("usuarios").document(uidEmpleado)
            .collection("controlHorario").document(nombreDoc)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists() && document.getString("estado") == "abierto") {
                    val horaEntrada = document.getString("horaEntrada") ?: ""
                    binding.tvInicioHora.text = "Hora: $horaEntrada"
                } else {
                    resetearCamposEntrada()
                }
            }
    }

    override fun onResume() {
        super.onResume()
        cargarHoraFichajeEntrada()
        mostrarHoraActual()
    }
    @SuppressLint("SetTextI18n")
    private fun resetearCamposEntrada() {

        binding.tvInicioFecha.text = "Seleccione fecha inicio"
        binding.tvFinFecha.text = "Seleccione fecha final"
        binding.tvInicioHora.text = "Seleccione hora de inicio "
        binding.tvFinHora.text = "Seleccione hora final"

    }


    private fun toolbar() {
        val barraHerramientas = binding.includeFichaje.toolbarComun
        setSupportActionBar(barraHerramientas)
        supportActionBar?.title = "CONTROL HORARIO"
        menu = ActionBarDrawerToggle(
            this,
            binding.menuFichaje,
            barraHerramientas,
            R.string.abrir_menu,
            R.string.cerrar_menu
        )
        binding.menuFichaje.addDrawerListener(menu)
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
            binding.menuFichaje.closeDrawer(GravityCompat.START)
            true
        }
    }
}
