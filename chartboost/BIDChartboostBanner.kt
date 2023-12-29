package io.bidapp.networks.chartboost

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.chartboost.sdk.ads.Ad
import com.chartboost.sdk.ads.Banner
import com.chartboost.sdk.callbacks.BannerCallback
import com.chartboost.sdk.events.CacheError
import com.chartboost.sdk.events.CacheEvent
import com.chartboost.sdk.events.ClickError
import com.chartboost.sdk.events.ClickEvent
import com.chartboost.sdk.events.ImpressionEvent
import com.chartboost.sdk.events.ShowError
import com.chartboost.sdk.events.ShowEvent
import io.bidapp.sdk.AdFormat
import io.bidapp.sdk.BIDLog
import io.bidapp.sdk.protocols.BIDBannerAdapterDelegateProtocol
import io.bidapp.sdk.protocols.BIDBannerAdapterProtocol
import java.lang.ref.WeakReference

@PublishedApi
internal class BIDChartboostBanner(
    var adapter: BIDBannerAdapterProtocol?,
    var location: String?,
    format: AdFormat?
) : BIDBannerAdapterDelegateProtocol {
    val TAG = "Banner Chartboost"
    private val bannerFormat = if (format?.isBanner_320x50 == true) Banner.BannerSize.STANDARD
    else if (format?.isBanner_300x250 == true) Banner.BannerSize.MEDIUM
    else {
        BIDLog.d(TAG, "Unsuported Chartboost banner format: $format")
        null
    }
    private var adView: WeakReference<Banner>? = null
    var cachedAd: WeakReference<Ad>? = null

    private val chartboostCallback = object : BannerCallback {
        override fun onAdClicked(event: ClickEvent, error: ClickError?) {
            if (error == null) {
                BIDLog.d(TAG, "ad click. location: ($location)")
                adapter?.onClick()
            }
            else BIDLog.d(TAG, "ad click is failure. location: ($location) Error: ${error.exception}")
        }

        override fun onAdLoaded(event: CacheEvent, error: CacheError?) {
            if (error == null) {
                cachedAd = WeakReference(event.ad)
                adapter?.onLoad()
                BIDLog.d(TAG, "ad loaded. location: ($location)")
            } else {
                BIDLog.d(TAG, "Chartboost failed to load ad. Error: ${error.exception} location: ($location)")
                adapter?.onFailedToLoad(Error(error.exception))
            }
        }

        override fun onAdRequestedToShow(event: ShowEvent) {
            BIDLog.d(TAG, "ad requested to show. location: ($location)")
        }

        override fun onAdShown(event: ShowEvent, error: ShowError?) {
            if (error != null) {
                BIDLog.d(TAG, "Chartboost failed to show ad. Error: ${error.exception?.message} location: ($location)")
                adapter?.onFailedToDisplay(Error(error.exception?.message))
                return
            }
            BIDLog.d(TAG, "ad show. location: ($location)")
        }

        override fun onImpressionRecorded(event: ImpressionEvent) {
            BIDLog.d(TAG, "ad impression recorded. location: ($location)")
            adapter?.onDisplay()
        }
    }

    override fun nativeAdView(): WeakReference<View>? {
        return WeakReference(adView?.get())
    }

    override fun isAdReady(): Boolean {
        return cachedAd?.get() != null
    }

    override fun load(context: Any) {
        cachedAd?.clear()
        if (bannerFormat == null || context as? Context == null) {
            adapter?.onFailedToLoad(Error("Chartboost banner loading error"))
            return
        }
        if (location == null) {
            adapter?.onFailedToLoad(Error("Chartboost banner location is null"))
            return
        }
        if (adView?.get() == null) {
            adView = WeakReference(
                Banner(
                    context,
                    location!!,
                    bannerFormat,
                    chartboostCallback
                )
            )
        }
        adView?.get()?.cache()
    }

    override fun destroy() {
        cachedAd = null
        adView?.get()?.detach()
        adView?.clear()
    }

    override fun showOnView(view: WeakReference<View>, density: Float): Boolean {
        try {
            val weightAndHeight: Array<Int> = when (bannerFormat) {
                Banner.BannerSize.MEDIUM -> arrayOf(300, 250)
                Banner.BannerSize.STANDARD -> arrayOf(320, 50)
                else -> arrayOf(0, 0)
            }
            (view.get() as FrameLayout).addView(
                adView!!.get(),
                0,
                ViewGroup.LayoutParams(
                    (weightAndHeight[0] * density).toInt(),
                    (weightAndHeight[1] * density).toInt()
                )
            )
            adView!!.get()!!.show()
            return true
        } catch (e: Exception) {
            BIDLog.d(TAG, "Show on view is failed ${e.message}")
            return false
        }
    }

    override fun waitForAdToShow(): Boolean {
        return true
    }

    override fun activityNeededForLoad(): Boolean {
        return false
    }

    override fun revenue(): Double? {
        return null
    }
}