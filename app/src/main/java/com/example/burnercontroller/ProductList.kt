package com.example.burnercontroller

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.findFragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.burnercontroller.data.Product
import com.example.burnercontroller.model.ProductListViewModel

class ProductList : Fragment() {

    private lateinit var viewModel: ProductListViewModel

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_product_list, container, false)
    }

    override fun onStart() {
        super.onStart()
        viewModel = ViewModelProvider(this).get(ProductListViewModel::class.java)
    }

    fun addProduct(product: Product) {
        /*
        parentFragmentManager.beginTransaction().apply {
            add(R.id.product_list, ProductView(product))
            commit()
        }
        */
    }

}
