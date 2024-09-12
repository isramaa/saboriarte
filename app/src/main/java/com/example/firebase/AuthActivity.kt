package com.example.firebase

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.example.firebase.databinding.ActivityAuthBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class AuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthBinding
    private val messages = ExceptionsMessages()
    private val dialogs = Dialogs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        setup()
        session()
    }
    private fun setup(){
        title = "Bienvenido"

        binding.btnLogin.setOnClickListener{
            if(binding.editEmail.text!!.isNotEmpty() && binding.editPass.text!!.isNotEmpty()){
                print(binding.editEmail.text!!.toString())
                FirebaseAuth.getInstance().signInWithEmailAndPassword(binding.editEmail.text.toString(), binding.editPass.text.toString()).addOnCompleteListener{
                    if(it.isSuccessful){
                        intentHome(it.result?.user?.email ?:"", proveedor = "BASIC")
                    }else dialogs.alertError(messages.getErrorString(it.exception?.message), this)

                }
            }
        }

    }

    override fun onStart() {
        super.onStart()
        binding.constraintLayoutAuth.visibility = View.VISIBLE
    }

    //este método verifica si el usuario ya ha iniciado sesión anteriormente
    //en la aplicación y si es así lo redirige a la página de inicio
    private fun session(){
        val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)
        val email: String? = prefs.getString("email", null)
        if(email != null ){
            binding.constraintLayoutAuth.visibility = View.INVISIBLE
            intentHome(email, "BASIC")
        }
    }
    //iniciar la actividad "HomeActivity" con los datos necesarios para la sesión iniciada
    private fun intentHome(email: String, proveedor: String){
        val intent = Intent(this, HomeActivity::class.java).apply {
            putExtra("email", email)
            putExtra("proveedor", proveedor)
        }
        startActivity(intent)
    }

}