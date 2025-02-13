package io.bidapp.networks.applovinmax


import android.content.Context
import android.content.pm.PackageManager
import com.applovin.sdk.AppLovinPrivacySettings
import com.applovin.sdk.AppLovinSdk
import com.applovin.sdk.AppLovinSdkSettings
import io.bidapp.sdk.BIDConsent
import io.bidapp.sdk.BIDLog
import io.bidapp.sdk.ConsentListener
import io.bidapp.sdk.protocols.BIDNetworkAdapterDelegateProtocol
import io.bidapp.sdk.protocols.BIDNetworkAdapterProtocol

internal const val ADAPTERVERSION = "2.3.5"
internal const val SDKVERSION = "13.1.0"

@PublishedApi
internal class BIDApplovinMaxSDK(
    private val adapter: BIDNetworkAdapterProtocol? = null,
    private val appId: String?,
    appSignature: String?
) : BIDNetworkAdapterDelegateProtocol, ConsentListener {


    override fun enableLogging(context: Context) {
        AppLovinSdk.getInstance(context).settings.setVerboseLogging(true)
    }

    override fun setConsent(consent: BIDConsent, context: Context?) {
        if (consent.GDPR != null) {
            AppLovinPrivacySettings.setHasUserConsent(consent.GDPR!!, context)
        }
        if (consent.CCPA != null) {
            AppLovinPrivacySettings.setDoNotSell(!consent.CCPA!!, context)
        }
        if (consent.COPPA != null) {
            BIDLog.e("Applovin SDK", "IMPORTANCE!!! COPPA method not support for latest version adapter. Please review the official information on the AppLovin website.")
        }
    }

    override fun enableTesting() {
    }

    override fun initializeSDK(context: Context) {
        if (appId == null) {
            adapter?.onInitializationComplete(false, "AppLovinMax App Id is null")
            return
        }
        if (isInitialized(context) ||
            null == adapter ||
            adapter.initializationInProgress()
        ) {
            return
        }
        adapter.onInitializationStart()
        ApplovinInitializerMax.start(adapter, context, appId)
    }

    override fun isInitialized(context: Context): Boolean {
        return AppLovinSdk.getInstance(context).isInitialized
    }

    override fun sharedSDK(): Any {
        return mapOf(
            "adapterVersion" to ADAPTERVERSION,
            "sdkVersion" to SDKVERSION
        )
    }

}