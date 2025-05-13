package com.example.neofichaje


import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.example.neofichaje.databinding.ActivityInicioEmpleadoBinding
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import com.google.firebase.firestore.FirebaseFirestore
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
                if (!notiVacaciones.isNullOrEmpty()) {
                    binding.notificationVacaciones.visibility = View.VISIBLE
                    binding.tvVacacionesEmpleado.text = notiVacaciones
                    binding.notificationVacaciones.setOnClickListener {
                        //  podemos abrir un historial de vacaciones
                        // o simplemente limpiar la notificación
                        db.collection("usuarios").document(uid).update("tvVacacionesEmpleado", "")
                        binding.notificationVacaciones.visibility = View.GONE
                    }
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