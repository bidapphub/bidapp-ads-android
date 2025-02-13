package io.bidapp.networks.liftoff


import android.content.Context
import com.vungle.ads.InitializationListener
import com.vungle.ads.VungleAds
import com.vungle.ads.VungleError
import com.vungle.ads.VunglePrivacySettings
import io.bidapp.sdk.BIDConsent
import io.bidapp.sdk.BIDLog
import io.bidapp.sdk.BidappAds
import io.bidapp.sdk.ConsentListener
import io.bidapp.sdk.protocols.BIDNetworkAdapterDelegateProtocol
import io.bidapp.sdk.protocols.BIDNetworkAdapterProtocol


internal const val ADAPTERVERSION = "2.3.5"
internal const val SDKVERSION = "7.4.3"
@PublishedApi
internal class BIDLiftoffSDK(
    private val adapter: BIDNetworkAdapterProtocol,
    private val appId: String?,
    appSignature: String?
) : BIDNetworkAdapterDelegateProtocol, ConsentListener {

    private val TAG = "Liftoff SDK adapter"


    override fun enableLogging(context: Context) {
    }

    override fun setConsent(consent: BIDConsent, context: Context?) {
        if (consent.GDPR != null) {
            VunglePrivacySettings.setGDPRStatus(consent.GDPR!!, "1.0.0")
        }
        if (consent.CCPA != null) {
            VunglePrivacySettings.setCCPAStatus(consent.CCPA!!)
        }
        if (consent.COPPA != null) {
            coppa = consent.COPPA
            VunglePrivacySettings.setCOPPAStatus(consent.COPPA!!)
        }
    }

    override fun enableTesting() {
        testMode = true
    }

    override fun initializeSDK(context: Context) {
        isCompatibility()
        if (isInitialized(context) || adapter.initializationInProgress())
        {
            return
        }
        if (appId.isNullOrEmpty()){
            initializationFailed("Liftoff appId is null or empty")
            return
        }
        adapter.onInitializationStart()
        VungleAds.setIntegrationName(VungleAds.WrapperFramework.vunglehbs, "51.0.0")
        VungleAds.init(context, appId, object : InitializationListener {
            override fun onError(vungleError: VungleError) {
                this@BIDLiftoffSDK.initializationFailed(vungleError.errorMessage)
            }
            override fun onSuccess() {
                this@BIDLiftoffSDK.initializationComplete()
            }
        })
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
        return VungleAds.isInitialized()
    }

    override fun sharedSDK(): Any? {
        return mapOf(
            "adapterVersion" to ADAPTERVERSION,
            "sdkVersion" to SDKVERSION
        )
    }
    private fun isCompatibility(){
       try {
           val majorVersion = BidappAds.VERSION.firstOrNull { it.isDigit() }?.toString()?.toIntOrNull()
           if (majorVersion != null && majorVersion < 2)
               throw IllegalStateException("The adapter version is not compatible with the Bidapp SDK. Please update the Bidapp SDK to the latest version")
       } catch (e : Exception){
               throw IllegalStateException("The adapter version is not compatible with the Bidapp SDK. Please update the Bidapp SDK to the latest version")
       }

    }
    companion object{
        var testMode : Boolean? = false
        var coppa : Boolean? = false
    }
}