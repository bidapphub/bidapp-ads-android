package io.bidapp.networks.ironsource

import android.content.Context
import com.ironsource.mediationsdk.IronSource
import com.ironsource.mediationsdk.logger.IronSourceError
import com.unity3d.ironsourceads.InitListener
import com.unity3d.ironsourceads.InitRequest
import com.unity3d.ironsourceads.IronSourceAds
import io.bidapp.sdk.BIDConsent
import io.bidapp.sdk.BIDLog
import io.bidapp.sdk.ConsentListener
import io.bidapp.sdk.protocols.BIDNetworkAdapterDelegateProtocol
import io.bidapp.sdk.protocols.BIDNetworkAdapterProtocol


internal const val ADAPTERVERSION = "2.2.5"
internal const val SDKVERSION = "6.18.0"

@PublishedApi
internal class BIDIronSourceSDK(
    private val adapter: BIDNetworkAdapterProtocol,
    private val appId: String?,
    secondKey: String?
) : BIDNetworkAdapterDelegateProtocol, ConsentListener {
    private val TAG = "IronSource SDK"
    private var isInitialize = false

    override fun initializeSDK(context: Context) {
        if (isInitialized(context) || adapter.initializationInProgress()) {
            return
        }
        if (appId.isNullOrEmpty()) {
            initializationFailed("IronSource appId is null or empty")
            return
        }
        adapter.onInitializationStart()
        val initRequest = InitRequest.Builder(appId).withLegacyAdFormats(
            listOf(
                IronSourceAds.AdFormat.INTERSTITIAL,
                IronSourceAds.AdFormat.REWARDED,
                IronSourceAds.AdFormat.BANNER
            )
        ).build()

        IronSourceAds.init(context, initRequest, object : InitListener{
            override fun onInitFailed(error: IronSourceError) {
                isInitialize = false
                initializationFailed(error.errorMessage ?: "initialization failed with unknown error")
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
        IronSource.setAdaptersDebug(true)
    }

    override fun enableLogging(context: Context) {}

    override fun sharedSDK(): Any {
        return mapOf(
            "adapterVersion" to ADAPTERVERSION,
            "sdkVersion" to SDKVERSION
        )
    }

    override fun setConsent(consent: BIDConsent, context: Context?) {
        if (consent.GDPR != null) {
            IronSource.setConsent(consent.GDPR!!)
        }
        if (consent.CCPA != null) {
            IronSource.setMetaData("do_not_sell",if (consent.CCPA!!) "false" else "true")
        }
        if (consent.COPPA != null) {
            IronSource.setMetaData("is_child_directed", if (consent.COPPA!!) "true" else "false")
        }
    }


}