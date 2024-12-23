package io.bidapp.networks.inmobi

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.inmobi.compliance.InMobiPrivacyCompliance
import com.inmobi.sdk.InMobiSdk
import com.inmobi.sdk.SdkInitializationListener
import io.bidapp.sdk.BIDConsent
import io.bidapp.sdk.BIDLog
import io.bidapp.sdk.ConsentListener
import io.bidapp.sdk.protocols.BIDNetworkAdapterDelegateProtocol
import io.bidapp.sdk.protocols.BIDNetworkAdapterProtocol
import org.json.JSONObject
import java.lang.Error


internal const val ADAPTERVERSION = "2.2.5"
internal const val SDKVERSION = "10.8.0"
@PublishedApi
internal class BIDInMobiSDK(
    private val adapter: BIDNetworkAdapterProtocol? = null,
    val accountId: String? = null,
    appSignature: String?
) : BIDNetworkAdapterDelegateProtocol, ConsentListener {

    private val TAG = "InMobi SDK"
    var testMode = false
    private var consentObject : JSONObject = JSONObject()

    override fun initializeSDK(context: Context) {
        if (isInitialized(context) || adapter?.initializationInProgress() == true)
        {
            return
        }
        adapter?.onInitializationStart()
        if (accountId.isNullOrEmpty()){
            initializationFailed("InMobi accountId is null or empty")
            return
        }
        val handler = Handler(Looper.getMainLooper())
        val initializeSDK = Runnable{
            val consentObject = JSONObject()
            InMobiSdk.init(context, accountId, consentObject, object : SdkInitializationListener{
                override fun onInitializationComplete(error: Error?) {
                    if (error != null){
                        initializationFailed(error.message ?: "InMobi SDK initialize failed with unknown error")
                    }
                    else{
                        initializationComplete()
                    }
                }
            })
        }
        handler.post(initializeSDK)
    }

    private fun initializationComplete() {
        BIDLog.d(TAG, "Initialization complete")
        adapter?.onInitializationComplete(true, null)
    }

    private fun initializationFailed(err: String) {
        BIDLog.d(TAG, "Initialization failed. Error:$err")
        adapter?.onInitializationComplete(false, err)
    }

    override fun isInitialized(context: Context): Boolean {
        return InMobiSdk.isSDKInitialized()
    }

    override fun enableTesting() {
        testMode = true
    }

    override fun enableLogging(context: Context) {
        InMobiSdk.setLogLevel(InMobiSdk.LogLevel.DEBUG)
    }

    override fun sharedSDK(): Any? {
        return mapOf(
            "adapterVersion" to ADAPTERVERSION,
            "sdkVersion" to SDKVERSION
        )
    }

    override fun setConsent(consent: BIDConsent, context: Context?) {
        if (consent.GDPR != null){
            consentObject.put("partner_gdpr_consent_available", consent.GDPR!!)
            InMobiSdk.setPartnerGDPRConsent(consentObject)
        }
        if (consent.CCPA != null){
            InMobiPrivacyCompliance.setDoNotSell(!consent.CCPA!!)
        }
    }
}