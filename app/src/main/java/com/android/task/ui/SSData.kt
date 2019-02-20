package com.android.task.ui

import androidx.lifecycle.MutableLiveData

data class SSData(
    val data: MutableLiveData<String> = MutableLiveData(),
    val dataError: MutableLiveData<String> = MutableLiveData()
)