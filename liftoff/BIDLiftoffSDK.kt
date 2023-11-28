package io.bidapp.networks.liftoff


import android.content.Context
import com.vungle.ads.InitializationListener
import com.vungle.ads.VungleAds
import com.vungle.ads.VungleError
import com.vungle.ads.VunglePrivacySettings
import io.bidapp.sdk.BIDConsent
import io.bidapp.sdk.BIDLog
import io.bidapp.sdk.ConsentListener
import io.bidapp.sdk.protocols.BIDNetworkAdapterDelegateProtocol
import io.bidapp.sdk.protocols.BIDNetworkAdapterProtocol


@PublishedApi
internal class BIDLiftoffSDK(
    val adapter: BIDNetworkAdapterProtocol,
    val appId: String,
    appSignature: String?
) : BIDNetworkAdapterDelegateProtocol, ConsentListener {

    val TAG = "Liftoff SDK"
    var testMode = false

    override fun enableLogging(context: Context) {
    }

    override fun setConsent(consent: BIDConsent, context: Context?) {
        if (consent.GDPR != null) {
            VunglePrivacySettings.setGDPRStatus(consent.GDPR!!, "1.0.0")
        }
        if (consent.CCPA != null) {
            VunglePrivacySettings.setCCPAStatus(consent.CCPA!!)
        }
        if (consent.COPPA != null) {
            VunglePrivacySettings.setCOPPAStatus(consent.COPPA!!)
        }
    }

    override fun enableTesting() {
        testMode = true
    }

    override fun initializeSDK(context: Context) {
        adapter.onInitializationStart()
        VungleAds.init(context, appId, object : InitializationListener {
            override fun onError(vungleError: VungleError) {
                this@BIDLiftoffSDK.initializationFailed(vungleError.errorMessage)
            }
            override fun onSuccess() {
                this@BIDLiftoffSDK.initializationComplete()
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
        return VungleAds.isInitialized()
    }

    override fun sharedSDK(): Any? {
        return null
    }
}