package io.bidapp.networks.ironsource

import android.app.Activity
import com.ironsource.mediationsdk.IronSource
import com.ironsource.mediationsdk.demandOnly.ISDemandOnlyInterstitialListener
import com.ironsource.mediationsdk.logger.IronSourceError
import io.bidapp.sdk.BIDLog
import io.bidapp.sdk.protocols.BIDFullscreenAdapterDelegateProtocol
import io.bidapp.sdk.protocols.BIDFullscreenAdapterProtocol

@PublishedApi
internal class BIDIronSourceInterstitial(
    private val adapter: BIDFullscreenAdapterProtocol? = null,
    private val instanceId: String? = null
) : BIDFullscreenAdapterDelegateProtocol {
    val TAG = "Interstitial IronSource"

    override fun revenue(): Double? {
        return null
    }

    override fun load(context: Any) {
        if (context as? Activity == null) {
            adapter?.onAdFailedToLoadWithError("IronSource interstitial load is failed. Activity is null")
            return
        }
        if (instanceId == null) {
            adapter?.onAdFailedToLoadWithError("IronSource interstitial load is failed. Instance ID is null")
            return
        }
        if (!canLoadInterstitial(instanceId)){
            adapter?.onAdFailedToLoadWithError("Instance is busy")
            return
        }
         val interstitialAdListener = InterstitialAdListener(TAG, adapter, instanceId)
         IronSource.setISDemandOnlyInterstitialListener(interstitialAdListener)
         IronSource.loadISDemandOnlyInterstitial(context, instanceId)
    }

    override fun show(activity: Activity?) {
        if (!IronSource.isISDemandOnlyInterstitialReady(instanceId)) {
            adapter?.onFailedToDisplay(
                "IronSource interstitial showing is failure is ad not ready"
            )
            return
        }
        IronSource.showISDemandOnlyInterstitial(instanceId)
    }

    override fun activityNeededForShow(): Boolean {
        return false
    }

    override fun activityNeededForLoad(): Boolean {
        return true
    }

    override fun readyToShow(): Boolean { return IronSource.isISDemandOnlyInterstitialReady(instanceId)
    }

    override fun shouldWaitForAdToDisplay(): Boolean {
        return true
    }

    override fun destroy() {
       currentInterstitialInstance = null
    }

    internal class InterstitialAdListener(
        private val tag: String,
        val adapter: BIDFullscreenAdapterProtocol?,
        private val instanceId: String?
    ) :
        ISDemandOnlyInterstitialListener {
        override fun onInterstitialAdReady(p0: String?) {
            BIDLog.d(tag, "Interstitial ad load $instanceId")
            adapter?.onAdLoaded()
        }

        override fun onInterstitialAdLoadFailed(p0: String?, p1: IronSourceError?) {
            val errorMessage = p1?.errorMessage ?: p0 ?: "Unknown Error"
            BIDLog.d(tag, "Load interstitial ad failed $instanceId Error: $errorMessage")
            adapter?.onAdFailedToLoadWithError(errorMessage)
        }

        override fun onInterstitialAdOpened(p0: String?) {
            BIDLog.d(tag, "Interstitial ad impression $instanceId")
            adapter?.onDisplay()
        }

        override fun onInterstitialAdShowFailed(p0: String?, p1: IronSourceError?) {
            val errorMessage = p1?.errorMessage ?: p0 ?: "Unknown Error"
            BIDLog.d(tag, "Interstitial ad display failed $instanceId Error: $errorMessage")
            adapter?.onFailedToDisplay(errorMessage)
        }

        override fun onInterstitialAdClicked(p0: String?) {
            BIDLog.d(tag, "Interstitial ad clicked $instanceId")
            adapter?.onClick()
        }

        override fun onInterstitialAdClosed(p0: String?) {
            BIDLog.d(tag, "Interstitial ad hide $instanceId")
            adapter?.onHide()
        }

    }

    internal companion object {
        var currentInterstitialInstance : String? = null

        fun canLoadInterstitial(instanceId: String?) : Boolean{
            if (currentInterstitialInstance == null) {
                currentInterstitialInstance = instanceId
                return true
            }
            return false
        }
    }

}