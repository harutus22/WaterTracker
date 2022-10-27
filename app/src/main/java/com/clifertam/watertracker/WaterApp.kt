package com.clifertam.watertracker

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import org.solovyev.android.checkout.Billing

@HiltAndroidApp
class WaterApp: Application() {
    private val mBilling = Billing(this, object : Billing.DefaultConfiguration() {
        override fun getPublicKey(): String {
            return KEY
        }
    })

    init {
        sInstance = this
    }

    companion object {
        private val KEY = ""
        private var sInstance: WaterApp? = null
        fun get(): WaterApp? {
            return sInstance
        }
    }

    fun getBilling(): Billing? {
        return mBilling
    }
}