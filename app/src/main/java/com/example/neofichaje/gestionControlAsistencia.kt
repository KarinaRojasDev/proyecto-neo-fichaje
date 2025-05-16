package com.example.neofichaje

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.neofichaje.databinding.ActivityGestionControlAsistenciaBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class gestionControlAsistencia : AppCompatActivity() {
    private lateinit var binding: ActivityGestionControlAsistenciaBinding
    private lateinit var menu: ActionBarDrawerToggle
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var adapter: FichajeAdapter
    private val listaFichajes = mutableListOf<Fichaje>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGestionControlAsistenciaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        toolbar()
        manejarOpcionesMenu()
        setupRecyclerView()
        configurarCalendario()
    }

    private fun toolbar() {
        val barraHerramientas = binding.includeGestionAsistencia.toolbarComun
        setSupportActionBar(barraHerramientas)
        supportActionBar?.title = "GESTIÓN CONTROL HORARIO"
        menu = ActionBarDrawerToggle(
            this,
            binding.menuGestionAsistencia,
            barraHerramientas,
            R.string.abrir_menu,
            R.string.cerrar_menu)
        binding.menuGestionAsistencia.addDrawerListener(menu)
        menu.syncState()
    }

    private fun setupRecyclerView() {
        adapter = FichajeAdapter(listaFichajes)
        binding.recyclerFichajes.layoutManager = LinearLayoutManager(this)
        binding.recyclerFichajes.adapter = adapter
    }

    private fun configurarCalendario() {
        binding.calendarView.setOnDateChangedListener(OnDateSelectedListener { _, date, _ ->
            val formato = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val fechaSeleccionada = formato.format(Calendar.getInstance().apply {
                set(date.year, date.month - 1, date.day)
            }.time)
            cargarFichajesPorFecha(fechaSeleccionada)
        })
    }

    @SuppressLint("SetTextI18n")
    private fun cargarFichajesPorFecha(fecha: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        db.collection("usuarios").document(uid).get().addOnSuccessListener { doc ->
            val empresaId = doc.getString("empresa_id") ?: return@addOnSuccessListener

            db.collection("empresas").document(empresaId)
                .collection("gestionControlHorario")
                .whereEqualTo("fecha", fecha)
                .get()
                .addOnSuccessListener { documentos ->
                    listaFichajes.clear()
                    var totalHoras = 0L

                    for (documento in documentos) {
                        val nombre = documento.getString("nombreEmpleado") ?: continue
                        val apellidos = documento.getString("apellidosEmpleado") ?: ""
                        val entrada = documento.getString("horaEntrada") ?: continue
                        val salida = documento.getString("horaSalida") ?: ""
                        val horas = calcularHorasTrabajadas(entrada, salida)
                        totalHoras += horas

                        listaFichajes.add(
                            Fichaje("$nombre $apellidos", entrada, salida, horas)
                        )
                    }

                    adapter.notifyDataSetChanged()
                    binding.tvResumenHoras.text = "Total horas trabajadas: $totalHoras h"
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error al cargar fichajes", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun calcularHorasTrabajadas(horaEntrada: String, horaSalida: String): Long {
        if (horaEntrada.isEmpty() || horaSalida.isEmpty()) return 0
        return try {
            val formato = SimpleDateFormat("HH:mm", Locale.getDefault())
            val entrada = formato.parse(horaEntrada)
            val salida = formato.parse(horaSalida)
            if (entrada != null && salida != null) {
                val diferenciaMillis = salida.time - entrada.time
                TimeUnit.MILLISECONDS.toHours(diferenciaMillis)
            } else 0
        } catch (e: Exception) {
            0
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

            binding.menuGestionAsistencia.closeDrawer(GravityCompat.START)
            true
        }
    }
}

// Modelo de datos
   data class Fichaje(val nombre: String, val entrada: String, val salida: String, val horas: Long)

// Adaptador RecyclerView (básico)
    class FichajeAdapter(private val lista: List<Fichaje>) : androidx.recyclerview.widget.RecyclerView.Adapter<FichajeViewHolder>() {
    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): FichajeViewHolder {
        val vista = android.view.LayoutInflater.from(parent.context).inflate(android.R.layout.simple_list_item_2, parent, false)
        return FichajeViewHolder(vista)
    }

    override fun onBindViewHolder(holder: FichajeViewHolder, position: Int) {
        val item = lista[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int = lista.size
}

class FichajeViewHolder(itemView: android.view.View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
    @SuppressLint("SetTextI18n")
    fun bind(fichaje: Fichaje) {
        val tv1 = itemView.findViewById<android.widget.TextView>(android.R.id.text1)
        val tv2 = itemView.findViewById<android.widget.TextView>(android.R.id.text2)
        tv1.text = fichaje.nombre
        tv2.text = "Entrada: ${fichaje.entrada} - Salida: ${fichaje.salida} - Horas: ${fichaje.horas}h"
    }

}
