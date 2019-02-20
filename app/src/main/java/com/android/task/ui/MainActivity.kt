package com.android.task.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.android.task.R
import com.android.task.databinding.ActivityMainBinding
import com.android.task.preference.SharedPreferenceManager
import com.google.android.material.snackbar.Snackbar
import dagger.android.AndroidInjection
import javax.inject.Inject

class MainActivity : AppCompatActivity() {

    private val TAG = MainActivity::class.java.simpleName

    private lateinit var binding: ActivityMainBinding

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var sharedPreferenceManager: SharedPreferenceManager

    private lateinit var mainViewModel: MainViewModel

    private val REQUEST_PERMISSIONS_REQUEST_CODE = 34

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        mainViewModel = ViewModelProviders.of(this, viewModelFactory).get(MainViewModel::class.java)
        binding.viewModel = mainViewModel
        binding.lifecycleOwner = this

        subscribeObserver()
    }

    public override fun onStart() {
        super.onStart()
        restoreData()
        if (!checkPermissions()) {
            requestPermissions()
        } else {
            // Play around with Geofence here
        }
    }

    private fun restoreData() {
        mainViewModel.latitude.data.value = sharedPreferenceManager.latitude
        mainViewModel.longitude.data.value = sharedPreferenceManager.longitude
        mainViewModel.radius.data.value = sharedPreferenceManager.radius
        mainViewModel.wifi.data.value = sharedPreferenceManager.wifi
    }

    /**
     * Return the current state of the permissions needed.
     */
    private fun checkPermissions(): Boolean {
        val permissionState = ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        return permissionState == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions() {
        val shouldProvideRationale = ActivityCompat.shouldShowRequestPermissionRationale(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        )

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.")
            showSnackbar(R.string.permission_rationale, android.R.string.ok,
                View.OnClickListener {
                    // Request permission
                    ActivityCompat.requestPermissions(
                        this@MainActivity,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        REQUEST_PERMISSIONS_REQUEST_CODE
                    )
                })
        } else {
            Log.i(TAG, "Requesting permission")
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            ActivityCompat.requestPermissions(
                this@MainActivity,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_PERMISSIONS_REQUEST_CODE
            )
        }
    }

    /**
     * Shows a [Snackbar] using `text`.
     *
     * @param text The Snackbar text.
     */
    private fun showSnackbar(text: String) {
        val container = findViewById<View>(android.R.id.content)
        if (container != null) {
            Snackbar.make(container, text, Snackbar.LENGTH_LONG).show()
        }
    }

    /**
     * Shows a [Snackbar].
     *
     * @param mainTextStringId The id for the string resource for the Snackbar text.
     * @param actionStringId   The text of the action item.
     * @param listener         The listener associated with the Snackbar action.
     */
    private fun showSnackbar(
        mainTextStringId: Int, actionStringId: Int,
        listener: View.OnClickListener
    ) {
        Snackbar.make(
            findViewById(android.R.id.content),
            getString(mainTextStringId),
            Snackbar.LENGTH_INDEFINITE
        )
            .setAction(getString(actionStringId), listener).show()
    }

    fun subscribeObserver() {
        subscribeLatitudeObserver()
        subscribeLatitudeErrorObserver()
        subscribeLongitudeObserver()
        subscribeLongitudeErrorObserver()
        subscribeRadiusObserver()
        subscribeRadiusErrorObserver()
        subscribeWifiObserver()
        subscribeWifiErrorObserver()
        subscribeAddRemoveObserver()
    }

    fun subscribeLatitudeObserver() {
        mainViewModel.latitude.data.observe(this, Observer {
            sharedPreferenceManager.latitude = it
        })
    }

    fun subscribeLatitudeErrorObserver() {
        mainViewModel.latitude.dataError.observe(this, Observer {
            binding.tilLatitude.error = it
        })
    }

    fun subscribeLongitudeObserver() {
        mainViewModel.longitude.data.observe(this, Observer {
            sharedPreferenceManager.longitude = it
        })
    }

    fun subscribeLongitudeErrorObserver() {
        mainViewModel.longitude.dataError.observe(this, Observer {
            binding.tilLongitude.error = it
        })
    }

    fun subscribeRadiusObserver() {
        mainViewModel.radius.data.observe(this, Observer {
            sharedPreferenceManager.radius = it
        })
    }

    fun subscribeRadiusErrorObserver() {
        mainViewModel.radius.dataError.observe(this, Observer {
            binding.tilRadius.error = it
        })
    }

    fun subscribeWifiObserver() {
        mainViewModel.wifi.data.observe(this, Observer {
            sharedPreferenceManager.wifi = it
        })
    }

    fun subscribeWifiErrorObserver() {
        mainViewModel.wifi.dataError.observe(this, Observer {
            binding.tilWifi.error = it
        })
    }

    fun subscribeAddRemoveObserver() {
        mainViewModel.isGeofenceAdded.observe(this, Observer {

        })
    }
}
