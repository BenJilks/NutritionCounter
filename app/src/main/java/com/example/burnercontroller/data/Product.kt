package com.example.burnercontroller.data

import android.os.Bundle
import androidx.room.*
import androidx.room.OnConflictStrategy.REPLACE
import java.security.InvalidParameterException

@Entity
data class Product (
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val barcode: String,
    val name: String,
    val imageUrl: String,
    val date: Long,

    @Embedded
    val nutriments: Nutriments,
)

@Dao
interface ProductDao {
    @Query("SELECT * FROM product")
    fun getAll(): List<Product>

    @Insert(onConflict = REPLACE)
    fun insert(vararg products: Product)
}

fun unBundleProduct(bundle: Bundle?): Product {
    if (bundle == null) {
        throw InvalidParameterException("Bundle cannot be null")
    }

    return Product(
        barcode = bundle.getString("barcode", ""),
        name = bundle.getString("name", ""),
        imageUrl = bundle.getString("imageUrl", ""),
        date = bundle.getLong("date"),
        nutriments = unBundleNutriments(bundle.getBundle("nutriments")),
    )
}

fun Product.bundle(): Bundle {
    return Bundle().apply {
        putString("barcode", barcode)
        putString("name", name)
        putString("imageUrl", imageUrl)
        putLong("date", date)
        putBundle("nutriments", nutriments.bundle())
    }
}