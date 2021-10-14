package calendar.event.reminder

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo

class ConnectionDetector(private val _context: Context) {
    val isConnectingToInternet: Boolean
        get() {
            val connectivity =
                _context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            if (connectivity != null) {
                val info = connectivity.allNetworkInfo
                for (networkInfo in info) if (networkInfo.state == NetworkInfo.State.CONNECTED) return true
            }
            return false
        }

}