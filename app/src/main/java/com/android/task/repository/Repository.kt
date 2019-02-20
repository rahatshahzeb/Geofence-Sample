package com.android.task.repository

import com.android.task.db.SSDb
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Repository @Inject constructor(
    private val ssDb: SSDb
){
}