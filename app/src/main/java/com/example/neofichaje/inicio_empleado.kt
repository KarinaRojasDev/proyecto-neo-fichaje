package com.example.neofichaje


import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.example.neofichaje.databinding.ActivityInicioEmpleadoBinding
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar
import java.util.Locale


class inicio_empleado : AppCompatActivity(), OnClickListener {
    private lateinit var binding: ActivityInicioEmpleadoBinding
    private lateinit var menu: ActionBarDrawerToggle


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityInicioEmpleadoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        configurarToolbar()
        manejarOpcionesMenu()
        mostrarNotificacionesEmpleado()
        comprobarEstadoFichaje()
        binding.notificationEmpleados.setOnClickListener(this)

    }
    override fun onClick(v: View?) {
        when(v?.id){
            binding.notificationEmpleados.id->{
                startActivity(Intent(this, empleado_control_horario::class.java))
            }
        }
    }

    private fun mostrarNotificacionesEmpleado() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        db.collection("usuarios").document(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null || !snapshot.exists()) {
                    return@addSnapshotListener
                }

                val notiNomina = snapshot.getString("tvNominas")
                val notiContrato = snapshot.getString("tvContrato")
                val notiVacaciones = snapshot.getString("tvVacacionesEmpleado")
                val notiPermisos = snapshot.getString("tvPermisos")
                val notiLeida = snapshot.getString("tvSolicitudLeida")

                //NOTIFICACION EMPRESARIO ACEPTO TU SOLICITUD :)
                if (!notiLeida.isNullOrEmpty()) {
                    binding.solicitudLeida.visibility = View.VISIBLE
                    binding.tvsolicitudAceptada.text = notiLeida
                    binding.solicitudLeida.setOnClickListener {
                        binding.solicitudLeida.visibility = View.GONE
                        // OPCIONAL: si quieres también limpiar de Firestore
                        db.collection("usuarios").document(uid).update("tvSolicitudLeida", "")
                    }
                } else {
                    binding.solicitudLeida.visibility = View.GONE
                }

                //NOTIFICACION PERMISO BAJA
                if (!notiPermisos.isNullOrEmpty()) {
                    binding.permiso.visibility = View.VISIBLE
                    binding.tvPermisos.text = notiPermisos
                    binding.permiso.setOnClickListener {
                        // Verifica si ya pasó la fecha fin antes de borrar
                        val permisosRef = db.collection("usuarios").document(uid)
                            .collection("permisos_bajas")
                            .orderBy("fechaFin")
                            .limitToLast(1)

                        permisosRef.get().addOnSuccessListener { docs ->
                            if (!docs.isEmpty) {
                                val fechaFinStr = docs.first().getString("fechaFin") ?: return@addOnSuccessListener
                                val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                                val fechaFin = sdf.parse(fechaFinStr)
                                val hoy = Calendar.getInstance().time

                                if (fechaFin != null && hoy.after(fechaFin)) {
                                    // Solo si ya pasó la fecha fin borramos la notificación
                                    db.collection("usuarios").document(uid).update("tvPermisos", "")
                                    binding.permiso.visibility = View.GONE
                                } else {
                                    Toast.makeText(this, "La notificación no se puede quitar hasta que finalice el permiso", Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    }
                } else {
                    binding.permiso.visibility = View.GONE
                }

                //NOTIFICACION NOMINA
                if (!notiNomina.isNullOrEmpty()) {
                    binding.nomina.visibility = View.VISIBLE
                    binding.tvNominas.text = notiNomina
                    binding.nomina.setOnClickListener {
                        // Al pulsar, abre pantalla y borra notificación
                        startActivity(Intent(this, nominas_empleado::class.java))
                        db.collection("usuarios").document(uid).update("tvNominas", "")
                    }
                } else {
                    binding.nomina.visibility = View.GONE
                }

                //NOTIFICACION CONTRATO
                if (!notiContrato.isNullOrEmpty()) {
                    binding.contrato.visibility = View.VISIBLE
                    binding.tvContrato.text = notiContrato
                    binding.contrato.setOnClickListener {
                        // Al pulsar, abre pantalla y borra notificación
                        startActivity(Intent(this, contratoEmpleado::class.java))
                        db.collection("usuarios").document(uid).update("tvContrato", "")
                    }
                } else {
                    binding.contrato.visibility = View.GONE
                }

                //NOTIFICACION VACACIONES
                if (!notiVacaciones.isNullOrEmpty()) {
                    binding.notificationVacaciones.visibility = View.VISIBLE
                    binding.tvVacacionesEmpleado.text = notiVacaciones
                    //binding.notificationVacaciones.setOnClickListener {
                    //}
                } else {
                    binding.notificationVacaciones.visibility = View.GONE
                }
            }
    }


    @SuppressLint("SetTextI18n")
    private fun comprobarEstadoFichaje() {
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
                    binding.notificationEmpleados.visibility = View.VISIBLE
                    binding.tvRegisterHours.text = "Has fichado a las $horaEntrada"
                } else {
                    binding.notificationEmpleados.visibility = View.GONE
                }
            }
    }
    private fun configurarToolbar() {
        val barraHerramientas = binding.includeInicioEmpleado.toolbarComun
        setSupportActionBar(barraHerramientas)

        // Cambiar el título del Toolbar
        supportActionBar?.title = "INICIO"

        menu = ActionBarDrawerToggle(
            this,
            binding.inicioEmpleado, barraHerramientas,
            R.string.abrir_menu,
            R.string.cerrar_menu
        )

        binding.inicioEmpleado.addDrawerListener(menu)
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

            binding.inicioEmpleado.closeDrawer(GravityCompat.START)
            true
        }
    }
    override  fun onResume(){
        super.onResume()
        comprobarEstadoFichaje()
    }
}