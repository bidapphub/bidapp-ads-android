package io.bidapp.networks.yandex

import android.content.Context
import com.yandex.mobile.ads.common.MobileAds
import io.bidapp.sdk.BIDConsent
import io.bidapp.sdk.BIDLog
import io.bidapp.sdk.ConsentListener
import io.bidapp.sdk.protocols.BIDNetworkAdapterDelegateProtocol
import io.bidapp.sdk.protocols.BIDNetworkAdapterProtocol

@PublishedApi
internal class BIDYandexSDK(
    val adapter: BIDNetworkAdapterProtocol,
    val appId: String?,
    appSignature: String?
) : BIDNetworkAdapterDelegateProtocol, ConsentListener {

    val TAG = "Yandex SDK"
    var isInit = false


    override fun setConsent(consent: BIDConsent, context: Context?) {
        if(consent.COPPA != null){
            MobileAds.setAgeRestrictedUser(consent.COPPA!!)
        }
        if(consent.GDPR != null){
            MobileAds.setUserConsent(consent.GDPR!!);
        }
    }

    override fun initializeSDK(context: Context) {
        if (isInitialized(context) || adapter.initializationInProgress())
        {
            return
        }
        adapter.onInitializationStart()
        MobileAds.initialize(context) {
            isInit = true
            this.initializationComplete()
        }
    }

    override fun isInitialized(context: Context): Boolean {
        return isInit
    }

    override fun enableTesting() {
        debugOrTesting = true
    }

    override fun enableLogging(context: Context) {
        MobileAds.enableLogging(true)
        debugOrTesting = true
    }

    override fun sharedSDK(): Any? {
        return null
    }

    fun initializationComplete() {
        BIDLog.d(TAG, "Initialization complete")
        adapter.onInitializationComplete(true, null)
    }

    companion object {
        var debugOrTesting : Boolean? = null
    }
}