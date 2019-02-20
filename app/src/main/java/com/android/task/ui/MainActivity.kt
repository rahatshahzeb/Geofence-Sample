package com.android.task.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.android.task.R
import com.android.task.databinding.ActivityMainBinding
import dagger.android.AndroidInjection
import javax.inject.Inject

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var mainViewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        mainViewModel = ViewModelProviders.of(this, viewModelFactory).get(MainViewModel::class.java)
        binding.viewModel = mainViewModel
        binding.lifecycleOwner = this

        subscribeObserver()
    }

    fun subscribeObserver() {
        subscribeLatitudeErrorObserver()
        subscribeLongitudeErrorObserver()
        subscribeRadiusErrorObserver()
        subscribeWifiErrorObserver()
        subscribeAddRemoveObserver()
    }

    fun subscribeLatitudeErrorObserver() {
        mainViewModel.latitude.dataError.observe(this, Observer {
            binding.tilLatitude.error = it
        })
    }

    fun subscribeLongitudeErrorObserver() {
        mainViewModel.longitude.dataError.observe(this, Observer {
            binding.tilLongitude.error = it
        })
    }

    fun subscribeRadiusErrorObserver() {
        mainViewModel.radius.dataError.observe(this, Observer {
            binding.tilRadius.error = it
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
