package com.example.neofichaje

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.view.View.OnClickListener
import androidx.appcompat.widget.Toolbar
import android.view.View.OnTouchListener
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import com.example.neofichaje.databinding.ActivityCambioContraseniaBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class cambio_contrasenia : AppCompatActivity(), OnClickListener, OnTouchListener {
    private lateinit var binding: ActivityCambioContraseniaBinding
    private lateinit var menu: ActionBarDrawerToggle
    private lateinit var auth: FirebaseAuth
    private var esVisible = false

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding= ActivityCambioContraseniaBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()

        toolbar()
        manejarOpcionesMenu()
        binding.btnGuardarPass.setOnClickListener(this)
        binding.etPassActual.setOnTouchListener(this)
        binding.etNuevaContrasenia.setOnTouchListener(this)
        binding.etConfirmeContrasenia.setOnTouchListener(this)

    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.btnGuardarPass.id -> {
                cambiarContrasenia(v)
            }
        }
    }
    private fun toolbar(){
        val toolbar = binding.includeEditarContrasenia.toolbarComun
        setSupportActionBar(toolbar)
        supportActionBar?.title = "CAMBIO DE CONTRASEÑA"

        menu = ActionBarDrawerToggle(
            this,
            binding.menuCambioPass,
            toolbar,
            R.string.abrir_menu,
            R.string.cerrar_menu)
        binding.menuCambioPass.addDrawerListener(menu)
        menu.syncState()
    }
    private fun cambiarContrasenia(vista: View) {
        val passActual = binding.etPassActual.text.toString().trim()
        val nuevaContrasenia = binding.etNuevaContrasenia.text.toString().trim()
        val confirmarContrasenia = binding.etConfirmeContrasenia.text.toString().trim()

        if (passActual.isEmpty() || nuevaContrasenia.isEmpty() || confirmarContrasenia.isEmpty()) {
            Snackbar.make(vista, "Por favor completa todos los campos", Snackbar.LENGTH_LONG).show()
            return
        }

        if (nuevaContrasenia != confirmarContrasenia) {
            Snackbar.make(vista, "La nueva contraseña no coincide", Snackbar.LENGTH_LONG).show()
            return
        }

        val user = auth.currentUser
        val email = user?.email ?: return

        // Primero reautenticamos al usuario con su contraseña actual
        val credential = com.google.firebase.auth.EmailAuthProvider.getCredential(email, passActual)

        user.reauthenticate(credential)
            .addOnSuccessListener {
                // Ahora sí cambiamos la contraseña
                user.updatePassword(nuevaContrasenia)
                    .addOnSuccessListener {
                        Snackbar.make(vista, "Contraseña actualizada correctamente", Snackbar.LENGTH_LONG).show()
                        binding.etPassActual.text.clear()
                        binding.etNuevaContrasenia.text.clear()
                        binding.etConfirmeContrasenia.text.clear()
                    }
                    .addOnFailureListener {
                        Snackbar.make(vista, "Error al actualizar la contraseña", Snackbar.LENGTH_LONG).show()
                    }
            }
            .addOnFailureListener {
                Snackbar.make(vista, "La contraseña actual es incorrecta", Snackbar.LENGTH_LONG).show()
            }
    }



    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        when (v?.id) {
            binding.etPassActual.id -> {
                if (event?.action == MotionEvent.ACTION_UP) {
                    val posicionIconoDerecha = 2
                    val iconoOjito = binding.etPassActual.compoundDrawables[posicionIconoDerecha]

                    if (iconoOjito != null) {
                        val anchoIcono = iconoOjito.bounds.width()
                        val inicioZonaToque = binding.etPassActual.width - binding.etPassActual.paddingEnd - anchoIcono

                        if (event.x >= inicioZonaToque) {
                            esVisible = !esVisible
                            cambiarVisibilidadContrasenia(binding.etPassActual)
                            return true
                        }
                    }
                }
            }
            binding.etNuevaContrasenia.id -> {
                if (event?.action == MotionEvent.ACTION_UP) {
                    val posicionIconoDerecha = 2
                    val iconoOjito = binding.etNuevaContrasenia.compoundDrawables[posicionIconoDerecha]

                    if (iconoOjito != null) {
                        val anchoIcono = iconoOjito.bounds.width()
                        val inicioZonaToque = binding.etNuevaContrasenia.width - binding.etNuevaContrasenia.paddingEnd - anchoIcono

                        if (event.x >= inicioZonaToque) {
                            esVisible = !esVisible
                            cambiarVisibilidadContrasenia(binding.etNuevaContrasenia)
                            return true
                        }
                    }
                }
            }
            binding.etConfirmeContrasenia.id -> {
                if (event?.action == MotionEvent.ACTION_UP) {
                    val posicionIconoDerecha = 2
                    val iconoOjito = binding.etConfirmeContrasenia.compoundDrawables[posicionIconoDerecha]

                    if (iconoOjito != null) {
                        val anchoIcono = iconoOjito.bounds.width()
                        val inicioZonaToque = binding.etConfirmeContrasenia.width - binding.etConfirmeContrasenia.paddingEnd - anchoIcono

                        if (event.x >= inicioZonaToque) {
                            esVisible = !esVisible
                            cambiarVisibilidadContrasenia(binding.etConfirmeContrasenia)
                            return true
                        }
                    }
                }
            }
        }
        return false
    }

    private fun cambiarVisibilidadContrasenia(view: View) {
        when (view.id) {
            binding.etPassActual.id -> {
                if (esVisible) {
                    binding.etPassActual.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                    binding.etPassActual.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_eye, 0)
                } else {
                    binding.etPassActual.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                    binding.etPassActual.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_eye, 0)
                }
                binding.etPassActual.setSelection(binding.etPassActual.text.length)
            }
            binding.etNuevaContrasenia.id -> {
                if (esVisible) {
                    binding.etNuevaContrasenia.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                    binding.etNuevaContrasenia.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_eye, 0)
                } else {
                    binding.etNuevaContrasenia.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                    binding.etNuevaContrasenia.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_eye, 0)
                }
                binding.etNuevaContrasenia.setSelection(binding.etNuevaContrasenia.text.length)
            }
            binding.etConfirmeContrasenia.id -> {
                if (esVisible) {
                    binding.etConfirmeContrasenia.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                    binding.etConfirmeContrasenia.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_eye, 0)
                } else {
                    binding.etConfirmeContrasenia.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                    binding.etConfirmeContrasenia.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_eye, 0)
                }
                binding.etConfirmeContrasenia.setSelection(binding.etConfirmeContrasenia.text.length)
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
            binding.menuCambioPass.closeDrawer(GravityCompat.START)
            true
        }
    }
}