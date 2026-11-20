package id.casan.monitor.core.network

import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

fun ConnectivityManager.validatedConnectivity() = callbackFlow {
    fun NetworkCapabilities?.validated() =
        this?.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) == true

    trySend(getNetworkCapabilities(activeNetwork).validated())
    val callback = object : ConnectivityManager.NetworkCallback() {
        private var current: Network? = null

        override fun onAvailable(network: Network) {
            current = network
        }

        override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {
            if (current == network) trySend(capabilities.validated())
        }

        override fun onLost(network: Network) {
            if (current == network) {
                current = null
                trySend(false)
            }
        }
    }
    registerDefaultNetworkCallback(callback)
    awaitClose { unregisterNetworkCallback(callback) }
}.distinctUntilChanged()
