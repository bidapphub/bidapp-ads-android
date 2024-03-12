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
    val TAG = "Rewarded MyTarget"
    private var ads: RewardedAd? = null
    var isAdsReady = false
    var isRewardGranted = false

    val callback = object : RewardedAd.RewardedAdListener {
        override fun onLoad(p0: RewardedAd) {
            isAdsReady = true
            BIDLog.d(TAG, "on ad load $slotId")
            adapter?.onAdLoaded()
        }

        override fun onNoAd(p0: IAdLoadingError, p1: RewardedAd) {
            BIDLog.d(TAG, "onError $slotId exception: ${p0.message}")
            adapter?.onAdFailedToLoadWithError(p0.message)
        }

        override fun onClick(p0: RewardedAd) {
            BIDLog.d(TAG, "on ad click. $slotId")
            adapter?.onClick()
        }

        override fun onDismiss(p0: RewardedAd) {
            BIDLog.d(TAG, "on ad dismiss. $slotId")
            if (isRewardGranted) {
                adapter?.onReward()
                isRewardGranted = false
            }
            adapter?.onHide()
        }

        override fun onReward(p0: Reward, p1: RewardedAd) {
            BIDLog.d(TAG, "on ad rewarded $slotId")
            isRewardGranted = true
        }


        override fun onDisplay(p0: RewardedAd) {
            BIDLog.d(TAG, "on ad impression $slotId")
            adapter?.onDisplay()
        }

    }

    override fun load(context: Any) {
        if (context as? Context == null) {
            adapter?.onAdFailedToLoadWithError("MyTarget fullscreen loading error")
            return
        }
        if (slotIdToInt == null) {
            adapter?.onAdFailedToLoadWithError("MyTarget fullscreen slotId is null or incorrect format")
            return
        }
        isAdsReady = false
        if (ads == null) {
            ads = RewardedAd(slotIdToInt, context)
        }
        ads?.listener = callback
        ads?.load()

    }

    override fun show(activity: Activity?) {
        if (ads == null && !isAdsReady) {
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
        return isAdsReady
    }

    override fun shouldWaitForAdToDisplay(): Boolean {
        return true
    }

    override fun destroy() {

    }


}