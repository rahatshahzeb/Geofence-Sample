package com.android.task.service

import android.app.job.JobParameters
import android.content.IntentFilter
import android.content.Intent
import android.app.job.JobService
import android.content.Context
import android.net.wifi.SupplicantState
import android.net.wifi.WifiManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.android.task.preference.SharedPreferenceManager
import com.android.task.receiver.ConnectivityReceiver
import com.android.task.util.SSConstants
import dagger.android.AndroidInjection
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class NetworkSchedulerService : JobService(), ConnectivityReceiver.ConnectivityReceiverListener {

    private val TAG = NetworkSchedulerService::class.java.simpleName

    @Inject
    lateinit var sharedPreferenceManager: SharedPreferenceManager

    private var mConnectivityReceiver: ConnectivityReceiver? = null

    override fun onCreate() {
        AndroidInjection.inject(this)
        super.onCreate()
        Log.i(TAG, "Service created")
        mConnectivityReceiver = ConnectivityReceiver(this)
    }

    /**
     * When the app's NetworkConnectionActivity is created, it starts this service. This is so that the
     * activity and this service can communicate back and forth. See "setUiCallback()"
     */
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.i(TAG, "onStartCommand")
        return START_NOT_STICKY
    }

    override fun onStartJob(params: JobParameters): Boolean {
        Log.i(TAG, "onStartJob" + mConnectivityReceiver!!)
        registerReceiver(mConnectivityReceiver, IntentFilter(SSConstants.CONNECTIVITY_ACTION))
        return true
    }

    override fun onStopJob(params: JobParameters): Boolean {
        Log.i(TAG, "onStopJob")
        unregisterReceiver(mConnectivityReceiver)
        return true
    }

    override fun onNetworkConnectionChanged(isConnected: Boolean) {
        val wifiSSID = getWifiSSID().replace("SSID: ","").replace("\"","")
        sharedPreferenceManager.connectedWifi = wifiSSID
    }

    fun getWifiSSID(): String  {
        var ssid = ""
        val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val wifiInfo = wifiManager.connectionInfo

        if (wifiInfo.supplicantState == SupplicantState.COMPLETED) {
            ssid = wifiInfo.ssid
        }
        return ssid
    }
}