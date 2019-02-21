package com.android.task.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.android.task.BuildConfig
import com.android.task.R
import com.android.task.databinding.ActivityMainBinding
import com.android.task.geofence.GeofenceBroadcastReceiver
import com.android.task.geofence.GeofenceErrorMessages
import com.android.task.preference.SharedPreferenceManager
import com.android.task.service.NetworkSchedulerService
import com.android.task.util.SSConstants.GEOFENCE_EXPIRATION_IN_MILLISECONDS
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import dagger.android.AndroidInjection
import java.util.ArrayList
import javax.inject.Inject

class MainActivity : AppCompatActivity(), OnCompleteListener<Void> {

    private val TAG = MainActivity::class.java.simpleName

    private val GEOFENCE_KEY = "ZONE"

    private lateinit var binding: ActivityMainBinding

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var sharedPreferenceManager: SharedPreferenceManager

    private lateinit var mainViewModel: MainViewModel

    private val REQUEST_PERMISSIONS_REQUEST_CODE = 34

    /**
     * The list of geofences.
     */
    private var mGeofenceList = ArrayList<Geofence>()

    private enum class PendingGeofenceTask {
        ADD, REMOVE, NONE
    }

    /**
     * Provides access to the Geofencing API.
     */
    private lateinit var mGeofencingClient: GeofencingClient

    /**
     * Used when requesting to add or remove geofences.
     */
    var mGeofencePendingIntent: PendingIntent? = null

    private var mPendingGeofenceTask = PendingGeofenceTask.NONE

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        mainViewModel = ViewModelProviders.of(this, viewModelFactory).get(MainViewModel::class.java)
        binding.viewModel = mainViewModel
        binding.lifecycleOwner = this

        setButtonsEnabledState()

        //Throws NPE
//        populateGeofenceList()

        mGeofencingClient = LocationServices.getGeofencingClient(this)

