package id.casan.monitor.notification

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import id.casan.monitor.BuildConfig

class CasanFirebaseMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        if (BuildConfig.DEBUG) Log.d(TAG, "FCM token refreshed")
    }

    override fun onMessageReceived(message: RemoteMessage) {
        if (BuildConfig.DEBUG) Log.d(TAG, "FCM message received")
    }

    private companion object {
        const val TAG = "CasanMessaging"
    }
}
