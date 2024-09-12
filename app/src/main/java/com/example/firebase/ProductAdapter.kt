package com.example.firebase

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.firebase.databinding.ItemProductBinding
import com.google.firebase.firestore.FirebaseFirestore

class ProductAdapter(private var products: MutableList<Product>, private var listener: ProductOnClick): RecyclerView.Adapter<ProductAdapter.ViewHolder>(){
    private lateinit var mContext: Context

    inner class ViewHolder(view: View): RecyclerView.ViewHolder(view){
        val binding = ItemProductBinding.bind(view)

        fun setProductListener(product: Product){
            binding.root.setOnClickListener{ listener.onProductClicked(product)}
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        mContext = parent.context
        val view = LayoutInflater.from(mContext).inflate(R.layout.item_product, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return this.products.size
    }

    //asignar valores a las vistas del elemento de la lista en función de la posición del elemento en la lista
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val product = products.get(position)
        with(holder){
            setProductListener(product)
            binding.tvProductName.text = product.nameProduct
            binding.tvQuantity.text = "${product.quantity}${product.unitType}"
            Glide.with(mContext)
                .load(product.photoUrl)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .centerCrop()
                .into(binding.imgProductPhoto)
        }
    }

    //obtener y mostrar los productos almacenados en una base de datos Firestore de Firebase
    fun setProducts(){
        val db = FirebaseFirestore.getInstance()
        db.collection("productos")
            .addSnapshotListener{
                snap, e ->
                if (snap?.documents != null){
                    val changes = snap.getDocumentChanges()
                    for (doc in changes){
                        var productToAdd = Product()
                        productToAdd.id = doc.document.id
                        productToAdd.nameProduct = doc.document.data["nameProduct"] as String
                        productToAdd.barcode = doc.document.data["barcode"] as String
                        productToAdd.description = doc.document.data["description"] as String
                        productToAdd.quantity = (doc.document.data.getValue("quantity").toString()).toDouble()
                        productToAdd.minimumQuantity = (doc.document.data.getValue("minimumQuantity").toString()).toDouble()
                        productToAdd.photoUrl = doc.document.data["photoUrl"] as String
                        productToAdd.unitType = doc.document.data["unitType"] as String
                        if (this.products.contains(productToAdd)){
                            val index = this.products.indexOf(productToAdd)
                            this.products.set(index = index, productToAdd)
                        }else{
                            this.products.add(productToAdd)
                        }
                        notifyDataSetChanged()
                    }
                }

            }
    }
}