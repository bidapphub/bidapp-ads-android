package io.bidapp.networks.startIo

import android.content.Context
import com.startapp.sdk.adsbase.StartAppAd
import com.startapp.sdk.adsbase.StartAppSDK
import io.bidapp.sdk.BIDConsent
import io.bidapp.sdk.BIDLog
import io.bidapp.sdk.ConsentListener
import io.bidapp.sdk.protocols.BIDNetworkAdapterDelegateProtocol
import io.bidapp.sdk.protocols.BIDNetworkAdapterProtocol

internal const val ADAPTERVERSION = "2.2.5"
internal const val SDKVERSION = "5.1.0"
class BIDStartIoSDK(
    private val adapter: BIDNetworkAdapterProtocol?,
    private val appId: String?,
    val developerId: String?
) : BIDNetworkAdapterDelegateProtocol, ConsentListener {
    private val TAG = "StartIo SDK"
    private var isInitializationComplete = false

    override fun setConsent(consent: BIDConsent, context: Context?) {
        consent.let {
            if (consent.GDPR != null && context != null) {
                StartAppSDK.setUserConsent (context,
                    "pas",
                    System.currentTimeMillis(),
                    consent.GDPR!!
                )
            }
            if (consent.CCPA != null) {
                val ccpa = if (consent.CCPA == true) "1YNN" else "1YYN"
                if (context != null) {
                    StartAppSDK.getExtras(context)
                        .edit()
                        .putString("IABUSPrivacy_String", ccpa)
                        .apply()
                }
            }
            if (consent.COPPA != null) {
                BIDLog.d(TAG, "Warning! To enable COPPA compliance for the Start.io adapter, you need to specify it in the manifest file of your application.")
            }
        }
    }

    override fun initializeSDK(context: Context) {
        if (isInitialized(context) || adapter?.initializationInProgress() == true)
        {
            return
        }
        if (appId == null){
            initializationFailed("StartIo appid is null")
            return
        }
        adapter?.onInitializationStart()
        StartAppSDK.init(context, appId, false)
        isInitializationComplete = true
        initializationComplete()
    }

    override fun isInitialized(context: Context): Boolean {
        return isInitializationComplete
    }

    private fun initializationComplete() {
        BIDLog.d(TAG, "Initialization complete")
        adapter?.onInitializationComplete(true, null)
    }

    private fun initializationFailed(err: String) {
        BIDLog.d(TAG, "Initialization failed. Error:$err")
        adapter?.onInitializationComplete(false, err)
    }

    override fun enableTesting() {
        StartAppSDK.setTestAdsEnabled(true)
    }

    override fun enableLogging(context: Context) {}

    override fun sharedSDK(): Any? {
        return mapOf(
            "adapterVersion" to ADAPTERVERSION,
            "sdkVersion" to SDKVERSION
        )
    }
}