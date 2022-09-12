/*
 * Copyright (c) 2022, Ben Jilks <benjyjilks@gmail.com>
 *
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.example.burnercontroller

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.example.burnercontroller.data.AppDatabase
import com.example.burnercontroller.data.Product
import com.example.burnercontroller.data.bundle
import com.example.burnercontroller.data.unBundleProduct
import com.example.burnercontroller.model.ProductListViewModel
import java.util.concurrent.Executors

class ProductList(private var database: AppDatabase,
                  private var unixTimeDay: Long) : Fragment() {

    private lateinit var viewModel: ProductListViewModel
    private var totalCalories: Double = 0.0

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val executor = Executors.newSingleThreadExecutor()
        val handler = AddProductHandler(Looper.getMainLooper())
        executor.execute {
            val products = database.products().onDay(unixTimeDay)
            for (product in products)
                handler.sendMessage(Message.obtain().apply { data = product.bundle() })
        }

        return inflater.inflate(R.layout.fragment_product_list, container, false)
    }

    override fun onStart() {
        super.onStart()
        viewModel = ViewModelProvider(this).get(ProductListViewModel::class.java)
    }

    fun addProduct(product: Product) {
        lifecycleScope.launchWhenResumed {
            parentFragmentManager.beginTransaction().apply {
                add(R.id.product_list, ProductView(product))
                commit()
            }
        }

        val totalCalDisplay = view?.findViewById<TextView>(R.id.total_cal_display)
        totalCalories += product.nutriments?.energy ?: 0.0
        totalCalDisplay?.text = totalCalories?.toString() + " kcal"
    }

    private inner class AddProductHandler(looper: Looper) : Handler(looper) {
        override fun handleMessage(message: Message) {
            super.handleMessage(message)

            val product = unBundleProduct(message.data)
            addProduct(product)
        }
    }

}
