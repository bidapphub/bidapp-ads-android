package io.bidapp.networks.admob

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
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

    override fun setConsent(consent: BIDConsent, context: Context?) {
        if (consent.COPPA != null){
            var coppa:Int = RequestConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_UNSPECIFIED
            if (consent.COPPA == true) coppa = RequestConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_TRUE
            else if (consent.COPPA == false) coppa = RequestConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_FALSE
            val requestConfiguration = MobileAds.getRequestConfiguration()
                .toBuilder()
                .setTagForChildDirectedTreatment(coppa)
                .build()
            MobileAds.setRequestConfiguration(requestConfiguration)
        }
        currentConsentGDPR = consent.GDPR
        if (consent.GDPR != null){
            var gdpr:Int = RequestConfiguration.TAG_FOR_UNDER_AGE_OF_CONSENT_UNSPECIFIED
            if (consent.GDPR == true) gdpr = RequestConfiguration.TAG_FOR_UNDER_AGE_OF_CONSENT_TRUE
            else if (consent.GDPR == false) gdpr = RequestConfiguration.TAG_FOR_UNDER_AGE_OF_CONSENT_FALSE
            val requestConfiguration = MobileAds.getRequestConfiguration()
                .toBuilder()
                .setTagForUnderAgeOfConsent(gdpr)
                .build()
            MobileAds.setRequestConfiguration(requestConfiguration)
        }
        if (consent.CCPA != null) {
            if (consent.CCPA == false) sharedPreferences?.edit()?.putInt("gad_rdp", 1)?.apply()
            else sharedPreferences?.edit()?.remove("gad_rdp")?.apply()
        }
    }

    override fun initializeSDK(context: Context) {

        if (isInitialized(context) ||
            null == adapter ||
            adapter.initializationInProgress()
        ) {
            return
        }
        adapter.onInitializationStart()
        MobileAds.initialize(
            context
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


    override fun isInitialized(context: Context): Boolean {
        return isInitializationComplete
    }

    override fun enableTesting() {
    }

    override fun enableLogging(context: Context) {
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