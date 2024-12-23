package io.bidapp.networks.chartboost

import android.app.Activity
import com.chartboost.sdk.ads.Rewarded
import com.chartboost.sdk.callbacks.RewardedCallback
import com.chartboost.sdk.events.CacheError
import com.chartboost.sdk.events.CacheEvent
import com.chartboost.sdk.events.ClickError
import com.chartboost.sdk.events.ClickEvent
import com.chartboost.sdk.events.DismissEvent
import com.chartboost.sdk.events.ImpressionEvent
import com.chartboost.sdk.events.RewardEvent
import com.chartboost.sdk.events.ShowError
import com.chartboost.sdk.events.ShowEvent
import io.bidapp.sdk.BIDLog
import io.bidapp.sdk.protocols.BIDFullscreenAdapterDelegateProtocol
import io.bidapp.sdk.protocols.BIDFullscreenAdapterProtocol

@PublishedApi
internal class BIDChartboostRewarded(
    val adapter: BIDFullscreenAdapterProtocol? = null,
    val location: String? = null,
    var isReward: Boolean
) :
    BIDFullscreenAdapterDelegateProtocol {
    val TAG = "Rewarded Chartboost"
    var chartboostRewarded: Rewarded? = null
    private var rewardedAdListener: RewardedAdListener? = null

    override fun load(context: Any) {
        if (location.isNullOrEmpty()) {
            adapter?.onAdFailedToLoadWithError("Chartboost rewarded location is null or empty")
            return
        }
        if (chartboostRewarded == null) {
            rewardedAdListener = RewardedAdListener(TAG, adapter, location)
            chartboostRewarded = Rewarded(location, rewardedAdListener!! , null)
        }
        chartboostRewarded?.cache()
    }

    override fun show(activity: Activity?) {
        if (chartboostRewarded == null || chartboostRewarded?.isCached() == false) {
            adapter?.onFailedToDisplay("Chart boost showing rewarded is failure")
            return
        }
        chartboostRewarded?.show()
    }

    override fun activityNeededForShow(): Boolean {
        return false
    }

    override fun activityNeededForLoad(): Boolean {
        return false
    }

    override fun readyToShow(): Boolean {
        return chartboostRewarded?.isCached() ?: false
    }

    override fun shouldWaitForAdToDisplay(): Boolean {
        return true
    }

    override fun revenue(): Double? {
        return null
    }

    override fun destroy() {
        chartboostRewarded?.clearCache()
        chartboostRewarded = null
        rewardedAdListener = null
    }

    private class RewardedAdListener (
        private val tag : String,
        private val adapter: BIDFullscreenAdapterProtocol?,
        private val location: String?
    ) : RewardedCallback {
        var isRewardGranted = false
        override fun onAdClicked(event: ClickEvent, error: ClickError?) {
            if (error == null) {
                BIDLog.d(tag, "Ad clicked. Location: ($location)")
                adapter?.onClick()
                return
            }
            BIDLog.d(tag, "Ad clicked is failure. Location: ($location)")
        }

        override fun onAdDismiss(event: DismissEvent) {
            if (isRewardGranted){
                adapter?.onReward()
                isRewardGranted = false
            }
            adapter?.onHide()
            BIDLog.d(tag, "Ad hidden. Location: ($location)")
        }

        override fun onAdLoaded(event: CacheEvent, error: CacheError?) {
            if (error == null && event.ad.isCached()) {
                BIDLog.d(tag, "Ad loaded. Location: ($location)")
                adapter?.onAdLoaded()
            } else {
                BIDLog.d(
                    tag,
                    "Failed to receive ad error ${error?.code?.name.toString()} Location: ($location)"
                )
                adapter?.onAdFailedToLoadWithError(error?.code?.name.toString())
            }
        }

        override fun onAdRequestedToShow(event: ShowEvent) {
            BIDLog.d(tag, "Ad requested to show. Location: ($location)")
        }

        override fun onAdShown(event: ShowEvent, error: ShowError?) {
            if (error == null) {
                BIDLog.d(tag, "Ad display. Location: ($location)")
            } else {
                BIDLog.d(tag, "Ad failed to display ${error.exception?.message} Location: ($location)")
                adapter?.onFailedToDisplay(error.exception?.message.toString())
            }
        }

        override fun onImpressionRecorded(event: ImpressionEvent) {
            BIDLog.d(tag, "Ad impression recorded. Location: ($location)")
            adapter?.onDisplay()
        }

        override fun onRewardEarned(event: RewardEvent) {
            BIDLog.d(tag, "On reward earned. Location: ($location)")
            isRewardGranted = true
        }
    }

}