package io.bidapp.networks.chartboost

import android.app.Activity
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
    val location: String? = null
) :
    BIDFullscreenAdapterDelegateProtocol {

    val TAG = "interstitial Chartboost"
    private var chartboostInterstitial: Interstitial? = null

    private fun init() {
        if (chartboostInterstitial == null){
            return
        }
        if (location.isNullOrEmpty()){
            BIDLog.d(TAG, "Location is null or empty")
            return
        }
        chartboostInterstitial = Interstitial(location, object : InterstitialCallback {
            override fun onAdClicked(event: ClickEvent, error: ClickError?) {
                if (error == null) {
                    BIDLog.d(TAG, "ad clicked. Location: ($location)")
                    adapter?.onClick()
                    return
                }
                BIDLog.d(TAG, "ad clicked is failure. Location: ($location)")
            }

            override fun onAdDismiss(event: DismissEvent) {
                adapter?.onHide()
                BIDLog.d(TAG, "ad hide. Location: ($location)")
            }

            override fun onAdLoaded(event: CacheEvent, error: CacheError?) {
                if (error == null && event.ad.isCached()) {
                    BIDLog.d(TAG, "ad loaded. Location: ($location)")
                    adapter?.onAdLoaded()
                } else {
                    BIDLog.d(
                        TAG,
                        "ad Failed To Receive Ad error ${error?.code?.name.toString()} Location: ($location)"
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
        }, null)
    }

    override fun load(context: Any) {
        if (location.isNullOrEmpty()) {
            adapter?.onAdFailedToLoadWithError("Chartboost interstitial location is null or empty")
            return
        }
        if (chartboostInterstitial == null) {
            init()
        }
        chartboostInterstitial?.cache()
    }

    override fun show(activity: Activity?) {
        if (chartboostInterstitial == null || chartboostInterstitial?.isCached() == false) {
            adapter?.onFailedToDisplay("Chart boost showing interstitial is failure. Location: ($location)")
            return
        }
        chartboostInterstitial?.show()
    }

    override fun activityNeededForShow(): Boolean {
        return false
    }

    override fun activityNeededForLoad(): Boolean {
        return false
    }

    override fun readyToShow(): Boolean {
        return chartboostInterstitial?.isCached() ?: false
    }

    override fun shouldWaitForAdToDisplay(): Boolean {
        return true
    }

    override fun revenue(): Double? {
        return null
    }

    override fun destroy() {
        chartboostInterstitial?.clearCache()
        chartboostInterstitial = null
    }
}