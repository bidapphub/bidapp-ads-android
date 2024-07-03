package io.bidapp.networks.mytarget

import android.content.Context
import com.my.target.common.MyTargetManager
import com.my.target.common.MyTargetPrivacy
import io.bidapp.sdk.BIDConsent
import io.bidapp.sdk.BIDLog
import io.bidapp.sdk.ConsentListener
import io.bidapp.sdk.protocols.BIDNetworkAdapterDelegateProtocol
import io.bidapp.sdk.protocols.BIDNetworkAdapterProtocol

internal const val ADAPTERVERSION = "2.0.0"
internal const val SDKVERSION = "5.21.0"
@PublishedApi
internal class BIDMyTargetSDK(
    private val adapter: BIDNetworkAdapterProtocol,
    val appId: String?,
    appSignature: String?
) : BIDNetworkAdapterDelegateProtocol, ConsentListener {

    private val TAG = "MyTarget SDK"
    private var isInit = false


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
        return mapOf(
            "adapterVersion" to ADAPTERVERSION,
            "sdkVersion" to SDKVERSION
        )
    }


    private fun initializationComplete() {
        BIDLog.d(TAG, "Initialization complete")
        adapter.onInitializationComplete(true, null)
    }

    private fun initializationFailed(err: String) {
        BIDLog.d(TAG, "Initialization failed.")
        adapter.onInitializationComplete(false, err)
    }
}