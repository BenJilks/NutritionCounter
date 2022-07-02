package com.example.burnercontroller.data

import android.os.Binder
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import androidx.room.*
import androidx.room.OnConflictStrategy.REPLACE
import java.io.BufferedReader
import java.io.InputStream
import java.lang.NumberFormatException
import java.net.HttpURLConnection
import java.net.URL
import java.util.zip.GZIPInputStream
import javax.net.ssl.HttpsURLConnection

private const val BATCH_SIZE = 10000

private const val INDEX_BARCODE = 0
private const val INDEX_PRODUCT_NAME = 7
private const val INDEX_IMAGE_URL = 68
private const val INDEX_IMAGE_SMALL_URL = 69

private const val INDEX_ENERGY_KCAL_100G = 75
private const val INDEX_ENERGY_100G = 76
private const val INDEX_FAT_100G = 78
private const val INDEX_SUGARS_100G = 115
private const val INDEX_SALT_100G = 131
private const val MIN_ENTRY_COUNT = INDEX_SALT_100G

@Entity(tableName = "open_food_facts")
data class OpenFoodFactsEntry (
    @PrimaryKey
    val barcode: String,

    val productName: String,
    val imageUrl: String,
    val imageSmallUrl: String,

    val energyKcal: Double?,
    val energy: Double?,
    val fat: Double?,
    val sugars: Double?,
    val salt: Double?,
)

@Dao
interface OpenFoodFactsEntryDao {
    @Query("SELECT * FROM open_food_facts WHERE barcode = :barcode LIMIT 1")
    fun get(barcode: String): OpenFoodFactsEntry?

    @Query("SELECT COUNT(*) FROM open_food_facts")
    fun count(): Int

    @Insert(onConflict = REPLACE)
    fun insert(vararg entry: OpenFoodFactsEntry)
}

private class CountingInputStream(private val input: InputStream) : InputStream() {
    var bytesRead = 0
        private set

    override fun read(): Int {
        bytesRead += 1
        return input.read()
    }
}

fun updateOpenFoodFactsDatabase(database: AppDatabase, updateHandler: Handler) {
    val url = URL("https://static.openfoodfacts.org/data/en.openfoodfacts.org.products.csv.gz")
    val connection = url.openConnection() as HttpURLConnection

    if (connection.responseCode != HttpsURLConnection.HTTP_OK) {
        Log.e("APP", "Error downloading OpenFoodFacts database")
        return
    }

    val length = connection.headerFields["Content-Length"]?.first()?.let {
        try { Integer.parseInt(it) }
        catch(_: NumberFormatException) { null }
    }

    val countingStream = CountingInputStream(connection.inputStream)
    val unzipStream = GZIPInputStream(countingStream)
    val reader = unzipStream.bufferedReader()
    streamCSVData(database, countingStream, reader, length, updateHandler)
}

private fun streamCSVData(database: AppDatabase,
                          countingStream: CountingInputStream,
                          reader: BufferedReader,
                          length: Int?,
                          updateHandler: Handler) {
    val batch = ArrayList<OpenFoodFactsEntry>()

    var startTime = System.currentTimeMillis()
    for (line in reader.lines().skip(1)) {
        val entry = readEntry(line)
        if (entry != null)
            batch.add(entry)

        if (batch.size >= BATCH_SIZE) {
            database.runInTransaction {
                database.foodData().insert(*batch.toTypedArray())
            }
            batch.clear()
        }

        if (System.currentTimeMillis() - startTime >= 1000 && length != null) {
            val progress = countingStream.bytesRead.toDouble() / length * 100.0
            updateHandler.sendMessage(Message.obtain().apply {
                data = Bundle().apply { putDouble("progress", progress) }
            })

            Log.i("APP", "Progress: %.2f%%".format(progress))
            startTime = System.currentTimeMillis()
        }
    }

    database.runInTransaction {
        database.foodData().insert(*batch.toTypedArray())
    }
}

private fun readEntry(line: String): OpenFoodFactsEntry? {
    val entries = line.split("\t")
    if (entries.size < MIN_ENTRY_COUNT)
        return null

    return OpenFoodFactsEntry(
        barcode = entries[INDEX_BARCODE],
        productName = entries[INDEX_PRODUCT_NAME],
        imageUrl = entries[INDEX_IMAGE_URL],
        imageSmallUrl = entries[INDEX_IMAGE_SMALL_URL],

        energyKcal = entries[INDEX_ENERGY_KCAL_100G].toDoubleOrNull(),
        energy = entries[INDEX_ENERGY_100G].toDoubleOrNull(),
        fat = entries[INDEX_FAT_100G].toDoubleOrNull(),
        sugars = entries[INDEX_SUGARS_100G].toDoubleOrNull(),
        salt = entries[INDEX_SALT_100G].toDoubleOrNull(),
    )
}
