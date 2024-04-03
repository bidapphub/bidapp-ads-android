package io.bidapp.networks.digitalturbine

import android.content.Context
import com.fyber.inneractive.sdk.external.InneractiveAdManager
import com.fyber.inneractive.sdk.external.OnFyberMarketplaceInitializedListener
import io.bidapp.sdk.BIDConsent
import io.bidapp.sdk.BIDLog
import io.bidapp.sdk.ConsentListener
import io.bidapp.sdk.protocols.BIDNetworkAdapterDelegateProtocol
import io.bidapp.sdk.protocols.BIDNetworkAdapterProtocol

class BIDDigitalTurbineSDK(
    private val adapter: BIDNetworkAdapterProtocol?,
    private val appId: String?,
    appSignature: String?
) : BIDNetworkAdapterDelegateProtocol, ConsentListener {

    private val TAG = "DigitalTurbine SDK"
    private var isInitializationComplete = false

    override fun setConsent(consent: BIDConsent, context: Context?) {
        if (consent.GDPR != null) {
            InneractiveAdManager.setGdprConsent(consent.GDPR!!)
        }
        if (consent.CCPA != null) {
            when (consent.CCPA) {
                true -> InneractiveAdManager.setUSPrivacyString("1YNN")
                false -> InneractiveAdManager.setUSPrivacyString("1YYN")
                else -> InneractiveAdManager.setUSPrivacyString("1---")
            }
        }
        if (consent.COPPA != null && consent.COPPA == true) {
            InneractiveAdManager.currentAudienceAppliesToCoppa()
        }
    }

    override fun initializeSDK(context: Context) {
        if (isInitialized(context) || adapter?.initializationInProgress() == true)
        {
            return
        }
        if (appId.isNullOrEmpty()){
            initializationFailed("App id is null or empty")
            return
        }
        adapter?.onInitializationStart()
        InneractiveAdManager.initialize(context, appId
        ) { digitalTurbineInitStatus ->
            if (digitalTurbineInitStatus == OnFyberMarketplaceInitializedListener.FyberInitStatus.SUCCESSFULLY){
                isInitializationComplete = true
                initializationComplete()
            }
            else {
                initializationFailed("$digitalTurbineInitStatus")
            }
        }
    }

    override fun isInitialized(context: Context): Boolean {
        return isInitializationComplete
    }

    override fun enableTesting() {}

    override fun enableLogging(context: Context) {}

    override fun sharedSDK(): Any? {
        return null
    }

    private fun initializationComplete() {
        BIDLog.d(TAG, "Initialization complete")
        adapter?.onInitializationComplete(true, null)
    }

    private fun initializationFailed(err: String) {
        BIDLog.d(TAG, "Initialization failed. Error:$err")
        adapter?.onInitializationComplete(false, err)
    }
}