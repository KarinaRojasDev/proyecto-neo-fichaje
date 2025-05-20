package com.example.neofichaje

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.View.OnClickListener
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.neofichaje.databinding.ActivityLoginEmpresarioBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore



class activity_login_empresario : AppCompatActivity(), OnClickListener, OnTouchListener {
    private lateinit var binding: ActivityLoginEmpresarioBinding
    private lateinit var auth: FirebaseAuth
    private var esVisible = false
    //------------------------
    private lateinit var googleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 9001
    //-----------------------


    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginEmpresarioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        binding.btnIniSesionEmpresa.setOnClickListener(this)
        binding.etContraseniaEmpresa.setOnTouchListener(this)
        binding.tvRegistrarse.setOnClickListener(this)
        binding.btnGoogle.setOnClickListener(this)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id)) // Lo sacas del archivo google-services.json
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
        binding.btnGoogle.setOnClickListener(this)
        binding.tvCambiarPass.setOnClickListener(this)

    }

    override fun onClick(v: View?) {
        when(v?.id){
            binding.btnIniSesionEmpresa.id -> {
                iniciarSesion()
            }
            binding.tvRegistrarse.id->{
                accionesIntentRegistro()
            }
            binding.tvCambiarPass.id-> {
                recuperarCorreo()
            }
            binding.btnGoogle.id -> {
                val signInIntent = googleSignInClient.signInIntent
                startActivityForResult(signInIntent, RC_SIGN_IN)
            }
        }
    }

    private fun accionesIntentRegistro() {
        val intent = Intent(this, registroUsuario::class.java)
        startActivity(intent)
    }

    private fun recuperarCorreo(){
        val email = binding.etCorreoEmpresa.text.toString().trim()

        if (email.isEmpty()) {
            Toast.makeText(this, "Por favor, introduce tu correo para recuperar la contraseña", Toast.LENGTH_SHORT).show()
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Introduce un correo válido", Toast.LENGTH_SHORT).show()
            return
        }

        auth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                Toast.makeText(this, "Te enviamos un enlace de recuperación a tu correo", Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al enviar el correo. Verifica que esté registrado.", Toast.LENGTH_LONG).show()
            }
    }

    private fun iniciarSesion() {
        val correo = binding.etCorreoEmpresa.text.toString().trim()
        val contrasenia = binding.etContraseniaEmpresa.text.toString().trim()

        if (correo.isEmpty() || contrasenia.isEmpty()) {
            Snackbar.make(binding.root, "Por favor, completa todos los campos", Snackbar.LENGTH_LONG).show()
            return
        }

        // Verificar si el correo está registrado con otro proveedor (ej: Google)
        auth.fetchSignInMethodsForEmail(correo)
            .addOnSuccessListener { result ->
                val providers = result.signInMethods ?: emptyList()

                when {
                    "password" in providers -> {
                        // Si tiene método password, intentamos login normal
                        auth.signInWithEmailAndPassword(correo, contrasenia)
                            .addOnCompleteListener(this) { task ->
                                if (task.isSuccessful) {
                                    verificarYRedirigir()
                                } else {
                                    Snackbar.make(binding.root, "Correo o contraseña incorrectos.", Snackbar.LENGTH_LONG).show()
                                }
                            }
                    }

                    "google.com" in providers -> {
                        // Si el correo fue registrado con Google
                        Snackbar.make(binding.root, "Este correo está registrado con Google. Usa el botón de Google para iniciar sesión.", Snackbar.LENGTH_LONG).show()
                    }

                    else -> {
                        Snackbar.make(binding.root, "No existe una cuenta con este correo. Regístrate primero.", Snackbar.LENGTH_LONG).show()
                    }
                }
            }
            .addOnFailureListener {
                Snackbar.make(binding.root, "Error al verificar el correo. Intenta más tarde.", Snackbar.LENGTH_LONG).show()
            }
    }
    private fun verificarYRedirigir() {
        val userId = auth.currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        db.collection("usuarios").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val puesto = document.getString("puesto") ?: ""
                    redirigirSegunPuesto(puesto)
                } else {
                    auth.signOut()
                    Snackbar.make(binding.root, "No se encontró el usuario", Snackbar.LENGTH_LONG).show()
                }
            }
            .addOnFailureListener {
                auth.signOut()
                Snackbar.make(binding.root, "Error al verificar el puesto del usuario.", Snackbar.LENGTH_LONG).show()
            }
    }
    private fun redirigirSegunPuesto(puesto: String) {
        when (puesto) {
            "Tecnico" -> startActivity(Intent(this, inicio_empleado::class.java))
            "Administrador", "Gerente", "RRHH" -> startActivity(Intent(this, inicio_empresario::class.java))
            else -> {
                auth.signOut()
                Snackbar.make(binding.root, "Tu cuenta no está autorizada aún. Contacta con tu empresa.", Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun limpiar(){
        binding.etCorreoEmpresa.text.clear()
        binding.etContraseniaEmpresa.text.clear()
    }
    // EMPRESARIOPARA VOLVER A LOGIN Y ESTE LIMPIO ESTA PANTALLA
    override fun onRestart() {
        super.onRestart()
        limpiar()
    }
    private fun cambiarVisibilidadContrasenia() {
        val campoContrasenia = binding.etContraseniaEmpresa
        if (esVisible) {
            campoContrasenia.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        } else {
            campoContrasenia.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        campoContrasenia.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_lock, 0, R.drawable.ic_eye, 0)
        campoContrasenia.setSelection(campoContrasenia.text.length)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        when (v?.id) {
            binding.etContraseniaEmpresa.id -> {
                if (event?.action == MotionEvent.ACTION_UP) {
                    val iconoDerecho = 2
                    val iconoOjito = binding.etContraseniaEmpresa.compoundDrawables[iconoDerecho]

                    if (iconoOjito != null) {
                        val anchoIcono = iconoOjito.bounds.width()
                        val inicioZonaToque = binding.etContraseniaEmpresa.width -
                                binding.etContraseniaEmpresa.paddingEnd - anchoIcono

                        if (event.x >= inicioZonaToque) {
                            esVisible = !esVisible
                            cambiarVisibilidadContrasenia()
                            return true
                        }
                    }
                }
            }
        }
        return false
    }
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Snackbar.make(binding.root, "Fallo en el login con Google", Snackbar.LENGTH_LONG).show()
            }
        }

    }
    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        FirebaseAuth.getInstance().signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = FirebaseAuth.getInstance().currentUser
                    // Puedes redirigir aquí según el puesto si ya está registrado
                    val uid = user?.uid ?: return@addOnCompleteListener
                    FirebaseFirestore.getInstance().collection("usuarios").document(uid).get()
                        .addOnSuccessListener { doc ->
                            if (doc.exists()) {
                                val puesto = doc.getString("puesto") ?: ""
                                when (puesto) {
                                    "Tecnico" -> startActivity(Intent(this, inicio_empleado::class.java))
                                    "Administrador", "Gerente", "RRHH" -> startActivity(Intent(this, inicio_empresario::class.java))
                                    else -> {
                                        FirebaseAuth.getInstance().signOut()
                                        Snackbar.make(binding.root, "Tu cuenta no está autorizada aún. Contacta con tu empresa.", Snackbar.LENGTH_LONG).show()
                                    }
                                }
                            } else {
                                FirebaseAuth.getInstance().signOut()
                                Snackbar.make(binding.root, "Necesitas registrarte antes de iniciar sesión con Google", Snackbar.LENGTH_LONG).show()
                            }
                        }
                        .addOnFailureListener {
                            FirebaseAuth.getInstance().signOut()
                            Snackbar.make(binding.root, "Error al verificar usuario en la base de datos", Snackbar.LENGTH_LONG).show()
                        }
                } else {
                    Snackbar.make(binding.root, "Autenticación fallida", Snackbar.LENGTH_LONG).show()
                }
            }
    }
}