package com.core.observers

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
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
class ConnectivityRepositoryImpl
    @Inject
    constructor(
        private val context: Context,
    ) : DefaultLifecycleObserver, ConnectivityRepository {
        // Backing StateFlow for network connectivity status
        private val _isConnected = MutableStateFlow(false)
        override val isConnected: StateFlow<Boolean> = _isConnected
        private var isCallbackRegistered = false

        override fun isInternetAvailable(): Boolean = _isConnected.value

        init {
            // Initialize connectivity status on creation
            updateConnectivityStatus()
        }

        /**
         * Checks the current network state and updates [_isConnected].
         *
         * Uses [ConnectivityManager.getNetworkCapabilities] because the app minSdk is 23.
         */
        private fun updateConnectivityStatus() {
            Log.d(TAG, "updateConnectivityStatus start")
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

            val activeNetwork = connectivityManager.activeNetwork
            val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
            _isConnected.value =
                capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
            Log.d(TAG, "updateConnectivityStatus end ${_isConnected.value}")
        }

        /**
         * Called when the application moves to the foreground.
         * Registers network callbacks while the application is foregrounded.
         */
        override fun onStart(owner: LifecycleOwner) {
            Log.d(TAG, "onStart start")
            if (isCallbackRegistered) return

            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
            isCallbackRegistered = true
        }

        /**
         * Called when the application moves to the background.
         * Unregisters network callbacks while the application is backgrounded.
         */
        override fun onStop(owner: LifecycleOwner) {
            Log.d(TAG, "onStop start")
            if (!isCallbackRegistered) return

            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            connectivityManager.unregisterNetworkCallback(networkCallback)
            isCallbackRegistered = false
        }

        private val networkRequest =
            NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()

        private val networkCallback =
            object : ConnectivityManager.NetworkCallback() {
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
