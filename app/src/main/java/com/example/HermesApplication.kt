package com.example

import android.app.Application
import com.example.data.local.HermesDatabase

class HermesApplication : Application() {
    val database: HermesDatabase by lazy { HermesDatabase.getInstance(this) }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: HermesApplication
            private set
    }
}
