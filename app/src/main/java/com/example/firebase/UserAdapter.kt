package com.example.firebase

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.firebase.databinding.ItemUserBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject

class UserAdapter(private var users: MutableList<User>, private var listener: UserOnClick) : RecyclerView.Adapter<UserAdapter.ViewHolder>() {
    private lateinit var mContext: Context

    inner class ViewHolder(view: View): RecyclerView.ViewHolder(view){
        val binding = ItemUserBinding.bind(view)

        fun setUserListener(user: User){
                binding.root.setOnClickListener { listener.onUserClicked(user) }
                //binding.imgIsAdmin.isVisible = user.isAdmin

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        mContext = parent.context
        val view = LayoutInflater.from(mContext).inflate(R.layout.item_user, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return this.users.size
    }
    //asignar valores a las vistas del elemento de la lista en función de la posición del elemento en la lista
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = users.get(position)
        with(holder){
            setUserListener(user)
            binding.tvUserName.text = "${user.name} ${user.lastName}"
            binding.imgIsAdmin.isVisible = user.isAdmin
            Glide.with(mContext)
                .load(user.photoUrl)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop()
                .into(binding.imgUserPhoto)
        }

    }
    //obtener y mostrar los usuarios almacenados en una base de datos de Firebase
    fun setUsers(){
        val db = FirebaseFirestore.getInstance()
        db.collection("usuarios")
            .addSnapshotListener{
                snap,e ->
                if (snap?.documents != null){
                    val changos = snap.getDocumentChanges()
                    for (cambio in changos){
                        var userToAdd = User()
                        userToAdd.name = cambio.document.data["name"] as String
                        userToAdd.lastName = cambio.document.data["lastName"] as String
                        userToAdd.email = cambio.document.data["email"] as String
                        userToAdd.phone = cambio.document.data["phone"] as String
                        userToAdd.photoUrl = cambio.document.data["photoUrl"] as String
                        userToAdd.isAdmin = cambio.document.data["isAdmin"] as Boolean
                        userToAdd.isOwner = cambio.document.data["isOwner"] as Boolean
                        userToAdd.id = cambio.document.id
                        if (this.users.contains(userToAdd)){
                            val index = this.users.indexOf(userToAdd)
                            this.users.set(index = index, userToAdd)
                        }else{
                            this.users.add(userToAdd)
                        }
                        notifyDataSetChanged()
                    }
                }
            }
    }

}