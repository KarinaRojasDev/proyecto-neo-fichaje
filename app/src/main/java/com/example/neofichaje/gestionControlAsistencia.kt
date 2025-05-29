package com.example.neofichaje

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.example.neofichaje.databinding.ActivityGestionControlAsistenciaBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

@Suppress("LABEL_NAME_CLASH")
class gestionControlAsistencia : AppCompatActivity() {

    private lateinit var binding: ActivityGestionControlAsistenciaBinding
    private lateinit var menu: ActionBarDrawerToggle
    private var selectedEmpleadoUID: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGestionControlAsistenciaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        configurarCalendario()
        toolbar()

        manejarOpcionesMenu()
        binding.calendarView.addDecorator(TodayDecorator())
        cargarListaEmpleados()


    }
    private fun cargarListaEmpleados() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        db.collection("usuarios").document(uid).get().addOnSuccessListener { empresaDoc ->
            val empresaId = empresaDoc.getString("empresa_id") ?: return@addOnSuccessListener

            db.collection("usuarios")
                .whereEqualTo("empresa_id", empresaId)
                .get()
                .addOnSuccessListener { documentos ->
                    val lista = mutableListOf("Todos los empleados")
                    val mapa = mutableMapOf<String, String>()

                    for (empleadoDoc  in documentos) {

                        val puesto = empleadoDoc.getString("puesto") ?: ""
                        if (puesto.lowercase() != "tecnico") continue


                        val nombre = empleadoDoc .getString("nombre") ?: ""
                        val apellidos = empleadoDoc .getString("apellidos") ?: ""
                        val nombreCompleto = "$nombre $apellidos"
                        lista.add(nombreCompleto)
                        mapa[nombreCompleto] = empleadoDoc .id
                    }

                    val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, lista)
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    binding.spinnerEmpleados.adapter = adapter

                    binding.spinnerEmpleados.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        @SuppressLint("DefaultLocale")
                        override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                            selectedEmpleadoUID = mapa[lista[pos]] // null si es "Todos"
                            binding.calendarView.selectedDate?.let {
                                val fecha = String.format("%04d-%02d-%02d", it.year, it.month + 1, it.day)
                                when {
                                    binding.radioDia.isChecked -> cargarFichajes(fecha)
                                    binding.radioMes.isChecked -> cargarResumenMensual(it.month + 1, it.year)
                                    binding.radioAnual.isChecked -> cargarResumenAnual(it.year)
                                }
                            }
                        }

                        override fun onNothingSelected(parent: AdapterView<*>) {}
                    }
                }
        }
    }


    @SuppressLint("DefaultLocale")
    private fun configurarCalendario() {
        binding.calendarView.setOnDateChangedListener { _, date, _ ->
            val fechaSeleccionada = String.format("%04d-%02d-%02d", date.year, date.month + 1, date.day)

            when {
                binding.radioDia.isChecked -> cargarFichajes(fechaSeleccionada)
                binding.radioMes.isChecked -> cargarResumenMensual(date.month + 1, date.year)
                binding.radioAnual.isChecked -> cargarResumenAnual(date.year)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun cargarFichajes(fecha: String) {
        binding.tvResumenHoras.text = "Cargando información..."
        binding.layoutFichajes.removeAllViews()

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        FirebaseFirestore.getInstance().collection("usuarios").document(uid).get()
            .addOnSuccessListener { userDoc ->
                val empresaId = userDoc.getString("empresa_id") ?: ""

                FirebaseFirestore.getInstance()
                    .collection("empresas").document(empresaId)
                    .collection("gestionControlHorario")
                    .get()
                    .addOnSuccessListener { documentos ->
                        var totalMinutos = 0
                        var empleadosContados = 0

                        for (doc in documentos ) {
                            val fechaDoc = doc.getString("fecha")
                            val uidEmpleadoDoc = doc.getString("uidEmpleado") ?: ""

                            if (fechaDoc == fecha && (selectedEmpleadoUID == null || selectedEmpleadoUID == uidEmpleadoDoc)) {
                                empleadosContados++
                                val nombre = doc.getString("nombreEmpleado") ?: ""
                                val apellidos = doc.getString("apellidosEmpleado") ?: ""
                                val horaEntrada = doc.getString("horaEntrada") ?: "-"
                                val horaSalida = doc.getString("horaSalida") ?: "-"

                                val fichaView = LayoutInflater.from(this)
                                    .inflate(R.layout.item_fichaje, binding.layoutFichajes, false)

                                fichaView.findViewById<TextView>(R.id.tvNombreEmpleado).text = "$nombre $apellidos"
                                fichaView.findViewById<TextView>(R.id.tvEntrada).text = "Entrada: $horaEntrada"
                                fichaView.findViewById<TextView>(R.id.tvSalida).text = "Salida: $horaSalida"

                                binding.layoutFichajes.addView(fichaView)

                                if (horaEntrada != "-" && horaSalida != "-") {
                                    try {
                                        val formato = SimpleDateFormat("HH:mm", Locale.getDefault())
                                        val entradaDate = formato.parse(horaEntrada)
                                        val salidaDate = formato.parse(horaSalida)
                                        val diff = salidaDate.time - entradaDate.time
                                        totalMinutos += (diff / 60000).toInt()
                                    } catch (_: Exception) {}
                                }
                            }
                        }

                        if (empleadosContados == 0) {
                            binding.tvResumenHoras.text = "No hay fichajes registrados para esta fecha"
                        } else {
                            val horas = totalMinutos / 60
                            val minutos = totalMinutos % 60
                            binding.tvResumenHoras.text = "Total trabajado: $horas h $minutos min"
                        }
                    }
                    .addOnFailureListener {
                        binding.tvResumenHoras.text = "Error al cargar fichajes"
                        Toast.makeText(this, "Error al acceder a la empresa", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener {
                binding.tvResumenHoras.text = "Error al obtener empresa"
                Toast.makeText(this, "No se pudo obtener el usuario", Toast.LENGTH_SHORT).show()
            }
    }

    @SuppressLint("SetTextI18n")
    private fun cargarResumenMensual(mes: Int, año: Int) {
        binding.tvResumenHoras.text = "Cargando resumen mensual..."
        binding.layoutFichajes.removeAllViews()

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        FirebaseFirestore.getInstance().collection("usuarios").document(uid).get()
            .addOnSuccessListener { userDoc ->
                val empresaId = userDoc.getString("empresa_id") ?: ""

                FirebaseFirestore.getInstance()
                    .collection("empresas").document(empresaId)
                    .collection("gestionControlHorario")
                    .get()
                    .addOnSuccessListener { documentos ->
                        val horasPorEmpleado = mutableMapOf<String, Int>()

                        for (doc in documentos) {
                            val fecha = doc.getString("fecha") ?: continue
                            val nombre = doc.getString("nombreEmpleado") ?: ""
                            val apellidos = doc.getString("apellidosEmpleado") ?: ""
                            val horaEntrada = doc.getString("horaEntrada") ?: "-"
                            val horaSalida = doc.getString("horaSalida") ?: "-"
                            val uidEmpleadoDoc = doc.getString("uidEmpleado") ?: ""

                            try {
                                val fechaParts = fecha.split("-")
                                val añoDoc = fechaParts[0].toInt()
                                val mesDoc = fechaParts[1].toInt()

                                if (añoDoc == año && mesDoc == mes && (selectedEmpleadoUID == null || selectedEmpleadoUID == uidEmpleadoDoc) && horaEntrada != "-" && horaSalida != "-") {
                                    val formato = SimpleDateFormat("HH:mm", Locale.getDefault())
                                    val entrada = formato.parse(horaEntrada)
                                    val salida = formato.parse(horaSalida)
                                    val diff = salida.time - entrada.time
                                    val minutos = (diff / 60000).toInt()

                                    val key = "$nombre $apellidos"
                                    horasPorEmpleado[key] = horasPorEmpleado.getOrDefault(key, 0) + minutos
                                }
                            } catch (_: Exception) {}
                        }

                        if (horasPorEmpleado.isEmpty()) {
                            binding.tvResumenHoras.text = "No hay fichajes para este mes"
                            return@addOnSuccessListener
                        }

                        binding.tvResumenHoras.text = "Resumen mensual: ${horasPorEmpleado.size} empleados"

                        for ((nombreCompleto, totalMinutos) in horasPorEmpleado) {
                            val horas = totalMinutos / 60
                            val minutos = totalMinutos % 60

                            val view = LayoutInflater.from(this)
                                .inflate(R.layout.item_fichaje, binding.layoutFichajes, false)

                            view.findViewById<TextView>(R.id.tvNombreEmpleado).text = nombreCompleto
                            view.findViewById<TextView>(R.id.tvEntrada).text = "Total: $horas h $minutos min"
                            view.findViewById<TextView>(R.id.tvSalida).text = ""

                            binding.layoutFichajes.addView(view)
                        }
                    }
                    .addOnFailureListener {
                        binding.tvResumenHoras.text = "Error al cargar resumen mensual"
                    }
            }
            .addOnFailureListener {
                binding.tvResumenHoras.text = "Error al obtener empresa"
            }
    }
    @SuppressLint("SetTextI18n")
    private fun cargarResumenAnual(año: Int) {
        binding.tvResumenHoras.text = "Cargando resumen anual..."
        binding.layoutFichajes.removeAllViews()

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        FirebaseFirestore.getInstance().collection("usuarios").document(uid).get()
            .addOnSuccessListener { userDoc ->
                val empresaId = userDoc.getString("empresa_id") ?: ""

                FirebaseFirestore.getInstance()
                    .collection("empresas").document(empresaId)
                    .collection("gestionControlHorario")
                    .get()
                    .addOnSuccessListener { documentos ->
                        val horasPorEmpleado = mutableMapOf<String, Int>()

                        for (doc in documentos) {
                            val fecha = doc.getString("fecha") ?: continue
                            val nombre = doc.getString("nombreEmpleado") ?: ""
                            val apellidos = doc.getString("apellidosEmpleado") ?: ""
                            val horaEntrada = doc.getString("horaEntrada") ?: "-"
                            val horaSalida = doc.getString("horaSalida") ?: "-"
                            val uidEmpleadoDoc = doc.getString("uidEmpleado") ?: ""

                            try {
                                val fechaParts = fecha.split("-")
                                val añoDoc = fechaParts[0].toInt()

                                if (añoDoc == año && (selectedEmpleadoUID == null || selectedEmpleadoUID == uidEmpleadoDoc) && horaEntrada != "-" && horaSalida != "-") {
                                    val formato = SimpleDateFormat("HH:mm", Locale.getDefault())
                                    val entrada = formato.parse(horaEntrada)
                                    val salida = formato.parse(horaSalida)
                                    val diff = salida.time - entrada.time
                                    val minutos = (diff / 60000).toInt()

                                    val key = "$nombre $apellidos"
                                    horasPorEmpleado[key] = horasPorEmpleado.getOrDefault(key, 0) + minutos
                                }
                            } catch (_: Exception) {}
                        }

                        if (horasPorEmpleado.isEmpty()) {
                            binding.tvResumenHoras.text = "No hay fichajes en este año"
                            return@addOnSuccessListener
                        }

                        binding.tvResumenHoras.text = "Resumen anual: ${horasPorEmpleado.size} empleados"

                        for ((nombreCompleto, totalMinutos) in horasPorEmpleado) {
                            val horas = totalMinutos / 60
                            val minutos = totalMinutos % 60

                            val view = LayoutInflater.from(this)
                                .inflate(R.layout.item_fichaje, binding.layoutFichajes, false)

                            view.findViewById<TextView>(R.id.tvNombreEmpleado).text = nombreCompleto
                            view.findViewById<TextView>(R.id.tvEntrada).text = "Total: $horas h $minutos min"
                            view.findViewById<TextView>(R.id.tvSalida).text = ""

                            binding.layoutFichajes.addView(view)
                        }
                    }
                    .addOnFailureListener {
                        binding.tvResumenHoras.text = "Error al cargar resumen anual"
                    }
            }
            .addOnFailureListener {
                binding.tvResumenHoras.text = "Error al obtener empresa"
            }
    }
    private fun toolbar(){
        val barraHerramientas = binding.includeGestionAsistencia.toolbarComun
        setSupportActionBar(barraHerramientas)
        // Cambiar el título del Toolbar
        supportActionBar?.title = "GESTIÓN ASISTENCIA EMPLEADOS"
        menu = ActionBarDrawerToggle(
            this,
            binding.menuGestionAsistencia,
            barraHerramientas,
            R.string.abrir_menu,
            R.string.cerrar_menu)
        binding.menuGestionAsistencia.addDrawerListener(menu)
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

            binding.menuGestionAsistencia.closeDrawer(GravityCompat.START)
            true
        }
    }
}