        subscribeObserver()
        scheduleJob()
    }

    public override fun onStart() {
        super.onStart()
        restoreData()
        if (!checkPermissions()) {
            requestPermissions()
        } else {
            performPendingGeofenceTask()
        }

        val startServiceIntent = Intent(this, NetworkSchedulerService::class.java)
        startService(startServiceIntent)
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private fun scheduleJob() {
        val wifiJob: JobInfo = JobInfo.Builder(0, ComponentName(this, NetworkSchedulerService::class.java))
                .setRequiresCharging(true)
                .setMinimumLatency(1000)
                .setOverrideDeadline(2000)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPersisted(true)
                .build()

        val jobScheduler: JobScheduler = getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        jobScheduler.schedule(wifiJob)
    }

    override fun onStop() {
        // A service can be "started" and/or "bound". In this case, it's "started" by this Activity
        // and "bound" to the JobScheduler (also called "Scheduled" by the JobScheduler). This call
        // to stopService() won't prevent scheduled jobs to be processed. However, failing
        // to call stopService() would keep it alive indefinitely.
        stopService(Intent(this, NetworkSchedulerService::class.java))
        super.onStop()
    }

    /**
     * Builds and returns a GeofencingRequest. Specifies the list of geofences to be monitored.
     * Also specifies how the geofence notifications are initially triggered.
     */
    private fun getGeofencingRequest(): GeofencingRequest {
        val builder = GeofencingRequest.Builder()

        // The INITIAL_TRIGGER_ENTER flag indicates that geofencing service should trigger a
        // GEOFENCE_TRANSITION_ENTER notification when the geofence is added and if the device
        // is already inside that geofence.
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)

        // Add the geofences to be monitored by geofencing service.
        builder.addGeofences(mGeofenceList)

        // Return a GeofencingRequest.
        return builder.build()
    }

    /**
     * Adds geofences, which sets alerts to be notified when the device enters or exits one of the
     * specified geofences. Handles the success or failure results returned by addGeofences().
     */
    fun addGeofencesHandler() {
        if (!checkPermissions()) {
            mPendingGeofenceTask = PendingGeofenceTask.ADD
            requestPermissions()
            return
        }
        addGeofences()
    }

    /**
     * Adds geofences. This method should be called after the user has granted the location
     * permission.
     */
    @SuppressLint("MissingPermission")
    private fun addGeofences() {
        if (!checkPermissions()) {
            showSnackbar(getString(R.string.insufficient_permissions))
            return
        }

        mGeofencingClient.addGeofences(getGeofencingRequest(), getGeofencePendingIntent())
            .addOnCompleteListener(this)
    }

    /**
     * Removes geofences, which stops further notifications when the device enters or exits
     * previously registered geofences.
     */
    fun removeGeofencesHandler() {
        if (!checkPermissions()) {
            mPendingGeofenceTask = PendingGeofenceTask.REMOVE
            requestPermissions()
            return
        }
        removeGeofences()
    }

    /**
     * Removes geofences. This method should be called after the user has granted the location
     * permission.
     */
    private fun removeGeofences() {
        if (!checkPermissions()) {
            showSnackbar(getString(R.string.insufficient_permissions))
            return
        }

        mGeofencingClient.removeGeofences(getGeofencePendingIntent()).addOnCompleteListener(this)
    }

    /**
     * Runs when the result of calling {@link #addGeofences()} and/or {@link #removeGeofences()}
     * is available.
     * @param task the resulting Task, containing either a result or error.
     */
    override fun onComplete(task: Task<Void>) {
        mPendingGeofenceTask = PendingGeofenceTask.NONE
        if (task.isSuccessful) {
            updateGeofencesAdded(!getGeofencesAdded())
            setButtonsEnabledState()

            val messageId = if (getGeofencesAdded())
                R.string.geofences_added
            else
                R.string.geofences_removed
            Toast.makeText(this, getString(messageId), Toast.LENGTH_SHORT).show()
        } else {
            // Get the status code for the error and log it using a user-friendly message.
            val errorMessage = GeofenceErrorMessages.getErrorString(this, task.exception!!)
            Log.w(TAG, errorMessage)
        }
    }

    /**
     * Gets a PendingIntent to send with the request to add or remove Geofences. Location Services
     * issues the Intent inside this PendingIntent whenever a geofence transition occurs for the
     * current list of geofences.
     *
     * @return A PendingIntent for the IntentService that handles geofence transitions.
     */
    private fun getGeofencePendingIntent(): PendingIntent? {
        // Reuse the PendingIntent if we already have it.
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent
        }
        val intent = Intent(this, GeofenceBroadcastReceiver::class.java)
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
        // addGeofences() and removeGeofences().
        mGeofencePendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        return mGeofencePendingIntent
    }

    /**
     * This sample hard codes geofence data. A real app might dynamically create geofences based on
     * the user's location.
     */
    private fun populateGeofenceList() {
        var latLng =
            LatLng(mainViewModel.latitude.data.value?.toDouble()!!, mainViewModel.longitude.data.value?.toDouble()!!)
        mGeofenceList.clear()
        mGeofenceList.add(
            Geofence.Builder()
                // Set the request ID of the geofence. This is a string to identify this
                // geofence.
                .setRequestId(GEOFENCE_KEY)

                // Set the circular region of this geofence.
                .setCircularRegion(
                    latLng.latitude,
                    latLng.longitude,
                    mainViewModel.radius.data.value?.toFloat()!!
                )

                // Set the expiration duration of the geofence. This geofence gets automatically
                // removed after this period of time.
                .setExpirationDuration(GEOFENCE_EXPIRATION_IN_MILLISECONDS)

                // Set the transition types of interest. Alerts are only generated for these
                // transition. We track entry and exit transitions in this sample.
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)

                // Create the geofence.
                .build()
        )

    }

    /**
     * Ensures that only one button is enabled at any time. The Add Geofences button is enabled
     * if the user hasn't yet added geofences. The Remove Geofences button is enabled if the
     * user has added geofences.
     */
    private fun setButtonsEnabledState() {
        mainViewModel.isGeofenceAdded.value = getGeofencesAdded()
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

    /**
     * Returns true if geofences were added, otherwise false.
     */
    private fun getGeofencesAdded(): Boolean {
        return sharedPreferenceManager.geofenceAddedKey
    }

    /**
     * Stores whether geofences were added or removed in [SharedPreferences];
     *
     * @param added Whether geofences were added or removed.
     */
    private fun updateGeofencesAdded(added: Boolean) {
        sharedPreferenceManager.geofenceAddedKey = added
    }

    /**
     * Performs the geofencing task that was pending until location permission was granted.
     */
    private fun performPendingGeofenceTask() {
        if (mPendingGeofenceTask == PendingGeofenceTask.ADD) {
            addGeofences()
        } else if (mPendingGeofenceTask == PendingGeofenceTask.REMOVE) {
            removeGeofences()
        }
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
     * Callback received when a permissions request has been completed.
     */
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>,
        grantResults: IntArray
    ) {
        Log.i(TAG, "onRequestPermissionResult")
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            when {
                grantResults.isEmpty() -> // If user interaction was interrupted, the permission request is cancelled and you
                    // receive empty arrays.
                    Log.i(TAG, "User interaction was cancelled.")
                grantResults[0] == PackageManager.PERMISSION_GRANTED -> {
                    Log.i(TAG, "Permission granted.")
                    performPendingGeofenceTask()
                }
                else -> {
                    // Permission denied.

                    // Notify the user via a SnackBar that they have rejected a core permission for the
                    // app, which makes the Activity useless. In a real app, core permissions would
                    // typically be best requested during a welcome-screen flow.

                    // Additionally, it is important to remember that a permission might have been
                    // rejected without asking the user for permission (device policy or "Never ask
                    // again" prompts). Therefore, a user interface affordance is typically implemented
                    // when permissions are denied. Otherwise, your app could appear unresponsive to
                    // touches or interactions which have required permissions.
                    showSnackbar(R.string.permission_denied_explanation, R.string.settings,
                        View.OnClickListener {
                            // Build intent that displays the App settings screen.
                            val intent = Intent()
                            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                            val uri = Uri.fromParts(
                                "package",
                                BuildConfig.APPLICATION_ID, null
                            )
                            intent.data = uri
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            startActivity(intent)
                        })
                    mPendingGeofenceTask = PendingGeofenceTask.NONE
                }
            }
        }
    }

    private fun restoreData() {
        mainViewModel.latitude.data.value = sharedPreferenceManager.latitude
        mainViewModel.longitude.data.value = sharedPreferenceManager.longitude
        mainViewModel.radius.data.value = sharedPreferenceManager.radius
        mainViewModel.wifi.data.value = sharedPreferenceManager.wifi
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
        subscribeAddGeofenceCallback()
        subscribeRemoveGeofenceCallback()
        subscribeGeneralError()
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

    fun subscribeAddGeofenceCallback() {
        mainViewModel.addGeofenceCallback.observe(this, Observer {
            populateGeofenceList()
            addGeofencesHandler()
        })
    }

    fun subscribeRemoveGeofenceCallback() {
        mainViewModel.removeGeofenceCallback.observe(this, Observer {
            removeGeofencesHandler()
        })
    }

    fun subscribeGeneralError() {
        mainViewModel.generalError.observe(this, Observer {
            showSnackbar(it)
        })
    }
}
