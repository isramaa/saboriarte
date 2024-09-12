package com.example.firebase

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.firebase.databinding.FragmentAddUserBinding
import com.example.firebase.databinding.FragmentUserBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.*
import java.util.zip.Inflater
//TODO: Resulver el problema de fab

class AddUserFragment: Fragment() {
    private lateinit var mBinding: FragmentAddUserBinding
    private var mActivity: HomeActivity? = null
    private var mIsEditMode: Boolean = false
    private var mUser: User? = null

    private var mPhotoUri: Uri? = null
    private val RC_GALLERY = 83
    private var isUploading = false
    private var urlDownload = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentAddUserBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    //se llama después de que la vista de un fragmento se haya creado y
    //proporciona una oportunidad para inicializar y configurar elementos de la vista.
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (arguments != null){
            mIsEditMode = true
            val args = requireArguments()
            val id = args.getString("id")
            val name = args.getString("name")
            val lastName = args.getString("lastName")
            val email = args.getString("email")
            val phone = args.getString("phone")
            val photoUrl = args.getString("photoUrl")
            val isAdmin = args.getBoolean("isAdmin")
            val isOwner = args.getBoolean("isOwner")
            val editableUser = User(
                id = id!!,
                name = name!!,
                lastName = lastName!!,
                email = email!!,
                phone = phone!!,
                photoUrl = photoUrl!!,
                isAdmin = isAdmin,
                isOwner = isOwner,
                password = ""
            )
            mUser = editableUser
            setUIUser(editableUser)
        }else{
            mIsEditMode = false
            mUser = User()
        }

        mActivity = activity as? HomeActivity
        setupSaveButton()
    }

    //destruir el fab(la burbuja de add) cuando se destruya el ffragment?
    override fun onDestroy() {
        mActivity?.toggleFab(true)
        super.onDestroy()
    }

    //boton para guardar usuario de agregacion
    private fun setupSaveButton(){
        mBinding.btnAddSave.setOnClickListener{
            if (mUser != null){
                println(mUser)
                println(urlDownload)
                with(mUser!!){
                    id = mBinding.editAddEmail.text.toString().trim()
                    email = mBinding.editAddEmail.text.toString().trim()
                    password = mBinding.editAddPass.text.toString().trim()
                    name = mBinding.editAddName.text.toString().trim()
                    lastName = mBinding.editAddLastName.text.toString().trim()
                    phone = mBinding.editAddPhone.text.toString().trim()
                    isAdmin = mBinding.cbIsAdmin.isChecked
                    photoUrl = mUser!!.photoUrl
                }
                if (validateUser()){

                if (mIsEditMode){ updateUser(mUser!!) } else { createUser(mUser!!) }
                }else{
                    val dialog = Dialogs()
                    dialog.alertError("Todos los campos son obligatorios", mActivity!!)
                }
            }
        }

        mBinding.imgAddUser.setOnClickListener{
            openGallery()
        }
    }

    //Funcion para añadir usuario
    private fun createUser(user: User){
        val db = FirebaseFirestore.getInstance()


        //Agregacion a firebase ó addUser a firebase junto a add a AUTH en firebase
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(user.id, user.password).addOnSuccessListener {
            db.collection("usuarios").document(user.id).set(hashMapOf(
                "email" to user.email,
                "name" to user.name,
                "lastName" to user.lastName,
                "phone" to user.phone,
                "isAdmin" to user.isAdmin,
                "isOwner" to user.isOwner,
                "photoUrl" to user.photoUrl
            )
            ).addOnSuccessListener {
                Toast.makeText(context,"Usuario creado exitosamente", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener {
                val dialog = Dialogs()
                dialog.alertError(it.message.toString(),mActivity!!)
            }
        }.addOnFailureListener {
            val dialog = Dialogs()
            dialog.alertError(it.message.toString(), mActivity!!)
        }




    }
//Actualiza un documento de usuario en Firestore, la base de datos en tiempo real de Firebase
    private fun updateUser(user: User){
        val db = FirebaseFirestore.getInstance()
        val docRef = db.collection("usuarios").document(user.id)

        val updates = hashMapOf(
            "email" to user.email,
            "name" to user.name,
            "lastName" to user.lastName,
            "phone" to user.phone,
            "isAdmin" to user.isAdmin,
            "isOwner" to user.isOwner,
            "photoUrl" to user.photoUrl
        )
        db.runTransaction { transaction ->
            transaction.update(docRef, updates as Map<String, Any>)
        }.addOnSuccessListener {
            mActivity?.onBackPressedDispatcher?.onBackPressed()
        }
    }

    //Validar si los campos de un formulario de registro de usuario están llenos o no
    private fun validateUser(): Boolean{
        var editPass = true
        if (!mIsEditMode){
            editPass = mBinding.editAddPass.text!!.isNotEmpty()
        }
        return mBinding.editAddName.text!!.isNotEmpty() &&
                mBinding.editAddLastName.text!!.isNotEmpty() &&
                mBinding.editAddPhone.text!!.isNotEmpty() &&
                mBinding.editAddEmail.text!!.isNotEmpty() &&
                editPass

    }

    //Actualizar la interfaz de usuario (UI) con la información del usuario proporcionado como parámetro de entrada
    private fun setUIUser(user: User){
        if (mIsEditMode){
            mBinding.tilAddPass.isGone = true
        }
        with(mBinding){
            editAddName.setText(user.name)
            editAddLastName.setText(user.lastName)
            editAddEmail.setText(user.email)
            editAddPhone.setText(user.phone)
            cbIsAdmin.isChecked = user.isAdmin
            if (user.photoUrl != "") {
                Glide.with(requireActivity())
                    .load(user.photoUrl)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .centerCrop()
                    .into(imgAddUser)
            }
        }
    }

    //sube una imagen seleccionada por el usuario a Firebase Storage y
    //obtiene la URL de descarga de la imagen subida para ser almacenada en Firestore y mostrarla en la interfaz de usuario.
    private fun uploadImage(uri: Uri){
        val formater = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.getDefault())
        val now = Date()
        val fileName = formater.format(now)

        val storage = FirebaseStorage.getInstance().getReference("imagenes/usuarios/${fileName}")
        var uploadTask = storage.putFile(uri)
        isUploading = true
        mBinding.btnAddSave.isEnabled = false
        val urlTask = uploadTask.continueWithTask{ task ->
            if (!task.isSuccessful){
                task.exception?.let {
                    throw it
                }
            }
            storage.downloadUrl
        }.addOnCompleteListener { task ->
            println(isUploading)
            if (task.isSuccessful){
                urlDownload = task.result.toString()
                mUser!!.photoUrl = urlDownload
            }else{
                Toast.makeText(context, "La imagen no se pudo subir correctamente", Toast.LENGTH_SHORT).show()
            }
            mBinding.btnAddSave.isEnabled = true
        }
    }

    //para abrir la galería de imágenes del dispositivo para que el usuario pueda seleccionar una imagen.
    private fun openGallery(){
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, RC_GALLERY)
    }

    //recibir y manejar los resultados devueltos por una actividad que fue iniciada mediante startActivityForResult()
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK){
            if (requestCode == RC_GALLERY){
                mPhotoUri = data?.data
                mBinding.imgAddUser.setImageURI(mPhotoUri)
                uploadImage(mPhotoUri!!)
            }
        }
    }

}