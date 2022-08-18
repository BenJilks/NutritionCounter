/*
 * Copyright (c) 2022, Ben Jilks <benjyjilks@gmail.com>
 *
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.example.burnercontroller

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.burnercontroller.data.AppDatabase
import com.example.burnercontroller.data.Product
import com.example.burnercontroller.data.unixTimeStampToDayString
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.concurrent.Executors

class ProductListPager(private val database: AppDatabase) : Fragment() {

    private var days = arrayListOf(Instant.now().truncatedTo(ChronoUnit.DAYS).epochSecond)
    private lateinit var todaysProductList: ProductList

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val executor = Executors.newSingleThreadExecutor()
        executor.execute {
            val daysWithEntries = ArrayList(database.products().daysWithEntries())
            val now = Instant.now().truncatedTo(ChronoUnit.DAYS).epochSecond
            if (!daysWithEntries.contains(now))
                daysWithEntries.add(now)

            daysWithEntries.sortDescending()
            days = daysWithEntries
        }

        return inflater.inflate(R.layout.fragment_product_list_pager, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        todaysProductList = ProductList(database, days[0])

        val pager = view.findViewById<ViewPager2>(R.id.product_list_pager)
        val tabLayout = view.findViewById<TabLayout>(R.id.tab_layout)
        pager.adapter = ProductListAdapter(this)

        TabLayoutMediator(tabLayout, pager) { tab, position ->
            tab.text = unixTimeStampToDayString(days[position])
        }.attach()
    }

    fun addProduct(product: Product) {
        todaysProductList.addProduct(product)
    }

    private inner class ProductListAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

        override fun getItemCount(): Int {
            return days.size
        }

        override fun createFragment(position: Int): Fragment {
            if (position == 0)
                return todaysProductList
            return ProductList(database, days[position])
        }

    }

}
