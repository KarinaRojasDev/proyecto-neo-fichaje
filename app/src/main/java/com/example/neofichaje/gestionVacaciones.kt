package com.example.neofichaje


import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.example.neofichaje.databinding.ActivityGestionVacacionesBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.prolificinteractive.materialcalendarview.CalendarDay
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class gestionVacaciones : AppCompatActivity() {
    private lateinit var binding: ActivityGestionVacacionesBinding
    private lateinit var menu: ActionBarDrawerToggle
    private var listaEmpleados: ArrayList<CharSequence> = arrayListOf()
    private val mapaVacaciones = mutableMapOf<String, Pair<String, String>>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGestionVacacionesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        toolbar()
        manejarOpcionesMenu()
        cargarSolicitudesVacaciones()
        configurarBotonAplicar()

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
    private fun cargarSolicitudesVacaciones() {
        val db = FirebaseFirestore.getInstance()
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        db.collection("usuarios").document(uid).get().addOnSuccessListener { doc ->
            val empresaId = doc.getString("empresa_id") ?: return@addOnSuccessListener

            db.collection("empresas").document(empresaId)
                .collection("gestionVacaciones")
                .addSnapshotListener { documentos, error ->
                    if (error != null || documentos == null) return@addSnapshotListener

                    listaEmpleados.clear()
                    mapaVacaciones.clear()
                    listaEmpleados.add("Selecciona un empleado")

                    for (documento in documentos) {
                        // FILTRAR solo solicitudes pendientes
                        if (documento.getString("estado") != "pendiente") continue

                        val partes = documento.id.split(" ")
                        if (partes.size < 4) continue
                        val nombreEmpleado = partes.subList(2, partes.size).joinToString(" ")
                        val fechaInicio = documento.getString("fechaInicio") ?: continue
                        val fechaFin = documento.getString("fechaFin") ?: continue

                        listaEmpleados.add(nombreEmpleado)
                        mapaVacaciones[nombreEmpleado] = Pair(fechaInicio, fechaFin)
                    }

                    val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, listaEmpleados)
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    binding.spinnerEmpleados.adapter = adapter

                    binding.spinnerEmpleados.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                            val nombreEmpleado = listaEmpleados[position]
                            val fechas = mapaVacaciones[nombreEmpleado]
                            if (fechas != null) {
                                marcarFechasCalendario(fechas.first, fechas.second)
                            }
                        }

                        override fun onNothingSelected(parent: AdapterView<*>) {}
                    }
                }
        }
    }

    private fun marcarFechasCalendario(fechaInicio: String, fechaFin: String) {
        val formato = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val calInicio = Calendar.getInstance()
        val calFin = Calendar.getInstance()

        try {
            calInicio.time = formato.parse(fechaInicio) ?: return
            calFin.time = formato.parse(fechaFin) ?: return
        } catch (e: ParseException) {
            e.printStackTrace()
            return
        }

        val diasSeleccionados = mutableListOf<CalendarDay>()
        val calActual = calInicio.clone() as Calendar

        while (!calActual.after(calFin)) {
            diasSeleccionados.add(CalendarDay.from(calActual))
            calActual.add(Calendar.DAY_OF_MONTH, 1)
        }

        binding.calendarView.clearSelection()
        for (dia in diasSeleccionados) {
            binding.calendarView.setDateSelected(dia, true)
        }
    }
    private fun configurarBotonAplicar() {
        binding.btnIniSesionEmpresa.setOnClickListener {
            val empleadoSeleccionado = binding.spinnerEmpleados.selectedItem?.toString()
            if (empleadoSeleccionado.isNullOrEmpty()) {
                Toast.makeText(this, "Selecciona un empleado", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val estado = when {
                binding.radioAprobar.isChecked -> "aprobado"
                binding.radioDenegar.isChecked -> "rechazado"
                else -> {
                    Toast.makeText(this, "Selecciona aprobar o denegar", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            }

            val db = FirebaseFirestore.getInstance()
            val uidAdmin = FirebaseAuth.getInstance().currentUser?.uid ?: return@setOnClickListener

            db.collection("usuarios").document(uidAdmin).get().addOnSuccessListener { doc ->
                val empresaId = doc.getString("empresa_id") ?: return@addOnSuccessListener

                db.collection("empresas").document(empresaId)
                    .collection("gestionVacaciones")
                    .get()
                    .addOnSuccessListener { documentos ->
                        for (documento in documentos) {
                            val partes = documento.id.split(" ")
                            if (partes.size < 4) continue
                            val nombreDoc = partes.subList(2, partes.size).joinToString(" ")

                            if (nombreDoc == empleadoSeleccionado) {
                                documento.reference.update("estado", estado)

                                val fechaInicio = documento.getString("fechaInicio") ?: ""
                                val fechaFin = documento.getString("fechaFin") ?: ""
                                val uidEmpleado = documento.getString("uidEmpleado") ?: ""

                                if (uidEmpleado.isNotEmpty()) {
                                    val mensaje = if (estado == "aprobado") {
                                        "Tu solicitud de vacaciones del $fechaInicio al $fechaFin ha sido APROBADA"
                                    } else {
                                        "Tu solicitud de vacaciones del $fechaInicio al $fechaFin ha sido RECHAZADA"
                                    }

                                    //Notificación al empleado
                                    db.collection("usuarios").document(uidEmpleado)
                                        .update("tvVacacionesEmpleado", mensaje)

                                    //Actualizar estado en usuarios
                                    db.collection("usuarios").document(uidEmpleado)
                                        .collection("vacaciones")
                                        .document(documento.id)
                                        .update("estado", estado)

                                    Toast.makeText(this, "Solicitud $estado correctamente", Toast.LENGTH_SHORT).show()

                                    binding.spinnerEmpleados.setSelection(0)
                                    binding.radioGrupo.clearCheck()
                                    binding.calendarView.clearSelection()
                                    break
                                }
                            }
                        }
                    }
            }
        }
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

            binding.menuGestionVacaciones.closeDrawer(GravityCompat.START)
            true
        }
    }
}