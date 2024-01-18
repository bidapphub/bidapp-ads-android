package io.bidapp.networks.facebook

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.facebook.ads.Ad
import com.facebook.ads.AdError
import com.facebook.ads.AdListener
import com.facebook.ads.AdSize
import com.facebook.ads.AdView
import io.bidapp.sdk.AdFormat
import io.bidapp.sdk.BIDLog
import io.bidapp.sdk.protocols.BIDBannerAdapterDelegateProtocol
import io.bidapp.sdk.protocols.BIDBannerAdapterProtocol
import java.lang.ref.WeakReference

class BIDFacebookBanner(adapter: BIDBannerAdapterProtocol, val adTag: String?, format: AdFormat) :
    BIDBannerAdapterDelegateProtocol {
    val TAG = "Banner Facebook"
    var adView : WeakReference<AdView>? = null
    var cachedAd: WeakReference<Ad>? = null
    private var adapter: BIDBannerAdapterProtocol? = adapter
    private val bannerFormat = if (format.isBanner_320x50) AdSize.BANNER_HEIGHT_50
    else if (format.isBanner_300x250) AdSize.RECTANGLE_HEIGHT_250
    else {
        BIDLog.d(TAG,"Unsupported Facebook banner format")
        null
    }

    private val bannerListener = object : AdListener {
        override fun onError(p0: Ad?, p1: AdError?) {
            BIDLog.d(TAG, "failed to load ad. Error: ${p1?.errorMessage} ${p0?.placementId}")
            adapter.onFailedToLoad(Error(p1?.errorMessage))
        }

        override fun onAdLoaded(p0: Ad?) {
            cachedAd = WeakReference(p0)
            adapter.onLoad()
            BIDLog.d(TAG, "loaded ad ${p0?.placementId}")
        }

        override fun onAdClicked(p0: Ad?) {
            BIDLog.d(TAG, "on click ${p0?.placementId}")
            adapter.onClick()
        }

        override fun onLoggingImpression(p0: Ad?) {
            BIDLog.d(TAG, "ad displayed ${p0?.placementId}")
            adapter.onDisplay()
        }

    }


    override fun nativeAdView(): WeakReference<View>? {
        return WeakReference(adView?.get() as? View)
    }

    override fun isAdReady(): Boolean {
    return cachedAd?.get() != null
    }

    override fun load(context: Any) {
        cachedAd?.clear()
        if (bannerFormat == null || context as? Context == null) {
            adapter?.onFailedToLoad(Error("ad banner loading error"))
            return
        }
        if (adTag == null) {
            adapter?.onFailedToLoad(Error("Facebook banner adtag is null"))
            return
        }
        if (adView?.get() == null) {
            adView = WeakReference(AdView(context, adTag, bannerFormat))
        }
            adView?.get()?.loadAd(adView?.get()?.buildLoadAdConfig()?.withAdListener(bannerListener)?.build())
    }

    override fun destroy() {
        adView?.get()?.destroy()
        adView?.clear()
        cachedAd?.get()?.destroy()
        cachedAd?.clear()
    }

    override fun showOnView(view: WeakReference<View>, density: Float): Boolean {
        return try {
            val weightAndHeight: Array<Int> = when (bannerFormat) {
                AdSize.RECTANGLE_HEIGHT_250 -> arrayOf(300, 250)
                AdSize.BANNER_HEIGHT_50 -> arrayOf(320, 50)
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
            return true
        } catch (e: Exception) {
            BIDLog.d(TAG, "Show on view is failed")
            false
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