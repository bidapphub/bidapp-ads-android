package io.bidapp.networks.chartboost

import android.app.Activity
import android.util.Log
import com.chartboost.sdk.ads.Interstitial
import com.chartboost.sdk.callbacks.InterstitialCallback
import com.chartboost.sdk.events.CacheError
import com.chartboost.sdk.events.CacheEvent
import com.chartboost.sdk.events.ClickError
import com.chartboost.sdk.events.ClickEvent
import com.chartboost.sdk.events.DismissEvent
import com.chartboost.sdk.events.ImpressionEvent
import com.chartboost.sdk.events.ShowError
import com.chartboost.sdk.events.ShowEvent
import io.bidapp.sdk.BIDLog
import io.bidapp.sdk.protocols.BIDFullscreenAdapterDelegateProtocol
import io.bidapp.sdk.protocols.BIDFullscreenAdapterProtocol

@PublishedApi
internal class BIDChartboostInterstitial(
    val adapter: BIDFullscreenAdapterProtocol? = null,
    location: String? = null
) :
    BIDFullscreenAdapterDelegateProtocol {

    val TAG = "interstitial Chartboost"
    var chartboostInterstitial: Interstitial? = null

    init {
        chartboostInterstitial = location?.let {
            Interstitial(it, object : InterstitialCallback {
                override fun onAdClicked(event: ClickEvent, error: ClickError?) {
                    BIDLog.d(TAG, "ad clicked")
                    adapter?.onClick()
                }

                override fun onAdDismiss(event: DismissEvent) {
                    adapter?.onHide()
                    BIDLog.d(TAG, "hide")
                }

                override fun onAdLoaded(event: CacheEvent, error: CacheError?) {
                    if (error == null && event.ad.isCached()) {
                        BIDLog.d(TAG, "loaded")
                        adapter?.onAdLoaded()
                    } else {
                        BIDLog.d(
                            TAG,
                            "Failed To Receive Ad error ${error?.exception?.message.toString()}"
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
                    BIDLog.d(TAG, "on impression recorded")
                }
            }, null)
        }
    }

    override fun load(context: Any) {
        val load = runCatching {
            chartboostInterstitial!!.cache()
        }
        if (load.isFailure) adapter?.onAdFailedToLoadWithError("Chart boost loading interstitial is failure")
    }

    override fun show(activity: Activity?) {
        if (chartboostInterstitial != null && chartboostInterstitial!!.isCached()) {
            chartboostInterstitial!!.show()
        } else adapter?.onFailedToDisplay("Chart boost showing interstitial is failure")
    }

    override fun activityNeededForShow(): Boolean {
        return false
    }

    override fun activityNeededForLoad(): Boolean {
        return false
    }

    override fun readyToShow(): Boolean {
        return chartboostInterstitial != null && chartboostInterstitial!!.isCached()
    }

    override fun shouldWaitForAdToDisplay(): Boolean {
        return true
    }
}