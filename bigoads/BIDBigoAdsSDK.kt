package io.bidapp.networks.bigoads

import android.content.Context
import io.bidapp.sdk.BIDConsent
import io.bidapp.sdk.BIDLog
import io.bidapp.sdk.ConsentListener
import io.bidapp.sdk.protocols.BIDNetworkAdapterDelegateProtocol
import io.bidapp.sdk.protocols.BIDNetworkAdapterProtocol
import sg.bigo.ads.BigoAdSdk
import sg.bigo.ads.ConsentOptions
import sg.bigo.ads.api.AdConfig

internal const val ADAPTERVERSION = "2.2.5"
internal const val SDKVERSION = "5.1.0"

@PublishedApi
internal class BIDBigoAdsSDK(
    private val adapter: BIDNetworkAdapterProtocol,
    private val appId: String?,
    secondKey: String?
) : BIDNetworkAdapterDelegateProtocol, ConsentListener {

    private val TAG = "BigoAds SDK"
    var enableLogging = false


    override fun initializeSDK(context: Context) {
        if (isInitialized(context) || adapter.initializationInProgress())
        {
            return
        }
        if (appId.isNullOrEmpty()){
            initializationFailed("BigoAds appId is null or empty")
            return
        }
        adapter.onInitializationStart()
        val config = AdConfig.Builder().setDebug(enableLogging).setAppId(appId).build()
        BigoAdSdk.initialize(context, config
        ) {
            initializationComplete()
        }

    }

    private fun initializationComplete() {
        BIDLog.d(TAG, "Initialization complete")
        adapter.onInitializationComplete(true, null)
    }

    private fun initializationFailed(err: String) {
        BIDLog.d(TAG, "Initialization failed. Error:$err")
        adapter.onInitializationComplete(false, err)
    }

    override fun isInitialized(context: Context): Boolean {
        return BigoAdSdk.isInitialized()
    }

    override fun enableTesting() {
        testMode = true
    }

    override fun enableLogging(context: Context) {
        enableLogging = true
    }

    override fun sharedSDK(): Any? {
        return mapOf(
            "adapterVersion" to ADAPTERVERSION,
            "sdkVersion" to SDKVERSION
        )
    }

    override fun setConsent(consent: BIDConsent, context: Context?) {
        if (consent.GDPR != null) {
            if (context == null) {
                BIDLog.e(TAG, "Error set GDPR - context is null")
                return
            }
            BigoAdSdk.setUserConsent(context, ConsentOptions.GDPR, consent.GDPR!!)
        }
        if (consent.CCPA != null) {
            if (context == null) {
                BIDLog.e(TAG, "Error set CCPA - context is null")
                return
            }
            BigoAdSdk.setUserConsent(context, ConsentOptions.CCPA, consent.CCPA!!)
        }
        if (consent.COPPA != null) {
            coppa = consent.COPPA
            if (context == null) {
                BIDLog.e(TAG, "Error set COPA - context is null")
                return
            }
            BigoAdSdk.setUserConsent(context, ConsentOptions.COPPA, consent.CCPA!!)
        }
    }

    companion object{
        var testMode : Boolean? = false
        var coppa : Boolean? = false
    }
}