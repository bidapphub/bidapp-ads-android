package io.bidapp.networks.applovinmax


import android.app.Activity
import com.applovin.sdk.AppLovinPrivacySettings
import com.applovin.sdk.AppLovinSdk
import io.bidapp.sdk.BIDConsent
import io.bidapp.sdk.ConsentListener
import io.bidapp.sdk.protocols.BIDNetworkAdapterDelegateProtocol
import io.bidapp.sdk.protocols.BIDNetworkAdapterProtocol

@PublishedApi
internal class BIDApplovinMaxSDK(
    val adapter: BIDNetworkAdapterProtocol? = null,
    appId:String?,
    appSignature: String?
) : BIDNetworkAdapterDelegateProtocol, ConsentListener {

    override fun enableLogging(activity: Activity) {
        AppLovinSdk.getInstance(activity).settings.setVerboseLogging(true)
    }

    override fun setConsent(consent: BIDConsent, activity: Activity?) {
        if(consent.GDPR != null){
            AppLovinPrivacySettings.setHasUserConsent(consent.GDPR!!, activity)
        }
        if(consent.CCPA != null){
            AppLovinPrivacySettings.setDoNotSell(!consent.CCPA!!, activity)
        }
        if (consent.COPPA != null){
            AppLovinPrivacySettings.setIsAgeRestrictedUser(!consent.COPPA!!, activity)
        }
    }

    override fun enableTesting() {
    }

    override fun initializeSDK(activity: Activity) {
        if (isInitialized(activity) ||
            null == adapter ||
            adapter.initializationInProgress() ) {
            return
        }
        adapter.onInitializationStart()
        ApplovinInitializerMax.start(adapter,activity)
    }

    override fun isInitialized(activity: Activity): Boolean {
        return AppLovinSdk.getInstance(activity).isInitialized
    }

    override fun sharedSDK(): Any? {
        return null
    }
}