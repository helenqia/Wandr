package hu.ait.wandr.ui.utils

import android.content.Context
import android.location.Geocoder
import java.util.Locale

fun getLocationNameFromCoordinates(context: Context, latitude: Double, longitude: Double): String {
    return try {
        val geocoder = Geocoder(context, Locale.getDefault())
        val addresses = geocoder.getFromLocation(latitude, longitude, 1)
        if (!addresses.isNullOrEmpty()) {
            addresses[0].getAddressLine(0) ?: "Unknown Location"
        } else {
            "Unknown Location"
        }
    } catch (e: Exception) {
        "Unknown Location"
    }
}