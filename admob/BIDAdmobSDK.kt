package io.bidapp.networks.admob

import android.content.Context
import android.content.SharedPreferences
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.ads.initialization.AdapterStatus
import io.bidapp.sdk.BIDConsent
import io.bidapp.sdk.BIDLog
import io.bidapp.sdk.ConsentListener
import io.bidapp.sdk.protocols.BIDNetworkAdapterDelegateProtocol
import io.bidapp.sdk.protocols.BIDNetworkAdapterProtocol

internal const val ADAPTERVERSION = "1.1.0"
internal const val SDKVERSION = "23.0.0"

@PublishedApi
internal class BIDAdmobSDK(
    private val adapter: BIDNetworkAdapterProtocol,
    appId: String?,
    appSignature: String?
) :
    BIDNetworkAdapterDelegateProtocol, ConsentListener {

    private var sharedPreferences: SharedPreferences? = null
    private var isInitializationComplete = false
    private val TAG = "Admob SDK"

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
        if (isInitialized(context) || adapter.initializationInProgress())
        {
            return
        }
        adapter.onInitializationStart()
        MobileAds.initialize(context) { p0 ->
            if (p0.adapterStatusMap[p0.adapterStatusMap.keys.first()]?.initializationState == AdapterStatus.State.READY) {
                initializationComplete()
                isInitializationComplete = true
            } else {
                initializationFailed("Error initialization AdMob")
            }
        }
    }

    private fun initializationComplete() {
        BIDLog.d(TAG, "Initialization complete")
        adapter.onInitializationComplete(true, null)
    }

    private fun initializationFailed(error: String) {
        BIDLog.d(TAG, "Initialization failed. Error: $error")
        adapter.onInitializationComplete(false, error)
    }


    override fun isInitialized(context: Context): Boolean {
        return isInitializationComplete
    }

    override fun enableTesting() {}

    override fun enableLogging(context: Context) {}

    override fun sharedSDK(): Any? {
        return mapOf(
            "adapterVersion" to ADAPTERVERSION,
            "sdkVersion" to SDKVERSION
        )
    }

    internal companion object {
        private var currentConsentGDPR: Boolean? = null

        fun getGDPR(): Boolean? {
            return currentConsentGDPR
        }
    }
}