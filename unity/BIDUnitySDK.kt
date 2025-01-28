package io.bidapp.networks.unity

import android.content.Context
import com.unity3d.ads.IUnityAdsInitializationListener
import com.unity3d.ads.UnityAds
import com.unity3d.ads.metadata.MetaData
import io.bidapp.sdk.BIDConsent
import io.bidapp.sdk.BIDLog
import io.bidapp.sdk.ConsentListener
import io.bidapp.sdk.protocols.BIDNetworkAdapterDelegateProtocol
import io.bidapp.sdk.protocols.BIDNetworkAdapterProtocol

internal const val ADAPTERVERSION = "2.3.0"
internal const val SDKVERSION = "4.12.15"

@PublishedApi
internal class BIDUnitySDK(
    private val adapter: BIDNetworkAdapterProtocol? = null,
    private val gameId: String? = null,
    appSignature: String?
) : BIDNetworkAdapterDelegateProtocol, ConsentListener {
    private var testMode = false
    private val TAG = "Unity SDK"

    override fun enableLogging(context: Context) {
        UnityAds.debugMode = true
    }

    override fun setConsent(consent: BIDConsent, context: Context?) {
        val privacyConsentMetaData = MetaData(context)
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

    override fun initializeSDK(context: Context) {
        if (isInitialized(context) || adapter?.initializationInProgress() == true) {
            return
        }
        if (gameId.isNullOrEmpty()) {
            initializationFailed("Unity gameId is null or empty")
            return
        }
        adapter?.onInitializationStart()
        UnityAds.initialize(
            context.applicationContext,
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

    fun initializationComplete() {
        BIDLog.d(TAG, "Initialization complete")
        adapter?.onInitializationComplete(true, null)
    }

    fun initializationFailed(err: String) {
        BIDLog.d(TAG, "Initialization failed. Error:$err")
        adapter?.onInitializationComplete(false, err)
    }

    override fun isInitialized(context: Context): Boolean {
        return UnityAds.isInitialized
    }

    override fun sharedSDK(): Any? {
        return mapOf(
            "adapterVersion" to ADAPTERVERSION,
            "sdkVersion" to SDKVERSION
        )
    }
}