package com.example.firebase

import android.app.Activity
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.firebase.databinding.FragmentAddProductBinding
import com.google.android.material.chip.ChipGroup
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.*


class AddProductFragment : Fragment() {
    private lateinit var mBinding: FragmentAddProductBinding
    private var mActivity: HomeActivity? = null
    private var mIsEditMode: Boolean = false
    private var mProduct: Product? = null

    private var mPhotoUri: Uri? = null
    private val RC_GALLERY = 84
    private var isUploading = false
    private var urlDownload = ""

    private var mUnitType: String = ""


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = FragmentAddProductBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    //indicador del chip group
    //comportamiento del botón de selección de unidad de medida
    //Dependiendo de la opción seleccionada (pieza, peso o litro)
    //se establece la unidad de medida del producto en cuestión
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mBinding.toggleUnit.setOnCheckedStateChangeListener{ group: ChipGroup, checkedIds: MutableList<Int> ->

            when(checkedIds[0]){
                R.id.btnUnitPiece -> mUnitType = "Pz"
                R.id.btnUnitWeight -> mUnitType = "Kg"
                R.id.btnUnitLiter -> mUnitType = "L"
            }
            println(mUnitType)
        }

        if (arguments != null){
            mIsEditMode = true
            val id = arguments?.getString("id")
            if (id != null){
                getProductById(id)
            }
        }else{
            mIsEditMode = false
            mProduct = Product()
        }
        mActivity = activity as? HomeActivity
        setupSaveButton()
    }

    //consultar producto por su ID de la tabla productos de firebase
    private fun getProductById(id: String){
        val products = FirebaseFirestore.getInstance().collection("productos")
        val doc = products.document(id)
        doc.get().addOnSuccessListener { document ->
            if (document != null){
                val product = Product()
                product.id = document.id
                product.nameProduct = document.data?.get("nameProduct") as String
                product.barcode = document.data?.get("barcode") as String
                product.description = document.data?.get("description") as String
                product.unitType = document.data?.get("unitType") as String
                product.photoUrl = document.data?.get("photoUrl") as String
                product.quantity = (document.data?.getValue("quantity").toString()).toDouble()
                product.minimumQuantity = (document.data?.getValue("minimumQuantity").toString()).toDouble()
                mProduct = product
                setUI(mProduct!!)
                println(product)
            }
        }.addOnFailureListener {
            val dialog = Dialogs()
            dialog.alertError("No se pudo recuperar el documento solicitado", mActivity!!)
        }
        println("producto: ${mProduct}")
    }

    private fun setupSaveButton(){
        mBinding.btnAddSaveProduct.setOnClickListener {
            println("Prueba")
            if (mProduct != null){
                with(mProduct!!){
                    nameProduct = mBinding.editAddProductName.text.toString().trim()
                    barcode = mBinding.editAddBarcode.text.toString().trim()
                    description = mBinding.editAddDescription.text.toString().trim()
                    quantity = (mBinding.editAddQuantity.text.toString().trim()).toDouble()
                    minimumQuantity = (mBinding.editAddMinQuantity.text.toString().trim()).toDouble()
                    unitType = mUnitType
                    photoUrl = mProduct!!.photoUrl
                }
                if (mIsEditMode) { updateProduct(mProduct!!) } else { createProduct(mProduct!!) }
            }
        }
        mBinding.imgProductAdd.setOnClickListener {
            openGallery()
        }
    }

    //Guardar/crear producto nuevo en la firebase tabla productos
    private fun createProduct(product: Product) {
        val dbCollection = FirebaseFirestore.getInstance().collection("productos")
        val document = hashMapOf(
            "nameProduct" to product.nameProduct,
            "barcode" to product.barcode,
            "description" to product.description,
            "quantity" to product.quantity,
            "minimumQuantity" to product.minimumQuantity,
            "unitType" to product.unitType,
            "photoUrl" to product.photoUrl
        )
        dbCollection.add(document).addOnSuccessListener {
            Toast.makeText(context,"Producto creado con exito", Toast.LENGTH_SHORT).show()
            mActivity?.onBackPressedDispatcher?.onBackPressed()
        }
    }
    //Actualizar la informacion modificada de un producto
    private fun updateProduct(product: Product){
        val db = FirebaseFirestore.getInstance()
        val docRef = db.collection("productos").document(product.id)

        val updates = hashMapOf(
            "nameProduct" to product.nameProduct,
            "barcode" to product.barcode,
            "description" to product.description,
            "quantity" to product.quantity,
            "minimumQuantity" to product.minimumQuantity,
            "unitType" to product.unitType,
            "photoUrl" to product.photoUrl
        )

        db.runTransaction { transaction ->
            transaction.update(docRef, updates as Map<String, Any>)
        }.addOnSuccessListener {
            mActivity?.onBackPressedDispatcher?.onBackPressed()
        }
    }
    //toma un objeto "Product" y actualiza los valores de los campos en la vista con los valores del objeto "Product"
    private fun setUI(product: Product){
        with(mBinding){
            editAddProductName.setText(product.nameProduct)
            editAddBarcode.setText(product.barcode)
            editAddDescription.setText(product.description)
            editAddQuantity.setText(product.quantity.toString())
            editAddMinQuantity.setText(product.minimumQuantity.toString())
            when(product.unitType){
                "Pz" -> toggleUnit.check(R.id.btnUnitPiece)
                "Kg" -> toggleUnit.check(R.id.btnUnitWeight)
                "L" -> toggleUnit.check(R.id.btnUnitLiter)
            }
            if (product.photoUrl != ""){
                Glide.with(requireActivity())
                    .load(product.photoUrl)
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .centerCrop()
                    .into(imgProductAdd)
            }
        }
    }
    //función que se encarga de subir una imagen al almacenamiento en la nube
    //de Firebase y actualizar la URL de la imagen en el objeto mProduct.
    private fun uploadImage(uri: Uri){
        val formater = SimpleDateFormat("yyyy_MM_dd_mm_ss", Locale.getDefault())
        val now = Date()
        val fileName = formater.format(now)

        val storage = FirebaseStorage.getInstance().getReference("imagenes/usuarios/${fileName}")
        val uploadTask = storage.putFile(uri)
        isUploading = true
        mBinding.btnAddSaveProduct.isEnabled = false
        val urlTask = uploadTask.continueWithTask{ task ->
            if (!task.isSuccessful){
                task.exception?.let {
                    throw it
                }
            }
            storage.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful){
                urlDownload = task.result.toString()
                mProduct!!.photoUrl = urlDownload
            }else{
                Toast.makeText(context, "La imagen no se pudo subir", Toast.LENGTH_SHORT).show()
            }
            mBinding.btnAddSaveProduct.isEnabled = true
        }
    }
    //Abrir la galeria para subir foto
    private fun openGallery(){
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, RC_GALLERY)
    }
    //Espera a que suba la imagen para evitar errores
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK){
            if (requestCode == RC_GALLERY){
                mPhotoUri = data?.data
                mBinding.imgProductAdd.setImageURI(mPhotoUri)
                uploadImage(mPhotoUri!!)
            }
        }
    }
    //desaparecer nuestro fab/boton +
    override fun onDestroy() {
        mActivity?.toggleFab(true)
        super.onDestroy()
    }


}