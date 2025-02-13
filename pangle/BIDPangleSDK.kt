package io.bidapp.networks.pangle

import android.content.Context
import com.bytedance.sdk.openadsdk.api.init.PAGConfig
import com.bytedance.sdk.openadsdk.api.init.PAGSdk
import io.bidapp.sdk.BIDConsent
import io.bidapp.sdk.BIDLog
import io.bidapp.sdk.ConsentListener
import io.bidapp.sdk.protocols.BIDNetworkAdapterDelegateProtocol
import io.bidapp.sdk.protocols.BIDNetworkAdapterProtocol

internal const val ADAPTERVERSION = "2.3.0"
internal const val SDKVERSION = "6.5.0.3"
@PublishedApi
internal class BIDPangleSDK(
    private val adapter: BIDNetworkAdapterProtocol,
    private val appId: String?,
    appSignature: String?
) : BIDNetworkAdapterDelegateProtocol, ConsentListener {
    private val TAG = "Pangle SDK adapter"



    override fun initializeSDK(context: Context) {
        if (isInitialized(context) || adapter.initializationInProgress())
        {
            return
        }
        if (appId.isNullOrEmpty()){
            initializationFailed("Liftoff appId is null or empty")
            return
        }
        val pagConfigBuilder = PAGConfig.Builder()
        if (CCPA == true) pagConfigBuilder.setDoNotSell(0)
        else if (CCPA == false) pagConfigBuilder.setDoNotSell(1)

        if (GDPR == true) pagConfigBuilder.setGDPRConsent(0)
        else if (GDPR == false) pagConfigBuilder.setGDPRConsent(1)

        if (COPPA == true) pagConfigBuilder.setChildDirected(1)
        else if (COPPA == false) pagConfigBuilder.setChildDirected(1)

        val pagConfig = pagConfigBuilder.appId(appId).debugLog(logging).supportMultiProcess(false).build()

        PAGSdk.init(context, pagConfig, object : PAGSdk.PAGInitCallback{
            override fun success() {
                initializationComplete()
            }

            override fun fail(p0: Int, p1: String?) {
                initializationFailed("Error $p1. Code $p0")
            }

        })
    }


    fun initializationComplete() {
        BIDLog.d(TAG, "Initialization complete")
        adapter.onInitializationComplete(true, null)
    }

    fun initializationFailed(err: String) {
        BIDLog.d(TAG, "Initialization failed. Error:$err")
        adapter.onInitializationComplete(false, err)
    }

    override fun isInitialized(context: Context): Boolean {
        return PAGSdk.isInitSuccess()
    }

    override fun enableTesting() {
        testMode = true
    }

    override fun enableLogging(context: Context) {
        logging = true
    }

    override fun sharedSDK(): Any? {
        return mapOf(
            "adapterVersion" to ADAPTERVERSION,
            "sdkVersion" to SDKVERSION
        )
    }

    override fun setConsent(consent: BIDConsent, context: Context?) {
        if (consent.CCPA != null) CCPA = consent.CCPA
        if (consent.GDPR != null) GDPR = consent.GDPR
        if (consent.COPPA != null) COPPA = consent.COPPA
    }
    internal companion object{
        var CCPA : Boolean? = null
        var GDPR : Boolean? = null
        var COPPA : Boolean? = null
        var logging = false
        var testMode = false
    }

}

