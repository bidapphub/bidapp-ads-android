package io.bidapp.networks.applovin


import android.content.Context
import android.content.pm.PackageManager
import com.applovin.sdk.AppLovinPrivacySettings
import com.applovin.sdk.AppLovinSdk
import com.applovin.sdk.AppLovinSdkSettings
import io.bidapp.sdk.BIDConsent
import io.bidapp.sdk.ConsentListener
import io.bidapp.sdk.protocols.BIDNetworkAdapterDelegateProtocol
import io.bidapp.sdk.protocols.BIDNetworkAdapterProtocol

@PublishedApi
internal class BIDApplovinSDK(
    private val adapter: BIDNetworkAdapterProtocol? = null,
    private val appId : String?,
    appSignature: String?
) : BIDNetworkAdapterDelegateProtocol, ConsentListener {

    override fun enableLogging(context: Context) {
        appLovinGetInstanceSDK(context.applicationContext).settings.setVerboseLogging(true)
    }

    override fun enableTesting() {
    }

    init {
        BIDApplovinSDK.appId = appId
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
        if (appId == null && !getApplovinKeyFromManifest(context)) {
            adapter?.onInitializationComplete(false, "AppLovin App Id is null")
            return
        }
        if (isInitialized(context) ||
            null == adapter ||
            adapter.initializationInProgress() ) {
            return
        }
        adapter.onInitializationStart()
        ApplovinInitializer.start(adapter,context)
    }

    override fun isInitialized(context: Context): Boolean {
        return appLovinGetInstanceSDK(context.applicationContext).isInitialized
    }

    override fun sharedSDK(): Any? {
        return null
    }

    companion object{
        var appId : String? = null

        fun appLovinGetInstanceSDK(context: Context) : AppLovinSdk {
            if (getApplovinKeyFromManifest(context) || this.appId == null) {
                return AppLovinSdk.getInstance(context)
            }
            return AppLovinSdk.getInstance(appId, AppLovinSdkSettings(context), context)
        }

        fun getApplovinKeyFromManifest(context: Context): Boolean {
            try {
                val appInfo = context.packageManager.getApplicationInfo(context.applicationContext.packageName, PackageManager.GET_META_DATA)
                val metaData = appInfo.metaData
                if (metaData?.getString("applovin.sdk.key") != null)
                    return true
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
                return false
            }
            return false
        }
    }

}