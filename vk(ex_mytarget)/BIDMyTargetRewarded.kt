package io.bidapp.networks.mytarget

import android.app.Activity
import android.content.Context
import com.my.target.ads.Reward
import com.my.target.ads.RewardedAd
import com.my.target.common.models.IAdLoadingError
import io.bidapp.sdk.BIDLog
import io.bidapp.sdk.protocols.BIDFullscreenAdapterDelegateProtocol
import io.bidapp.sdk.protocols.BIDFullscreenAdapterProtocol

class BIDMyTargetRewarded(
    val adapter: BIDFullscreenAdapterProtocol? = null,
    val slotId: String? = null,
    isReward: Boolean
) : BIDFullscreenAdapterDelegateProtocol {

    private val slotIdToInt = slotId?.toIntOrNull()
    private val TAG = "Rewarded MyTarget"
    private var ads: RewardedAd? = null
    private var rewardedAdListener: RewardedAdListener? = null


    override fun load(context: Any) {
        if (context as? Context == null) {
            adapter?.onAdFailedToLoadWithError("MyTarget fullscreen loading error")
            return
        }
        if (slotIdToInt == null) {
            adapter?.onAdFailedToLoadWithError("MyTarget fullscreen slotId is null or incorrect format")
            return
        }
        rewardedAdListener = RewardedAdListener(TAG, adapter, slotId)
        if (ads == null) {
            ads = RewardedAd(slotIdToInt, context)
        }
        ads?.listener = rewardedAdListener
        ads?.load()

    }

    override fun show(activity: Activity?) {
        if (ads == null && rewardedAdListener?.isAdsReady != true) {
            adapter?.onFailedToDisplay("Failed to display")
            return
        }
        ads?.show()
    }

    override fun activityNeededForShow(): Boolean {
        return false
    }

    override fun activityNeededForLoad(): Boolean {
        return false
    }

    override fun readyToShow(): Boolean {
        return rewardedAdListener?.isAdsReady ?: false
    }

    override fun shouldWaitForAdToDisplay(): Boolean {
        return true
    }

    override fun destroy() {
        ads?.destroy()
        ads = null
        rewardedAdListener = null
    }

    private class RewardedAdListener (
        private val tag : String,
        private val adapter: BIDFullscreenAdapterProtocol?,
        private val slotId: String?
    ) : RewardedAd.RewardedAdListener {
        var isAdsReady = false
        var isRewardGranted = false
        override fun onLoad(p0: RewardedAd) {
            isAdsReady = true
            BIDLog.d(tag, "Ad load $slotId")
            adapter?.onAdLoaded()
        }

        override fun onNoAd(p0: IAdLoadingError, p1: RewardedAd) {
            BIDLog.d(tag, "On error $slotId exception: ${p0.message}")
            adapter?.onAdFailedToLoadWithError(p0.message)
        }

        override fun onClick(p0: RewardedAd) {
            BIDLog.d(tag, "Ad click. $slotId")
            adapter?.onClick()
        }

        override fun onFailedToShow(p0: RewardedAd) {
            BIDLog.d(tag, "Ad failed to display $slotId")
            adapter?.onFailedToDisplay("on ad failed to display")
        }

        override fun onDismiss(p0: RewardedAd) {
            BIDLog.d(tag, "Ad dismiss. $slotId")
            if (isRewardGranted) {
                adapter?.onReward()
                isRewardGranted = false
            }
            adapter?.onHide()
        }

        override fun onReward(p0: Reward, p1: RewardedAd) {
            BIDLog.d(tag, "Ad rewarded $slotId")
            isRewardGranted = true
        }


        override fun onDisplay(p0: RewardedAd) {
            BIDLog.d(tag, "Ad impression $slotId")
            adapter?.onDisplay()
        }
    }

}