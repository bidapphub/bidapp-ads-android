package io.bidapp.networks.chartboost

import android.content.Context
import com.chartboost.sdk.Chartboost
import com.chartboost.sdk.LoggingLevel
import com.chartboost.sdk.privacy.model.CCPA
import com.chartboost.sdk.privacy.model.COPPA
import com.chartboost.sdk.privacy.model.GDPR
import io.bidapp.sdk.BIDConsent
import io.bidapp.sdk.BIDLog
import io.bidapp.sdk.ConsentListener
import io.bidapp.sdk.protocols.BIDNetworkAdapterDelegateProtocol
import io.bidapp.sdk.protocols.BIDNetworkAdapterProtocol

internal const val ADAPTERVERSION = "2.3.0"
internal const val SDKVERSION = "9.8.2"

@PublishedApi
internal class BIDChartboostSDK(
    private val adapter: BIDNetworkAdapterProtocol?,
    private val appId: String?,
    private val appSignature: String?
) : BIDNetworkAdapterDelegateProtocol, ConsentListener {

    private val TAG = "Chartboost SDK"

    override fun setConsent(consent: BIDConsent, context: Context?) {
        if (consent.GDPR != null) {
            if (consent.GDPR == true) {
                val dataUseConsent = GDPR(GDPR.GDPR_CONSENT.BEHAVIORAL)
                context?.let {
                    Chartboost.addDataUseConsent(
                        it,
                        dataUseConsent
                    )
                }
            } else if (consent.GDPR == false) {
                val dataUseConsent = GDPR(GDPR.GDPR_CONSENT.NON_BEHAVIORAL)
                context?.let {
                    Chartboost.addDataUseConsent(
                        it,
                        dataUseConsent
                    )
                }
            }
            if (consent.CCPA != null) {
                if (consent.CCPA == true) {
                    val dataUseConsent = CCPA(CCPA.CCPA_CONSENT.OPT_IN_SALE)
                    context?.let {
                        Chartboost.addDataUseConsent(
                            it,
                            dataUseConsent
                        )
                    }
                } else if (consent.CCPA == false) {
                    val dataUseConsent = CCPA(CCPA.CCPA_CONSENT.OPT_OUT_SALE)
                    context?.let {
                        Chartboost.addDataUseConsent(
                            it,
                            dataUseConsent
                        )
                    }
                }
            }
            if (consent.COPPA != null) {
                if (consent.COPPA == true) {
                    val dataUseConsent = COPPA(true)
                    context?.let {
                        Chartboost.addDataUseConsent(
                            it,
                            dataUseConsent
                        )
                    }
                } else if (consent.COPPA == false) {
                    val dataUseConsent = COPPA(false)
                    context?.let {
                        Chartboost.addDataUseConsent(
                            it,
                            dataUseConsent
                        )
                    }
                }
            }
        }
    }

    override fun initializeSDK(context: Context) {
        if (isInitialized(context) ||
            null == adapter ||
            adapter.initializationInProgress()
        ) {
            return
        }
        if (appId.isNullOrEmpty() || appSignature.isNullOrEmpty()) {
            initializationFailed("Chartboost initialization is failure. appId or appSignature is null or empty")
            return
        }
        adapter.onInitializationStart()
        Chartboost.startWithAppId(
            context, appId, appSignature
        ) { startError ->
            if (startError == null) {
                this@BIDChartboostSDK.initializationComplete()
            } else this@BIDChartboostSDK.initializationFailed(startError.exception.toString())
        }
    }


    private fun initializationComplete() {
        BIDLog.d(TAG, "initialization complete")
        adapter?.onInitializationComplete(true, null)
    }

    private fun initializationFailed(err: String) {
        BIDLog.d(TAG, "initialization failed. Error:$err")
        adapter?.onInitializationComplete(false, err)
    }

    override fun isInitialized(context: Context): Boolean {
        return Chartboost.isSdkStarted()
    }

    override fun enableTesting() {}

    override fun enableLogging(context: Context) {
        Chartboost.setLoggingLevel(LoggingLevel.ALL)
    }

    override fun sharedSDK(): Any {
        return mapOf(
            "adapterVersion" to ADAPTERVERSION,
            "sdkVersion" to SDKVERSION
        )
    }
}