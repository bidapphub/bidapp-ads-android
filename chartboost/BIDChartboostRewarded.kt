package io.bidapp.networks.chartboost

import android.app.Activity
import android.util.Log
import com.chartboost.sdk.ads.Interstitial
import com.chartboost.sdk.ads.Rewarded
import com.chartboost.sdk.callbacks.InterstitialCallback
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
    location: String? = null,
    var isReward: Boolean
) :
    BIDFullscreenAdapterDelegateProtocol {
    val TAG = "Rewarded Chartboost"
    var chartboostRewarded: Rewarded? = null


    init {
        chartboostRewarded = location?.let {
            Rewarded(it, object : RewardedCallback {
                override fun onAdClicked(event: ClickEvent, error: ClickError?) {
                    BIDLog.d(TAG, "ad clicked")
                    adapter?.onClick()
                }

                override fun onAdDismiss(event: DismissEvent) {
                    adapter?.onHide()
                    BIDLog.d(TAG, "ad hidden")
                }

                override fun onAdLoaded(event: CacheEvent, error: CacheError?) {
                    if (error == null && event.ad.isCached()) {
                        BIDLog.d(TAG, "loaded Ad")
                        adapter?.onAdLoaded()
                    } else {
                        BIDLog.d(
                            TAG,
                            "failed to receive ad error ${error?.exception?.message.toString()}"
                        )
                        adapter?.onAdFailedToLoadWithError(error?.exception?.message.toString())
                    }
                }

                override fun onAdRequestedToShow(event: ShowEvent) {
                    BIDLog.d(TAG, "on ad requested to show")
                }

                override fun onAdShown(event: ShowEvent, error: ShowError?) {
                    if (error == null) {
                        adapter?.onDisplay()
                        BIDLog.d(TAG, "on display")
                    } else {
                        BIDLog.d(TAG, "failed to display ${error.exception?.message}")
                        adapter?.onFailedToDisplay(error.exception?.message.toString())
                    }
                }

                override fun onImpressionRecorded(event: ImpressionEvent) {
                }

                override fun onRewardEarned(event: RewardEvent) {
                    BIDLog.d(TAG, "on reward earned")
                    adapter?.onReward()
                }
            }, null)
        }
    }

    override fun load(context: Any) {
        val load = runCatching {
            chartboostRewarded!!.cache()
        }
        if (load.isFailure) adapter?.onAdFailedToLoadWithError("Chart boost loading rewarded is failure")
    }

    override fun show(activity: Activity?) {
        if (chartboostRewarded != null && chartboostRewarded!!.isCached()) {
            chartboostRewarded!!.show()
        } else adapter?.onFailedToDisplay("Chart boost showing rewarded is failure")
    }

    override fun activityNeededForShow(): Boolean {
        return false
    }

    override fun activityNeededForLoad(): Boolean {
        return false
    }

    override fun readyToShow(): Boolean {
        return chartboostRewarded != null && chartboostRewarded!!.isCached()
    }

    override fun shouldWaitForAdToDisplay(): Boolean {
        return true
    }

    override fun revenue(): Double? {
        return null
    }
}