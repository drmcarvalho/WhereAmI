package com.example.whereami

import android.Manifest
import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.CancellationSignal
import androidx.annotation.RequiresPermission
import androidx.core.location.LocationManagerCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

class MyLocation(private val context: Context) {
    @RequiresPermission(anyOf = [Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION])
    suspend fun getFullAddress(): String? {
        val manager = context.getSystemService(LocationManager::class.java)
        val provider = if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) LocationManager.GPS_PROVIDER else LocationManager.NETWORK_PROVIDER
        val location = suspendCancellableCoroutine<Location?> { cont ->
            val signal = CancellationSignal()
            cont.invokeOnCancellation { signal.cancel() }
            LocationManagerCompat.getCurrentLocation(
                manager,
                provider,
                signal,
                context.mainExecutor
            ) { cont.resume(it) }
        } ?: return null
        @Suppress("DEPRECATION")
        val address = withContext(Dispatchers.IO) {
            Geocoder(context).getFromLocation(location.latitude, location.longitude, 1)
        }?.firstOrNull() ?: return null
        return with(address) {
            listOfNotNull(thoroughfare, subThoroughfare, locality ?: subAdminArea, adminArea, countryName)
        }.joinToString(", ").ifEmpty { null }
    }
}