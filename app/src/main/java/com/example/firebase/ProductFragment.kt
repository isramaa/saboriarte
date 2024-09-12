package com.example.firebase

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import androidx.recyclerview.widget.GridLayoutManager
import com.example.firebase.databinding.FragmentProductBinding
import com.google.android.material.snackbar.Snackbar

class ProductFragment : Fragment(), ProductOnClick {
    private lateinit var mAdapter: ProductAdapter
    private lateinit var mGridLayout: GridLayoutManager
    private lateinit var mBinding: FragmentProductBinding
    private var mActivity: HomeActivity? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        mBinding = FragmentProductBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mActivity = activity as? HomeActivity
        setupRecyleView()
    }
    //configuracion de nuestro recyclerView que son nuestros cuadros de elementos
    private fun setupRecyleView(){
        mAdapter = ProductAdapter(mutableListOf(), this)
        mGridLayout = GridLayoutManager(this.context, 3)
        getProducts()
        mBinding.recyclerProduct.apply {
            setHasFixedSize(true)
            layoutManager = mGridLayout
            adapter = mAdapter
        }
    }

    private fun getProducts(){
        mAdapter.setProducts()
    }

    override fun onProductClicked(product: Product) {
        val bundle = Bundle()
        bundle.putString("id", product.id)
        //Snackbar.make(this.requireView(), product.id, Snackbar.LENGTH_SHORT).show()
        mActivity?.launchAddProductFragment(bundle)
    }


}