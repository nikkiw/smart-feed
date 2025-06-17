package com.core.observers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
/**
 * Repository for monitoring and querying network connectivity status.
 */
interface ConnectivityRepository {
    /**
     * A [StateFlow] emitting the current network connectivity state.
     * `true` if the device is currently connected to the internet, `false` otherwise.
     */
    val isConnected: StateFlow<Boolean>

    /**
     * Returns the latest known internet availability state.
     *
     * @return `true` if the device is currently connected to the internet, `false` otherwise.
     */
    fun isInternetAvailable(): Boolean
}

/**
 * Default implementation of [ConnectivityRepository] that observes
 * the application lifecycle to register and unregister network callbacks
 * or broadcast receivers based on API level.
 *
 * @constructor
 * Creates an instance of [ConnectivityRepositoryImpl].
 * @param context Application [Context] used to access connectivity services and register receivers.
 */
class ConnectivityRepositoryImpl @Inject constructor(
    private val context: Context
) : LifecycleObserver, ConnectivityRepository {

    // Backing StateFlow for network connectivity status
    private val _isConnected = MutableStateFlow(false)
    override val isConnected: StateFlow<Boolean> = _isConnected

    override fun isInternetAvailable(): Boolean = _isConnected.value

    // BroadcastReceiver for API levels 21–22
    private val networkReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            updateConnectivityStatus()
        }
    }

    init {
        // Initialize connectivity status on creation
        updateConnectivityStatus()
    }

    /**
     * Checks the current network state and updates [_isConnected].
     *
     * For API 23+ uses [ConnectivityManager.getNetworkCapabilities],
     * for older versions uses [ConnectivityManager.getActiveNetworkInfo].
     */
    private fun updateConnectivityStatus() {
        Log.d(TAG, "updateConnectivityStatus start")
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val activeNetwork = connectivityManager.activeNetwork
            val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
            _isConnected.value =
                capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        } else {
            val networkInfo = connectivityManager.activeNetworkInfo
            _isConnected.value = networkInfo?.isConnected == true
        }
        Log.d(TAG, "updateConnectivityStatus end ${_isConnected.value}")
    }

    /**
     * Called when the application moves to the foreground.
     * Registers network callbacks or broadcast receivers depending on API level.
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onStart() {
        Log.d(TAG, "onStart start")
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            connectivityManager.registerDefaultNetworkCallback(networkCallback)
        } else {
            val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
            context.registerReceiver(networkReceiver, filter)
        }
    }

    /**
     * Called when the application moves to the background.
     * Unregisters network callbacks or broadcast receivers depending on API level.
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onStop() {
        Log.d(TAG, "onStop start")
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            connectivityManager.unregisterNetworkCallback(networkCallback)
        } else {
            context.unregisterReceiver(networkReceiver)
        }
    }

    // NetworkCallback for API 24+
    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            Log.d(TAG, "networkCallback onAvailable")
            _isConnected.update { true }
        }

        override fun onLost(network: Network) {
            super.onLost(network)
            Log.d(TAG, "networkCallback onLost")
            _isConnected.update { false }
        }
    }

    companion object {
        private const val TAG = "ConnectivityRepository"
    }
}
