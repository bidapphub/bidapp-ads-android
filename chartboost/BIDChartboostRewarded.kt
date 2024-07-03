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
    var isRewardGranted = false


    private fun init() {
        if (chartboostRewarded == null){
            return
        }
        if (location.isNullOrEmpty()){
            BIDLog.d(TAG, "Location is null or empty")
            return
        }
        chartboostRewarded = Rewarded(location, object : RewardedCallback {
            override fun onAdClicked(event: ClickEvent, error: ClickError?) {
                if (error == null) {
                    BIDLog.d(TAG, "ad clicked. Location: ($location)")
                    adapter?.onClick()
                    return
                }
                BIDLog.d(TAG, "ad clicked is failure. Location: ($location)")
            }

            override fun onAdDismiss(event: DismissEvent) {
                if (isRewardGranted){
                    adapter?.onReward()
                    isRewardGranted = false
                }
                adapter?.onHide()
                BIDLog.d(TAG, "ad hidden. Location: ($location)")
            }

            override fun onAdLoaded(event: CacheEvent, error: CacheError?) {
                if (error == null && event.ad.isCached()) {
                    BIDLog.d(TAG, "loaded Ad. Location: ($location)")
                    adapter?.onAdLoaded()
                } else {
                    BIDLog.d(
                        TAG,
                        "failed to receive ad error ${error?.code?.name.toString()} Location: ($location)"
                    )
                    adapter?.onAdFailedToLoadWithError(error?.code?.name.toString())
                }
            }

            override fun onAdRequestedToShow(event: ShowEvent) {
                BIDLog.d(TAG, "ad requested to show. Location: ($location)")
            }

            override fun onAdShown(event: ShowEvent, error: ShowError?) {
                if (error == null) {
                    BIDLog.d(TAG, "ad display. Location: ($location)")
                } else {
                    BIDLog.d(TAG, "ad failed to display ${error.exception?.message} Location: ($location)")
                    adapter?.onFailedToDisplay(error.exception?.message.toString())
                }
            }

            override fun onImpressionRecorded(event: ImpressionEvent) {
                BIDLog.d(TAG, "ad impression recorded. Location: ($location)")
                adapter?.onDisplay()
            }

            override fun onRewardEarned(event: RewardEvent) {
                BIDLog.d(TAG, "on reward earned. Location: ($location)")
                isRewardGranted = true
            }
        }, null)
    }

    override fun load(context: Any) {
        if (location.isNullOrEmpty()) {
            adapter?.onAdFailedToLoadWithError("Chartboost rewarded location is null or empty")
            return
        }
        if (chartboostRewarded == null) {
            init()
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
    }
}