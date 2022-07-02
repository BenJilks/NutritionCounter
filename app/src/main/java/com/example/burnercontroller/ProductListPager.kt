package com.example.burnercontroller

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Adapter
import android.widget.FrameLayout
import android.widget.TextView
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class ProductListPager : Fragment() {

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_product_list_pager, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val pager = view.findViewById<ViewPager2>(R.id.product_list_pager)
        val tabLayout = view.findViewById<TabLayout>(R.id.tab_layout)
        pager.adapter = ProductListAdapter(this)

        TabLayoutMediator(tabLayout, pager) { tab, position ->
            tab.text = "Page $position"
        }.attach()
    }

    private inner class ProductListAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

        override fun getItemCount(): Int = 100

        override fun createFragment(position: Int): Fragment {
            return TestFragment(position)
        }

    }

}

class TestFragment(private val position: Int) : Fragment() {

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        val content = TextView(requireContext())
        content.text = "Page $position"

        Log.i("APP", "Created view for page $position")
        return content
    }

}
