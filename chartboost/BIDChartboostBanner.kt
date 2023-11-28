package io.bidapp.networks.chartboost

import android.app.Activity
import android.content.Context
import android.util.Log
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
    val bannerFormat = if (format?.isbanner_320x50 == true) Banner.BannerSize.STANDARD
    else if (format?.isbanner_300x250 == true) Banner.BannerSize.MEDIUM
    else {
        BIDLog.d(TAG, "Unsuported Chartboost banner format: $format")
        null
    }
    var adView: WeakReference<Banner>? = null
    var cachedAd: WeakReference<Ad>? = null

    val chartboost = object : BannerCallback {
        override fun onAdClicked(event: ClickEvent, error: ClickError?) {
            BIDLog.d(TAG, "on ad click")
            adapter?.onClick()
        }

        override fun onAdLoaded(event: CacheEvent, error: CacheError?) {
            if (error == null) {
                cachedAd = WeakReference(event.ad)
                adapter?.onLoad()
                BIDLog.d(TAG, "loaded")
            } else {
                BIDLog.d(TAG, "Chartboost failed to load ad. Error: ${error.exception}")
                adapter?.onFailedToLoad(Error(error.exception))
            }
        }

        override fun onAdRequestedToShow(event: ShowEvent) {
            BIDLog.d(TAG, "on ad requested to show")
        }

        override fun onAdShown(event: ShowEvent, error: ShowError?) {
            if (error == null) {
                BIDLog.d(TAG, "show")
                adapter?.onDisplay()
            } else {
                BIDLog.d(TAG, "Admob failed to show ad. Error: ${error.exception?.message}")
                adapter?.onFailedToDisplay(Error(error.exception?.message))
            }
        }

        override fun onImpressionRecorded(event: ImpressionEvent) {
            BIDLog.d(TAG, "on impression recorded")
        }
    }

    override fun nativeAdView(): WeakReference<View>? {
        return WeakReference(adView?.get())
    }

    override fun isAdReady(): Boolean {
        return cachedAd != null
    }

    override fun load(context: Any) {
        cachedAd = null
        if (adView == null) {
            val load = runCatching {
                adView = WeakReference(
                    Banner(
                        context as Context,
                        location!!,
                        bannerFormat!!,
                        chartboost
                    )
                )
                adView!!.get()!!.cache()
            }
            if (load.isFailure) {
                adapter?.onFailedToLoad(Error("Banner load Chartboost is failure"))
            }
        }
    }

    override fun destroy() {
       cachedAd = null
       adView?.get()?.detach()
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
}