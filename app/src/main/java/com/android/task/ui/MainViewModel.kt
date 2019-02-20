package com.android.task.ui

import android.text.Editable
import android.text.TextWatcher
import androidx.databinding.Bindable
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.android.task.repository.Repository
import com.android.task.vm.ObservableViewModel
import javax.inject.Inject

class MainViewModel @Inject constructor(
    private val repository: Repository): ObservableViewModel() {

    var isGeofenceAdded = MutableLiveData<Boolean>()
    val latitude: SSData = SSData()
    val longitude: SSData = SSData()
    val radius: SSData = SSData()
    val wifi: SSData = SSData()

    init {
        latitude.dataError.value = "Input requried"
        longitude.dataError.value = "Input requried"
        radius.dataError.value = "Input requried"
        wifi.dataError.value = "Input requried"
    }

    fun addGeofence() {
        if (isValidData()) {
            isGeofenceAdded.value = true
        }
    }

    fun removeGeofence() {
        isGeofenceAdded.value = false
    }

    @Bindable
    fun getLatitudeTextWatcher(): TextWatcher {
        return object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                setLatitude(s.toString())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        }
    }

    @Bindable
    fun getLongitudeTextWatcher(): TextWatcher {
        return object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                setLongitude(s.toString())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        }
    }

    @Bindable
    fun getRadiusTextWatcher(): TextWatcher {
        return object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                setRadius(s.toString())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        }
    }

    @Bindable
    fun getWifiTextWatcher(): TextWatcher {
        return object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                setWifi(s.toString())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        }
    }

    fun setLatitude(latitude: String) {
        if (!latitude.isNullOrEmpty()) {
            if (isValidLatitude(latitude.toDouble())) {
                this.latitude.data.value = latitude
                this.latitude.dataError.value = null
            } else {
                this.latitude.dataError.value = "Input requried"
            }
        }else {
            this.latitude.dataError.value = "Input requried"
        }
    }

    fun setLongitude(longitude: String) {
        if (!longitude.isNullOrEmpty()) {
            if (isValidLongitude(longitude.toDouble())) {
                this.longitude.data.value = longitude
                this.longitude.dataError.value = null
            } else {
                this.longitude.dataError.value = "Input requried"
            }
        } else {
            this.longitude.dataError.value = "Input requried"
        }

    }

    fun setRadius(radius: String) {
        if (!radius.isNullOrEmpty()) {
            this.radius.data.value = radius
            this.radius.dataError.value = null

        } else {
            this.radius.dataError.value = "Input requried"
        }
    }

    fun setWifi(wifi: String) {
        if (!wifi.isNullOrEmpty()) {
            this.wifi.data.value = wifi
            this.wifi.dataError.value = null
        } else {
            this.wifi.dataError.value = "Input requried"
        }
    }

    fun isValidLatitude(lat: Double): Boolean {
        if (lat < -90 || lat > 90) {
            return false
        }
        return true
    }

    fun isValidLongitude(lng: Double): Boolean {
        if (lng < -180 || lng > 180) {
            return false
        }
        return true
    }

    fun isValidData(): Boolean {
        return latitude.dataError.value.isNullOrEmpty()
                && longitude.dataError.value.isNullOrEmpty()
                && radius.dataError.value.isNullOrEmpty()
                && wifi.dataError.value.isNullOrEmpty()
    }

}