package it.nicolasfarabegoli.crowd

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity

private const val ACCESS_LOCATION_REQUEST = 2

internal fun AppCompatActivity.checkPermissions(run: () -> Unit) {
    val missingPermissions = getMissingPermissions(requiredPermissions)
    if (missingPermissions.isNotEmpty()) {
        requestPermissions(missingPermissions, ACCESS_LOCATION_REQUEST)
    } else {
        run()
    }
}

internal fun Context.getMissingPermissions(requiredPermissions: Array<String>): Array<String> {
    val missingPermissions: MutableList<String> = ArrayList()
    for (requiredPermission in requiredPermissions) {
        if (applicationContext.checkSelfPermission(requiredPermission) != PackageManager.PERMISSION_GRANTED) {
            missingPermissions.add(requiredPermission)
        }
    }
    return missingPermissions.toTypedArray()
}

private val Context.requiredPermissions: Array<String>
    get() {
        val targetSdkVersion = applicationInfo.targetSdkVersion
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && targetSdkVersion >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_ADVERTISE,
            )
        } else if (targetSdkVersion >= Build.VERSION_CODES.Q) {
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
    }
