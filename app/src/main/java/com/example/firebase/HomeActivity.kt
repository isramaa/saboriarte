package com.example.firebase

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.GridLayoutManager
import com.example.firebase.databinding.ActivityHomeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import io.grpc.NameResolver.Args


class HomeActivity : AppCompatActivity() {
    private lateinit var mBinding: ActivityHomeBinding
    private lateinit var mActivityFragment: Fragment
    private lateinit var mFragmentManager: FragmentManager


    //private val db = FirebaseFirestore.getInstance()

    /*Inflar el diseño de la actividad a partir del archivo de diseño ActivityHomeBinding.
    Establecer el diseño inflado como el contenido de la actividad con el método setContentView().
    Obtener el valor del correo electrónico del usuario a través de los extras del intent que lanzó la actividad y guardarlo en SharedPreferences para que no tenga que iniciar sesión de nuevo.
    Establecer el título de la actividad en "Informes".
    Configurar la barra de navegación inferior mediante el método setupBottom().
    Crear una instancia de la clase UserDAO.*/
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //"declaracion" de existencia de BINDING (sirve para acceder a las caracteristicas de nuestro xml
        mBinding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        //Esto es para evitar iniciar sesion otra vez
        val bundle: Bundle? = intent.extras
        //el signo "?" es para decir que el valor no es nulo
        val email = bundle?.getString("email")
        val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit()
        prefs.putString("email", email)
        prefs.apply()

        title = "Productos"
        //Barra de navegacion
        setupBottom()
        //cerrarTemporal()

        val dao: UserDAO = UserDAO()

    }

    //Barra de navegacion
    private fun setupBottom() {
        //Esta funcion es para inflar los fragmentos en la actividad principal
        mFragmentManager = supportFragmentManager
        //Se instancian los fragmentos

        val infoFragment = InfoFragment()

        val productFragment = ProductFragment()
        val userFragment = UserFragment()

        //administrar los fragmentos en una actividad
        mActivityFragment = productFragment
        mBinding.fabMain.setOnClickListener { launchAddProductFragment(null) }
        //Se adhieren al contenedor
        mFragmentManager.beginTransaction()
            .add(R.id.hostFragment, userFragment, UserFragment::class.java.name)
            .hide(userFragment)
            .commit()
        mFragmentManager.beginTransaction()
            .add(R.id.hostFragment, productFragment, ProductFragment::class.java.name)
            .commit()
       /* mFragmentManager.beginTransaction()
            .add(R.id.hostFragment, infoFragment, InfoFragment::class.java.name)
            .commit()*/


        //El código se utiliza para manejar la selección de
        //elementos en la barra de navegación inferior en la aplicación y actualizar los fragmentos
        //y el botón flotante (add) según el elemento seleccionado.
        mBinding.bottomNav.setOnItemSelectedListener {
            when(it.itemId){
                /*R.id.action_info -> {
                    mFragmentManager.beginTransaction().hide(mActivityFragment).show(infoFragment).commit()
                    mActivityFragment = infoFragment
                    //mBinding.fabMain.setOnClickListener {  }
                    title = "Informes"
                    true
                }*/
                R.id.action_products -> {
                    mFragmentManager.beginTransaction().hide(mActivityFragment).show(productFragment).commit()
                    mActivityFragment = productFragment
                    toggleFab(true)
                    mBinding.fabMain.setOnClickListener { launchAddProductFragment(null) }
                    title = "Productos"
                    true
                }
                R.id.action_users -> {
                    mFragmentManager.beginTransaction().hide(mActivityFragment).show(userFragment).commit()
                    mActivityFragment = userFragment
                    mBinding.fabMain.setOnClickListener { launchAddUserFragment(null) }
                    toggleFab(true)
                    //mBinding.fabMain.setOnClickListener { launchAddUserFragment(null) }
                    title = "Usuarios"
                    true
                }
                else -> false
            }
        }
    }

    private fun logout(){
        val prefs = getSharedPreferences("com.example.firebase.PREFERENCE_FILE_KEY", Context.MODE_PRIVATE).edit()
        prefs.clear()
        prefs.apply()
        FirebaseAuth.getInstance().signOut()
        val intent = Intent(mBinding.fabMain.context, AuthActivity::class.java)
        startActivity(intent)
    }

    //es una función que se encarga de iniciar un nuevo fragmento AddUserFragment() en la actividad actual. fragmento de usuario
    fun launchAddUserFragment(args: Bundle? = null){
        val fragment = AddUserFragment()
        if (args != null){
            fragment.arguments = args
        }
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.add(R.id.containerUsers, fragment)
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()
        toggleFab(false)

    }

    //crear y lanzar nuevo fragmento de prooducto
    fun launchAddProductFragment(args: Bundle? = null){
        // val para fragmentos
        val fragment = AddProductFragment()
        if (args != null){
            fragment.arguments = args
        }
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.add(R.id.containerProducts, fragment)
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()
        toggleFab(false)
    }

    fun toggleFab(visivility: Boolean){
        if (visivility) mBinding.fabMain.show() else mBinding.fabMain.hide()
    }



}