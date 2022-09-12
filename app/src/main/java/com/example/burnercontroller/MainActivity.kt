/*
 * Copyright (c) 2022, Ben Jilks <benjyjilks@gmail.com>
 *
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.example.burnercontroller

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.*
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import androidx.core.content.ContextCompat
import androidx.room.Room
import com.example.burnercontroller.data.*
import com.google.zxing.integration.android.IntentIntegrator
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private var database: AppDatabase? = null
    private var isUpdatingDatabase = false

    private lateinit var productList: ProductListPager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val updateDatabaseButton = findViewById<Button>(R.id.update_database_button)
        val scanButton = findViewById<Button>(R.id.scan_button)
        updateDatabaseButton.setOnClickListener { updateDatabaseButtonClick() }
        scanButton.setOnClickListener { scanButtonClick() }
    }

    override fun onStart() {
        super.onStart()

        if (database == null) {
            database = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "db")
                .enableMultiInstanceInvalidation()
                .fallbackToDestructiveMigration()
                .build()

            productList = ProductListPager(database!!)
            supportFragmentManager.beginTransaction().apply {
                add(R.id.product_list_view, productList)
                commitNow()
            }
        }

        if (!isUpdatingDatabase)
            checkOpenFoodDatabaseLoaded()
    }

    private fun scanButtonClick() {
        val cameraPermissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        if (cameraPermissionCheck != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.CAMERA), 0)
        }

        val integrator = IntentIntegrator(this)
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES)
        integrator.initiateScan()
    }

    private fun updateDatabaseButtonClick() {
        if (isUpdatingDatabase)
            return

        isUpdatingDatabase = true
        val executor = Executors.newSingleThreadExecutor()
        val progress = DatabaseUpdateProgress()

        supportFragmentManager.beginTransaction().apply {
            add(R.id.controls, progress)
            commitNow()
        }

        val updateHandler = progress.updateHandler(Looper.getMainLooper())
        executor.execute {
            if (database != null) {
                updateOpenFoodFactsDatabase(database as AppDatabase, updateHandler)
                checkOpenFoodDatabaseLoaded()
            }

            isUpdatingDatabase = false
            supportFragmentManager.beginTransaction().apply {
                remove(progress)
            }
        }
    }

    private fun checkOpenFoodDatabaseLoaded() {
        val executor = Executors.newSingleThreadExecutor()
        val doneHandler = OpenFoodDatabaseUpdatedHandler(Looper.getMainLooper())

        executor.execute {
            val count = database?.foodData()?.count() ?: 0
            val loaded = (count > 0)

            doneHandler.sendMessage(Message.obtain().apply {
                data = Bundle().apply {
                    putBoolean("loaded", loaded)
                }
            })
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        val result = IntentIntegrator.parseActivityResult(resultCode, data)
        if (result == null) {
            Log.w("APP", "No barcode found in scan")
            return
        }

        val barcode = result.contents
        Log.i("APP", "Got barcode '$barcode'")

        val executor = Executors.newSingleThreadExecutor()
        val handler = FoodDataHandler(Looper.getMainLooper())
        executor.execute {
            sendFoodDataRequest(barcode, handler)
        }
    }

    private fun sendFoodDataRequest(barcode: String, handler: Handler) {
        Log.i("APP", "Sending request for food data")

        val entry = database?.foodData()?.get(barcode)
        if (entry == null) {
            Log.e("APP", "Unable to find barcode '${ barcode }'")
            return
        }

        val product = Product(
            barcode = barcode,
            name = entry.productName,
            imageUrl = entry.imageSmallUrl.ifEmpty { entry.imageUrl },
            date = Instant.now().truncatedTo(ChronoUnit.DAYS).epochSecond,
            nutriments = readNutriments(entry),
        )

        val message = Message.obtain()
        message.data = product.bundle()
        handler.sendMessage(message)

        database?.products()?.insert(product)
    }

    private inner class FoodDataHandler(looper: Looper) : Handler(looper) {
        override fun handleMessage(message: Message) {
            super.handleMessage(message)

            val product = unBundleProduct(message.data)
            productList.addProduct(product)
        }
    }

    private inner class OpenFoodDatabaseUpdatedHandler(looper: Looper) : Handler(looper) {
        override fun handleMessage(message: Message) {
            super.handleMessage(message)

            val scanButton = findViewById<Button>(R.id.scan_button)
            val loaded = message.data.getBoolean("loaded", false)
            scanButton.isEnabled = loaded
        }
    }

}
