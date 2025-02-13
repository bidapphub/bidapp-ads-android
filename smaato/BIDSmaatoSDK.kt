package io.bidapp.networks.smaato

import android.app.Application
import android.content.Context
import com.smaato.sdk.core.Config
import com.smaato.sdk.core.SmaatoSdk
import io.bidapp.sdk.BIDConsent
import io.bidapp.sdk.BIDLog
import io.bidapp.sdk.ConsentListener
import io.bidapp.sdk.protocols.BIDNetworkAdapterDelegateProtocol
import io.bidapp.sdk.protocols.BIDNetworkAdapterProtocol


internal const val ADAPTERVERSION = "2.4.0"
internal const val SDKVERSION = "22.7.2"
@PublishedApi
internal class BIDSmaatoSDK (
    private val adapter: BIDNetworkAdapterProtocol,
    private val appId: String?,
    appSignature: String?
) : BIDNetworkAdapterDelegateProtocol, ConsentListener {
    private val TAG = "Smaato SDK"

    override fun initializeSDK(context: Context) {
        if (isInitialized(context) || adapter.initializationInProgress())
        {
            return
        }
        if ((context as? Application) == null){
            initializationFailed("Smaato cast application context in Application failed")
            return
        }
        if (appId.isNullOrEmpty()){
            initializationFailed("Smaato appId is null or empty")
            return
        }
        val config = Config.builder().enableLogging(logging).setHttpsOnly(false).build()
        SmaatoSdk.init(context, config, appId, object : SmaatoSdk.SmaatoSdkInitialisationListener{
            override fun onInitialisationSuccess() {
                initializationComplete()
                COPPA?.let { SmaatoSdk.setCoppa(it) }
            }

            override fun onInitialisationFailure(p0: String?) {
                initializationFailed(p0 ?: "Smaato initialize failed with unknown error")
            }

        })

        COPPA?.let { SmaatoSdk.setCoppa(it) }

    }

    fun initializationComplete() {
        BIDLog.d(TAG, "Initialization complete")
        adapter.onInitializationComplete(true, null)
    }

    fun initializationFailed(err: String) {
        BIDLog.d(TAG, "Initialization failed. Error:$err")
        adapter.onInitializationComplete(false, err)
    }

    override fun isInitialized(context: Context): Boolean {
        return SmaatoSdk.isSmaatoSdkInitialised()
    }

    override fun enableTesting() {
        testMode = true
    }

    override fun enableLogging(context: Context) {
        logging = true
    }

    override fun sharedSDK(): Any? {
        return mapOf(
            "adapterVersion" to ADAPTERVERSION,
            "sdkVersion" to SDKVERSION
        )
    }

    override fun setConsent(consent: BIDConsent, context: Context?) {
        val shared = context?.getSharedPreferences(context.packageName, Context.MODE_PRIVATE)
        val editor = shared?.edit()
        if (consent.COPPA != null){
            COPPA = consent.COPPA
            SmaatoSdk.setCoppa(consent.COPPA!!)
        }
        if (consent.GDPR != null){
            GDPR = consent.GDPR
            if (shared == editor) {
                BIDLog.e(TAG, "Set Smaato GDPR is failed context is null")
                return
            }
            editor?.putString("IABTCF_gdprApplies", if (consent.GDPR == true) "1" else "0")?.apply()
        }
        if (consent.CCPA != null){
            if (shared == editor) {
                BIDLog.e(TAG, "Set Smaato CCPA is failed context is null")
                return
            }
            if (consent.CCPA == true) {
                CCPA = consent.CCPA
                editor?.putString("IABUSPrivacy_String", "iab string")?.apply()
            }
            else {
                editor?.remove("IABUSPrivacy_String")?.apply()
            }
        }

    }

    companion object
    {
        var COPPA : Boolean? = null
        var GDPR : Boolean? = null
        var CCPA : Boolean? = null
        var logging = false
        var testMode = false
    }
}