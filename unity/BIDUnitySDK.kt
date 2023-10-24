package io.bidapp.networks.unity

import android.app.Activity
import android.util.Log
import io.bidapp.sdk.protocols.BIDNetworkAdapterDelegateProtocol
import io.bidapp.sdk.protocols.BIDNetworkAdapterProtocol
import com.unity3d.ads.IUnityAdsInitializationListener
import com.unity3d.ads.UnityAds
import com.unity3d.ads.metadata.MetaData
import io.bidapp.sdk.BIDConsent
import io.bidapp.sdk.BIDLog
import io.bidapp.sdk.ConsentListener

@PublishedApi
internal class BIDUnitySDK(
    val adapter: BIDNetworkAdapterProtocol? = null,
    val gameId: String? = null,
    appSignature: String?
) : BIDNetworkAdapterDelegateProtocol, ConsentListener {
    var testMode = false
    val TAG = "Unity SDK"

    override fun enableLogging(activity: Activity) {
        UnityAds.setDebugMode(true)
    }

    override fun setConsent(consent: BIDConsent, activity: Activity?) {
        val privacyConsentMetaData = MetaData(activity)
        consent.let {
            if (consent.GDPR != null) {
                privacyConsentMetaData.set("gdpr.consent", consent.GDPR)
                privacyConsentMetaData.commit()
            }
            if (consent.CCPA != null) {
                privacyConsentMetaData.set("privacy.consent", consent.CCPA)
                privacyConsentMetaData.commit()
            }
            privacyConsentMetaData.set("privacy.mode", "mixed")
            privacyConsentMetaData.commit()

            if (consent.COPPA != null) {
                privacyConsentMetaData.set("user.nonbehavioral", consent.COPPA)
            }
        }
    }

    override fun enableTesting() {
        testMode = true
    }

    override fun initializeSDK(activity: Activity) {
        if (!this.isInitialized(activity)) {
            adapter?.onInitializationStart()
            UnityAds.initialize(
                activity,
                gameId,
                testMode,
                object : IUnityAdsInitializationListener {
                    override fun onInitializationComplete() {
                        initializationComplete()
                    }

                    override fun onInitializationFailed(
                        error: UnityAds.UnityAdsInitializationError?,
                        message: String?
                    ) {
                        initializationFailed(error?.name.toString())
                    }
                })
        }
    }

    fun initializationComplete() {
        BIDLog.d(TAG, "Initialization complete")
        adapter?.onInitializationComplete(true, null)
    }

    fun initializationFailed(err: String) {
        BIDLog.d(TAG, "Initialization failed. Error:$err")
        adapter?.onInitializationComplete(false, err)
    }

    override fun isInitialized(activity: Activity): Boolean {
        return UnityAds.isInitialized()
    }

    override fun sharedSDK(): Any? {
        return null
    }
}