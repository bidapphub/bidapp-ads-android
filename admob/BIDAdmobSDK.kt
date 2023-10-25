package io.bidapp.networks.admob

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.initialization.AdapterStatus
import io.bidapp.sdk.BIDConsent
import io.bidapp.sdk.BIDLog
import io.bidapp.sdk.ConsentListener
import io.bidapp.sdk.protocols.BIDNetworkAdapterDelegateProtocol
import io.bidapp.sdk.protocols.BIDNetworkAdapterProtocol

@PublishedApi
internal class BIDAdmobSDK(
    private val adapter: BIDNetworkAdapterProtocol?,
    appId: String,
    appSignature: String?
) :
    BIDNetworkAdapterDelegateProtocol, ConsentListener {

    var sharedPreferences: SharedPreferences? = null
    var isInitializationComplete = false
    val TAG = "Admob SDK"

    override fun setConsent(consent: BIDConsent, activity: Activity?) {
        currentConsentGDPR = consent.GDPR
        if (sharedPreferences == null)
            sharedPreferences = activity?.getSharedPreferences(
                activity.packageName.toString(),
                Context.MODE_PRIVATE
            )
        if (consent.CCPA != null) {
            if (consent.CCPA == true) sharedPreferences?.edit()?.putInt("gad_rdp", 1)?.apply()
            else sharedPreferences?.edit()?.remove("gad_rdp")?.apply()
        }
    }

    override fun initializeSDK(activity: Activity) {
        if (isInitialized(activity) ||
            null == adapter ||
            adapter.initializationInProgress()
        ) {
            return
        }
        adapter.onInitializationStart()
        MobileAds.initialize(
            activity
        ) { p0 ->
            if (p0.adapterStatusMap[p0.adapterStatusMap.keys.first()]?.initializationState == AdapterStatus.State.READY) {
                initializationComplete()
                isInitializationComplete = true
            } else {
                initializationFailed("Error initialization AdMob")
            }
        }
    }

    fun initializationComplete() {
        BIDLog.d(TAG, "Initialization complete")
        adapter?.onInitializationComplete(true, null)
    }

    fun initializationFailed(err: String) {
        BIDLog.d(TAG, "Initialization failed. Error: $err")
        adapter?.onInitializationComplete(false, err)
    }


    override fun isInitialized(activity: Activity): Boolean {
        return isInitializationComplete
    }

    override fun enableTesting() {
    }

    override fun enableLogging(activity: Activity) {
    }

    override fun sharedSDK(): Any? {
        return null
    }

    companion object {
        private var currentConsentGDPR: Boolean? = null

        fun getGDPR(): Boolean? {
            return currentConsentGDPR
        }
    }
}