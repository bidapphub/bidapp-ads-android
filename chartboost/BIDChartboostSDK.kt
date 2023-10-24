package io.bidapp.networks.chartboost

import android.app.Activity
import android.util.Log
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


@PublishedApi
internal class BIDChartboostSDK(
    private val adapter: BIDNetworkAdapterProtocol?,
    val appId: String,
    val appSignature: String?
) : BIDNetworkAdapterDelegateProtocol, ConsentListener {

    val TAG = "Chartboost SDK"

    override fun setConsent(consent: BIDConsent, activity: Activity?) {
        if (consent.GDPR != null) {
            if (consent.GDPR == true) {
                val dataUseConsent = GDPR(GDPR.GDPR_CONSENT.BEHAVIORAL)
                activity?.applicationContext?.let {
                    Chartboost.addDataUseConsent(
                        it,
                        dataUseConsent
                    )
                }
            } else {
                val dataUseConsent = GDPR(GDPR.GDPR_CONSENT.NON_BEHAVIORAL)
                activity?.applicationContext?.let {
                    Chartboost.addDataUseConsent(
                        it,
                        dataUseConsent
                    )
                }
            }
            if (consent.CCPA != null) {
                if (consent.CCPA == true) {
                    val dataUseConsent = CCPA(CCPA.CCPA_CONSENT.OPT_IN_SALE)
                    activity?.applicationContext?.let {
                        Chartboost.addDataUseConsent(
                            it,
                            dataUseConsent
                        )
                    }
                } else {
                    val dataUseConsent = CCPA(CCPA.CCPA_CONSENT.OPT_OUT_SALE)
                    activity?.applicationContext?.let {
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
                    activity?.applicationContext?.let {
                        Chartboost.addDataUseConsent(
                            it,
                            dataUseConsent
                        )
                    }
                } else {
                    val dataUseConsent = COPPA(false)
                    activity?.applicationContext?.let {
                        Chartboost.addDataUseConsent(
                            it,
                            dataUseConsent
                        )
                    }
                }
            }
        }
    }

    override fun initializeSDK(activity: Activity) {
        if (isInitialized(activity) ||
            null == adapter ||
            adapter.initializationInProgress()
        ) {
            return
        }
        adapter.onInitializationStart()
        BIDLog.d(TAG, "Chartboost SDK Version - ${Chartboost.getSDKVersion()}")
        val initialization = runCatching {
            Chartboost.startWithAppId(
                activity.applicationContext, appId, appSignature!!
            ) { startError ->
                if (startError == null) {
                    this@BIDChartboostSDK.initializationComplete()
                } else this@BIDChartboostSDK.initializationFailed(startError.exception.toString())
            }
        }
        if (initialization.isFailure) initializationFailed("Chartboost initialization is failure")

    }


    fun initializationComplete() {
        BIDLog.d(TAG, "initialization complete")
        adapter?.onInitializationComplete(true, null)
    }

    fun initializationFailed(err: String) {
        BIDLog.d(TAG, "initialization failed. Error:$err")
        adapter?.onInitializationComplete(false, err)
    }

    override fun isInitialized(activity: Activity): Boolean {
        return Chartboost.isSdkStarted()
    }

    override fun enableTesting() {
        //enableTesting
    }

    override fun enableLogging(activity: Activity) {
        Chartboost.setLoggingLevel(LoggingLevel.ALL)
    }

    override fun sharedSDK(): Any? {
        return null
    }
}