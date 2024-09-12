package com.example.firebase

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.firebase.databinding.FragmentUserBinding
import com.example.firebase.databinding.ItemUserBinding

class UserFragment : Fragment(), UserOnClick {
    private lateinit var mAdapter: UserAdapter
    private lateinit var mGridLayout: GridLayoutManager
    private lateinit var dao: UserDAO
    private lateinit var mBinding: FragmentUserBinding
    private var mActivity: HomeActivity? = null


    //inflar el diseño de la interfaz de usuario asociado al fragmento.
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        mBinding = FragmentUserBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    //ciclo de vida de un Fragment de Android que se llama después de que la vista se ha creado
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mActivity = activity as? HomeActivity
        setupRecyclerView()
    }
    //funcion que define el ciclo de vida del fragment
    //combinacion de nuestro xml con info de db
    private fun setupRecyclerView() {
        mAdapter = UserAdapter(mutableListOf(), this)
        mGridLayout = GridLayoutManager(this.context, 2)
        getUsers()
        mBinding.recyclerUser.apply {
            setHasFixedSize(true)
            layoutManager = mGridLayout
            adapter = mAdapter
        }
    }

    private fun getUsers() {
        mAdapter.setUsers()
    }

    //se llama cuando el usuario hace clic en un elemento de la
    //lista y se le pasa como argumento el usuario correspondiente al elemento clicado
    override fun onUserClicked(user: User) {
        val bundle: Bundle = Bundle()
        with(bundle){
            putString("id", user.id)
            putString("name", user.name)
            putString("lastName", user.lastName)
            putString("email", user.email)
            putString("phone", user.phone)
            putString("photoUrl", user.photoUrl)
            putBoolean("isAdmin", user.isAdmin)
            putBoolean("isOwner", user.isOwner)
        }
        mActivity?.launchAddUserFragment(bundle)
    }

}





