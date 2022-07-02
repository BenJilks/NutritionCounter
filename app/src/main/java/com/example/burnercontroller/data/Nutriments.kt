package com.example.burnercontroller.data

import android.os.Bundle
import org.json.JSONObject

data class Nutriments(
    val energy: Double?,
    val fat: Double?,
    val sugars: Double?,
    val salt: Double?,
)

fun readNutriments(data: OpenFoodFactsEntry): Nutriments {
    val energy = data.energyKcal ?: data.energy
    val fat = data.fat
    val sugars = data.sugars
    val salt = data.salt
    return Nutriments(energy, fat, sugars, salt)
}

fun unBundleNutriments(bundle: Bundle?): Nutriments {
    if (bundle == null) {
        return Nutriments(null, null, null, null)
    }

    val energy = unbundleValue(bundle, "energy")
    val fat = unbundleValue(bundle, "fat")
    val sugars = unbundleValue(bundle, "sugars")
    val salt = unbundleValue(bundle, "salt")
    return Nutriments(energy, fat, sugars, salt)
}

fun Nutriments.bundle(): Bundle {
    return Bundle().apply {
        bundleValue(this, "energy", energy)
        bundleValue(this, "fat", fat)
        bundleValue(this, "sugars", sugars)
        bundleValue(this, "salt", salt)
    }
}

private fun bundleValue(bundle: Bundle, name: String, value: Double?) {
    bundle.putBoolean("has_$name", value != null)
    if (value != null)
        bundle.putDouble(name, value)
}

private fun unbundleValue(bundle: Bundle, name: String): Double? {
    return if (bundle.getBoolean("has_$name"))
        bundle.getDouble(name)
    else
        null
}

/*

private fun readEnergyValue(data: JSONObject): Double? {
    if (!data.has("energy_100g"))
        return null

    val energy = data.getDouble("energy_100g")
    if (!data.has("energy_unit"))
        return energy

    return when (data.getString("energy_unit").lowercase()) {
        "j" -> energy / 4184.0
        "kj" -> energy / 4.184
        "kcal" -> energy
        else -> null
    }
}

private fun readWeightValue(data: JSONObject, name: String): Double? {
    if (!data.has("${ name }_100g"))
        return null

    val weight = data.getDouble("${ name }_100g")
    if (!data.has("${ name }_unit"))
        return weight

    return when (data.getString("${ name }_unit").lowercase()) {
        "mg" -> weight / 1000.0
        "g" -> weight
        else -> weight
    }
}

*/
