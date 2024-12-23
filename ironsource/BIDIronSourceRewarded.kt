package io.bidapp.networks.ironsource

import android.app.Activity
import android.util.Log
import com.ironsource.mediationsdk.IronSource
import com.ironsource.mediationsdk.demandOnly.ISDemandOnlyRewardedVideoListener
import com.ironsource.mediationsdk.logger.IronSourceError
import io.bidapp.networks.ironsource.BIDIronSourceInterstitial.Companion
import io.bidapp.sdk.BIDLog
import io.bidapp.sdk.protocols.BIDFullscreenAdapterDelegateProtocol
import io.bidapp.sdk.protocols.BIDFullscreenAdapterProtocol

@PublishedApi
internal class BIDIronSourceRewarded(
    val adapter: BIDFullscreenAdapterProtocol? = null,
    val instanceId: String? = null,
    isReward: Boolean
) : BIDFullscreenAdapterDelegateProtocol {
    val TAG = "Rewarded IronSource"
    private var rewardedAdListener: RewardedAdListener? = null


    override fun revenue(): Double? {
        return null
    }

    override fun load(context : Any) {
        if (context as? Activity == null){
            adapter?.onAdFailedToLoadWithError("IronSource rewarded load is failed. Activity is null")
            return
        }
        if (dontLoad) {
            adapter?.onAdFailedToLoadWithError("IronSource rewarded load is failed")
            return
        }
        if (instanceId== null){
            adapter?.onAdFailedToLoadWithError("IronSource rewarded load is failed. Instance ID is null")
            return
        }
        rewardedAdListener = RewardedAdListener(TAG, adapter, instanceId)
        IronSource.setISDemandOnlyRewardedVideoListener(rewardedAdListener)
        IronSource.loadISDemandOnlyRewardedVideo(context, instanceId)
    }

    override fun show(activity: Activity?) {
        if (!IronSource.isISDemandOnlyRewardedVideoAvailable(instanceId)){
            adapter?.onFailedToDisplay("IronSource rewarded showing is failure")
            return
        }
        IronSource.showISDemandOnlyRewardedVideo(instanceId)
    }

    override fun activityNeededForShow(): Boolean {
        return false
    }

    override fun activityNeededForLoad(): Boolean {
        return true
    }

    override fun readyToShow(): Boolean {
       return IronSource.isISDemandOnlyRewardedVideoAvailable(instanceId)
    }

    override fun shouldWaitForAdToDisplay(): Boolean {
        return true
    }

    override fun destroy() {
        dontLoad = false
        rewardedAdListener = null
        IronSource.removeRewardedVideoListener()
    }

    private class RewardedAdListener(
       private val tag : String,
       private val adapter: BIDFullscreenAdapterProtocol?,
       private val instanceId: String?
    ) :
        ISDemandOnlyRewardedVideoListener {
        var isRewardGranted = false
        override fun onRewardedVideoAdLoadSuccess(p0: String?) {
            BIDLog.d(tag, "Rewarded load $instanceId")
            adapter?.onAdLoaded()
        }

        override fun onRewardedVideoAdLoadFailed(p0: String?, p1: IronSourceError?) {
            val errorMessage = p1?.errorMessage ?: p0 ?: "Unknown Error"
            BIDLog.d(tag, "Load rewarded ad failed $instanceId Error: $errorMessage")
            adapter?.onAdFailedToLoadWithError(errorMessage)
        }

        override fun onRewardedVideoAdOpened(p0: String?) {
            BIDLog.d(tag, "Rewarded ad impression $instanceId")
            adapter?.onDisplay()
            dontLoad = true
        }

        override fun onRewardedVideoAdShowFailed(p0: String?, p1: IronSourceError?) {
            val errorMessage = p1?.errorMessage ?: p0 ?: "Unknown Error"
            BIDLog.d(tag, "Rewarded ad display failed $instanceId Error: $errorMessage")
            adapter?.onFailedToDisplay(errorMessage)
            dontLoad = false
        }

        override fun onRewardedVideoAdClicked(p0: String?) {
            BIDLog.d(tag, "Rewarded ad clicked $instanceId")
            adapter?.onClick()
        }

        override fun onRewardedVideoAdRewarded(p0: String?) {
            BIDLog.d(tag, "Ad rewarded $instanceId")
            isRewardGranted = true
        }

        override fun onRewardedVideoAdClosed(p0: String?) {
            BIDLog.d(tag, "Rewarded ad hide. $instanceId")
            if (isRewardGranted) {
                adapter?.onReward()
                isRewardGranted = false
            }
            dontLoad = false
            adapter?.onHide()
        }

    }

    companion object {
        var dontLoad = false
    }

}