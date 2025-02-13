package io.bidapp.networks.mintegral

import android.content.Context
import com.mbridge.msdk.MBridgeConstans
import com.mbridge.msdk.out.MBridgeSDKFactory
import com.mbridge.msdk.out.SDKInitStatusListener
import io.bidapp.sdk.BIDConsent
import io.bidapp.sdk.BIDLog
import io.bidapp.sdk.ConsentListener
import io.bidapp.sdk.protocols.BIDNetworkAdapterDelegateProtocol
import io.bidapp.sdk.protocols.BIDNetworkAdapterProtocol

internal const val ADAPTERVERSION = "2.4.0"
internal const val SDKVERSION = "16.9.31"
@PublishedApi
internal class BIDMintegralSDK(
    private val adapter: BIDNetworkAdapterProtocol,
    private val appId: String?,
    private val appKey: String?
) : BIDNetworkAdapterDelegateProtocol, ConsentListener {

    private val TAG = "Mintegral SDK"
    private var isInitialize = false
    private var logging = false
    private var CCPA : Boolean? = null
    private var GDPR : Boolean? = null

    override fun initializeSDK(context: Context) {
        if (isInitialized(context) || adapter.initializationInProgress()) {
            return
        }
        if (appId.isNullOrEmpty() || appKey.isNullOrEmpty()) {
            initializationFailed("Mintegral " + if (appId.isNullOrEmpty()) "appId" else "appKey" + "is null or empty")
            return
        }
        adapter.onInitializationStart()
        val mBridgeSDK = MBridgeSDKFactory.getMBridgeSDK()
        if (GDPR != null) {
            val consent = if (GDPR == true) MBridgeConstans.IS_SWITCH_ON else MBridgeConstans.IS_SWITCH_OFF
            mBridgeSDK.setUserPrivateInfoType(context, MBridgeConstans.AUTHORITY_ALL_INFO, consent)
            mBridgeSDK.setConsentStatus(context, consent)
        }
        if (CCPA != null){
            mBridgeSDK.setDoNotTrackStatus(context, CCPA == false)
        }
        val map: Map<String, String> = mBridgeSDK.getMBConfigurationMap(appId, appKey)
        mBridgeSDK.init(map, context, object : SDKInitStatusListener{
            override fun onInitFail(p0: String?) {
                initializationFailed(p0 ?: "initialization failed with unknown error")
                isInitialize = false
            }

            override fun onInitSuccess() {
                isInitialize = true
                initializationComplete()
            }
        })
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
        return isInitialize
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
        CCPA = consent.CCPA
        GDPR = consent.GDPR
        COPPA = consent.COPPA
    }

    companion object {
        var COPPA : Boolean? = null
        var testMode : Boolean? = false
    }
}