package io.bidapp.networks.applovinmax


import android.content.Context
import android.view.View
import android.widget.FrameLayout
import com.applovin.mediation.MaxAd
import com.applovin.mediation.MaxAdFormat
import com.applovin.mediation.MaxAdViewAdListener
import com.applovin.mediation.MaxError
import com.applovin.mediation.ads.MaxAdView
import io.bidapp.sdk.AdFormat
import io.bidapp.sdk.BIDLog
import io.bidapp.sdk.protocols.BIDBannerAdapterDelegateProtocol
import io.bidapp.sdk.protocols.BIDBannerAdapterProtocol
import java.lang.ref.WeakReference

@PublishedApi
internal class BIDApplovinMaxBanner(
    adapter: BIDBannerAdapterProtocol,
    val adTag: String?,
    format: AdFormat
) : BIDBannerAdapterDelegateProtocol, MaxAdViewAdListener {
    val TAG = "Banner Max"
    val bannerFormat = if (format.isBanner_320x50) MaxAdFormat.BANNER
    else if (format.isBanner_300x250) MaxAdFormat.MREC
    else if (format.isBanner_728x90) MaxAdFormat.LEADER
    else {
        BIDLog.d(TAG, "Unsupported applovin MAX banner format: ${format?.name()}")
        null
    }
    var adapter: BIDBannerAdapterProtocol? = adapter
    var adView: WeakReference<MaxAdView>? = null
    var cachedAd: MaxAd? = null

    override fun nativeAdView(): WeakReference<View> {
        return WeakReference(adView?.get() as? View)
    }

    override fun isAdReady(): Boolean {
        return cachedAd != null
    }

    override fun load(context: Any) {
        cachedAd = null
        if (bannerFormat == null || context as? Context == null) {
            adapter?.onFailedToLoad(Error("Max banner loading error"))
            return
        }
        if (adTag.isNullOrEmpty()) {
            adapter?.onFailedToLoad(Error("Max banner adTag is null or empty"))
            return
        }
        if (adView?.get() == null) {
            adView = WeakReference(MaxAdView(adTag, context))
            adView?.get()?.setExtraParameter( "allow_pause_auto_refresh_immediately", "true" )
            adView?.get()?.stopAutoRefresh()
        }
        adView?.get()?.setListener(this as MaxAdViewAdListener)
        adView?.get()?.loadAd()
    }


    override fun destroy() {
        cachedAd = null
        adView?.get()?.destroy()
    }

    override fun showOnView(view: WeakReference<View>, density: Float): Boolean {
        return try {
            val weightAndHeight: Array<Int> = when (adView!!.get()!!.adFormat) {
                MaxAdFormat.MREC -> arrayOf(300, 250)
                MaxAdFormat.BANNER -> arrayOf(320, 50)
                MaxAdFormat.LEADER -> arrayOf(728, 90)
                else -> arrayOf(0, 0)
            }
            (view.get() as FrameLayout).addView(
                adView!!.get(),
                (weightAndHeight[0] * density).toInt(),
                (weightAndHeight[1] * density).toInt()
            )
            true
        } catch (e: Exception) {
            BIDLog.d(TAG, "Show on view is failed")
            false
        }
    }

    override fun waitForAdToShow(): Boolean {
        return true
    }

    override fun onAdLoaded(p0: MaxAd) {
        cachedAd = p0
        adapter?.onLoad()
        BIDLog.d(TAG, "Ad loaded. adtag: ($adTag)")
    }

    private fun onFailedToLoad(error: String) {
        cachedAd = null
        BIDLog.d(TAG, "Failed to load ad. Error: $error adtag: ($adTag)")
        adapter?.onFailedToLoad(Error(error))
    }

    override fun onAdDisplayed(p0: MaxAd) {
        BIDLog.d(TAG, "Ad displayed. adtag: ($adTag)")
        adapter?.onDisplay()
    }

    override fun onAdHidden(p0: MaxAd) {
        BIDLog.d(TAG, "Ad hide. adtag: ($adTag)")
        cachedAd = null
        adapter?.onHide()
        adView?.get()?.destroy()
    }

    override fun onAdClicked(p0: MaxAd) {
        BIDLog.d(TAG, "Ad clicked. adtag: ($adTag)")
        adapter?.onClick()
    }

    override fun onAdLoadFailed(p0: String, p1: MaxError) {
        val error = p1.message ?: "Unknown error"
        BIDLog.d(TAG, "Ad load failed. Error:${error}")
        onFailedToLoad(error)
    }

    override fun onAdExpanded(p0: MaxAd) {
        BIDLog.d(TAG, "Ad expanded. adtag: ($adTag)")
    }

    override fun onAdCollapsed(p0: MaxAd) {
        BIDLog.d(TAG, "Ad collapsed. adtag: ($adTag)")
    }

    override fun onAdDisplayFailed(p0: MaxAd, p1: MaxError) {
        adapter?.onFailedToDisplay(kotlin.Error(p1.message))
        BIDLog.d(TAG, "Failed to display ad. Error: ${p1.message} adtag: ($adTag)")
    }


    override fun revenue(): Double? {
        if (cachedAd != null) {
           return cachedAd?.revenue
        }
        return null
    }

    override fun activityNeededForLoad(): Boolean {
        return false
    }

}





