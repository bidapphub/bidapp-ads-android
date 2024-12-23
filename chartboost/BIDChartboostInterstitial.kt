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

    val TAG = "Interstitial Chartboost"
    private var chartboostInterstitial: Interstitial? = null
    private var interstitialAdListener: InterstitialAdListener? = null


    override fun load(context: Any) {
        if (location.isNullOrEmpty()) {
            adapter?.onAdFailedToLoadWithError("Chartboost interstitial location is null or empty")
            return
        }
        if (chartboostInterstitial == null) {
            interstitialAdListener = InterstitialAdListener(TAG, adapter, location)
            chartboostInterstitial = Interstitial(location, interstitialAdListener!!, null)
        }
        chartboostInterstitial?.cache()
    }

    override fun show(activity: Activity?) {
        if (chartboostInterstitial == null || chartboostInterstitial?.isCached() == false) {
            adapter?.onFailedToDisplay("Chartboost showing interstitial is failure. Location: ($location)")
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
        interstitialAdListener = null
    }

    private class InterstitialAdListener(
        private val tag : String,
        private val adapter: BIDFullscreenAdapterProtocol?,
        private val location: String?
    ) : InterstitialCallback  {
        override fun onAdClicked(event: ClickEvent, error: ClickError?) {
            if (error == null) {
                BIDLog.d(tag, "Ad clicked. Location: ($location)")
                adapter?.onClick()
                return
            }
            BIDLog.d(tag, "Ad clicked is failure. Location: ($location)")
        }

        override fun onAdDismiss(event: DismissEvent) {
            adapter?.onHide()
            BIDLog.d(tag, "Ad hide. Location: ($location)")
        }

        override fun onAdLoaded(event: CacheEvent, error: CacheError?) {
            if (error == null && event.ad.isCached()) {
                BIDLog.d(tag, "Ad loaded. Location: ($location)")
                adapter?.onAdLoaded()
            } else {
                BIDLog.d(
                    tag,
                    "Ad Failed To Receive Ad error ${error?.code?.name.toString()} Location: ($location)"
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

    }
}