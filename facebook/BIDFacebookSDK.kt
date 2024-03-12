package io.bidapp.networks.facebook

import android.content.Context
import com.facebook.ads.AdSettings
import com.facebook.ads.AudienceNetworkAds
import com.facebook.ads.BidderTokenProvider
import io.bidapp.sdk.BIDConsent
import io.bidapp.sdk.BIDLog
import io.bidapp.sdk.ConsentListener
import io.bidapp.sdk.protocols.BIDNetworkAdapterDelegateProtocol
import io.bidapp.sdk.protocols.BIDNetworkAdapterProtocol

class BIDFacebookSDK(
    private val adapter: BIDNetworkAdapterProtocol? = null,
    val appId: String? = null,
    appSignature: String?
) : BIDNetworkAdapterDelegateProtocol, ConsentListener {

    private val TAG = "Facebook SDK"
    private var isInitialize = false
    private val initializeListener = AudienceNetworkAds.InitListener { p0 ->
        if(p0?.isSuccess == true){
            isInitialize = true
            initializationComplete()
        } else {
            isInitialize = false
            initializationFailed(p0?.message ?: "initialization failed with unknown error")
        }
    }

    override fun setConsent(consent: BIDConsent, context: Context?) {
        if (consent.COPPA != null) {
            AdSettings.setMixedAudience(consent.COPPA!!)
        }
    }


    private fun initializationComplete() {
        BIDLog.d(TAG, "Initialization complete")
        adapter?.onInitializationComplete(true, null)
    }

    private fun initializationFailed(err: String) {
        BIDLog.d(TAG, "Initialization failed. Error:$err")
        adapter?.onInitializationComplete(false, err)
    }



    override fun initializeSDK(context: Context) {
        if (isInitialized(context) || adapter?.initializationInProgress() == true)
        {
            return
        }
        adapter?.onInitializationStart()
        AudienceNetworkAds.buildInitSettings(context)
            .withInitListener(initializeListener)
            .initialize()
    }

    override fun isInitialized(context: Context): Boolean {
       return isInitialize
    }

    override fun enableTesting() {
        AdSettings.setDebugBuild(true)
    }

    override fun enableLogging(context: Context) {}

    override fun sharedSDK(): Any? {
        return null
    }
}