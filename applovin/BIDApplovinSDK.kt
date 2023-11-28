package io.bidapp.networks.applovin


import android.app.Activity
import android.content.Context
import com.applovin.sdk.AppLovinPrivacySettings
import com.applovin.sdk.AppLovinSdk
import io.bidapp.sdk.BIDConsent
import io.bidapp.sdk.ConsentListener
import io.bidapp.sdk.protocols.BIDNetworkAdapterDelegateProtocol
import io.bidapp.sdk.protocols.BIDNetworkAdapterProtocol
@PublishedApi
internal class BIDApplovinSDK(
    val adapter: BIDNetworkAdapterProtocol? = null,
    appId : String?,
    appSignature: String?
) : BIDNetworkAdapterDelegateProtocol, ConsentListener {

    override fun enableLogging(context: Context) {
        AppLovinSdk.getInstance(context).settings.setVerboseLogging(true)
    }

    override fun enableTesting() {
    }

    override fun setConsent(consent: BIDConsent, context: Context?) {
        if(consent.GDPR != null){
            AppLovinPrivacySettings.setHasUserConsent(consent.GDPR!!, context)
        }
        if(consent.CCPA != null){
            AppLovinPrivacySettings.setDoNotSell(!consent.CCPA!!, context)
        }
        if (consent.COPPA != null){
            AppLovinPrivacySettings.setIsAgeRestrictedUser(consent.COPPA!!, context)
        }
    }

    override fun initializeSDK(context: Context) {
        if (isInitialized(context) ||
            null == adapter ||
            adapter.initializationInProgress() ) {
            return
        }

        adapter.onInitializationStart()
        ApplovinInitializer.start(adapter,context)
    }

    override fun isInitialized(context: Context): Boolean {
        return AppLovinSdk.getInstance(context.applicationContext).isInitialized
    }

    override fun sharedSDK(): Any? {
        return null
    }

}