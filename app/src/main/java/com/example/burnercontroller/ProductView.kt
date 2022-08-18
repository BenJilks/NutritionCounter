/*
 * Copyright (c) 2022, Ben Jilks <benjyjilks@gmail.com>
 *
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.example.burnercontroller

import android.content.Context
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.example.burnercontroller.data.Product
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors
import javax.net.ssl.HttpsURLConnection

class ProductView(private val product: Product) : Fragment() {

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_product_view, container, false)
        val title = view.findViewById<TextView>(R.id.title)
        val description = view.findViewById<TextView>(R.id.description)
        title.text = product.name
        description.text = product.nutriments.toString()
        setIcon()

        return view
    }

    private fun setIcon() {
        val hasCache = requireContext().fileList().contains(product.barcode)
        val executor = Executors.newSingleThreadExecutor()
        val handler = IconLoadedHandler(Looper.getMainLooper())

        executor.execute {
            if (hasCache)
                loadIconFromCache(handler)
            else
                requestIconImage(handler)
        }
    }

    private fun loadIconFromCache(handler: Handler) {
        Log.i("APP", "Using cached icon")

        val fileStream = requireContext().openFileInput(product.barcode)
        val imageBytes = fileStream.readBytes()
        fileStream.close()

        handler.sendMessage(Message.obtain().apply {
            data = Bundle().apply { putByteArray("data", imageBytes) }
        })
    }

    private fun requestIconImage(handler: Handler) {
        val url = URL(product.imageUrl)
        val connection = url.openConnection() as HttpURLConnection

        if (connection.responseCode != HttpsURLConnection.HTTP_OK) {
            Log.w("APP", "Unable to load '${product.imageUrl}'")
            return
        }

        val reader = connection.inputStream.buffered()
        val imageBytes = reader.readBytes()
        reader.close()

        handler.sendMessage(Message.obtain().apply {
            data = Bundle().apply { putByteArray("data", imageBytes) }
        })

        val fileStream = requireContext().openFileOutput(product.barcode, Context.MODE_PRIVATE)
        fileStream.write(imageBytes)
        fileStream.close()
    }

    private inner class IconLoadedHandler(looper: Looper) : Handler(looper) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)

            val data = msg.data.getByteArray("data") ?: return
            Log.i("APP", "Loaded icon image")

            val bitmap = BitmapFactory.decodeByteArray(data, 0, data.size)
            val icon = requireView().findViewById<ImageView>(R.id.icon)
            icon.setImageBitmap(bitmap)
        }
    }

}
