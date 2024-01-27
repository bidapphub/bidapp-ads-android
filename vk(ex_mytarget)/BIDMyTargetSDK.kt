package io.bidapp.networks.mytarget

import android.content.Context
import com.my.target.common.MyTargetConfig
import com.my.target.common.MyTargetManager
import com.my.target.common.MyTargetPrivacy
import io.bidapp.sdk.BIDConsent
import io.bidapp.sdk.BIDLog
import io.bidapp.sdk.ConsentListener
import io.bidapp.sdk.protocols.BIDNetworkAdapterDelegateProtocol
import io.bidapp.sdk.protocols.BIDNetworkAdapterProtocol

@PublishedApi
internal class BIDMyTargetSDK(
    val adapter: BIDNetworkAdapterProtocol,
    val appId: String?,
    appSignature: String?
) : BIDNetworkAdapterDelegateProtocol, ConsentListener {

    val TAG = "MyTarget SDK"
    var isInit = false


    override fun setConsent(consent: BIDConsent, context: Context?) {
       if(consent.COPPA != null){
           MyTargetPrivacy.setUserAgeRestricted(consent.COPPA!!)
        }
        if(consent.GDPR != null){
            MyTargetPrivacy.setUserConsent(consent.GDPR!!);
        }
        if (consent.CCPA != null){
            MyTargetPrivacy.setCcpaUserConsent(consent.CCPA!!)
        }
    }

    override fun initializeSDK(context: Context) {
         if (isInitialized(context) || adapter.initializationInProgress())
        {
            return
        }
        adapter.onInitializationStart()
        MyTargetManager.initSdk(context)
        if (MyTargetManager.isSdkInitialized()) {
            isInit = true
            this.initializationComplete()
        }
        else {
            isInit = false
            this.initializationFailed("MyTarget initialization is failure")
        }
    }

    override fun isInitialized(context: Context): Boolean {
       return isInit
    }

    override fun enableTesting() {
        MyTargetManager.setDebugMode(true)
    }

    override fun enableLogging(context: Context) {}

    override fun sharedSDK(): Any? {
        return null
    }


    fun initializationComplete() {
        BIDLog.d(TAG, "Initialization complete")
        adapter.onInitializationComplete(true, null)
    }

    fun initializationFailed(err: String) {
        BIDLog.d(TAG, "Initialization failed.")
        adapter.onInitializationComplete(false, err)
    }
}