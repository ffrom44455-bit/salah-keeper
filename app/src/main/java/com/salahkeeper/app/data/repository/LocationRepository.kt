package com.salahkeeper.app.data.repository

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.os.Build
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

data class LocationResult(
    val latitude: Double,
    val longitude: Double,
    val cityName: String,
    val isSuccess: Boolean
)

@Singleton
class LocationRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val fusedClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): LocationResult = withContext(Dispatchers.IO) {
        try {
            val location = suspendCancellableCoroutine<Location?> { cont ->
                val cts = CancellationTokenSource()
                fusedClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, cts.token)
                    .addOnSuccessListener { loc ->
                        if (loc != null) {
                            cont.resume(loc)
                        } else {
                            // Fallback to last location
                            fusedClient.lastLocation.addOnSuccessListener { lastLoc ->
                                cont.resume(lastLoc)
                            }.addOnFailureListener {
                                cont.resume(null)
                            }
                        }
                    }
                    .addOnFailureListener {
                        cont.resume(null)
                    }

                cont.invokeOnCancellation {
                    cts.cancel()
                }
            }

            if (location != null) {
                val cityName = resolveCityName(location.latitude, location.longitude)
                LocationResult(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    cityName = cityName,
                    isSuccess = true
                )
            } else {
                LocationResult(24.8607, 67.0011, "Karachi (Default)", false)
            }
        } catch (e: Exception) {
            LocationResult(24.8607, 67.0011, "Karachi (Default)", false)
        }
    }

    private fun resolveCityName(lat: Double, lon: Double): String {
        return try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocation(lat, lon, 1)
            if (!addresses.isNullOrEmpty()) {
                val addr = addresses[0]
                val locality = addr.locality ?: addr.subAdminArea ?: addr.adminArea
                val country = addr.countryName
                if (!locality.isNullOrBlank() && !country.isNullOrBlank()) {
                    "$locality, $country"
                } else locality ?: country ?: "Detected Location"
            } else {
                "Detected Location"
            }
        } catch (e: Exception) {
            "Detected Location"
        }
    }
}
