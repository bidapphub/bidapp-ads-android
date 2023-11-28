package io.bidapp.networks.applovinmax


import android.app.Activity
import android.content.Context
import android.util.Log
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
internal class BIDApplovinMaxBanner(adapter: BIDBannerAdapterProtocol, adTag: String, format: AdFormat) : BIDBannerAdapterDelegateProtocol, MaxAdViewAdListener {
    val TAG = "Banner Max"
    val bannerFormat = if (format.isbanner_320x50) MaxAdFormat.BANNER
    else if (format.isbanner_300x250) MaxAdFormat.MREC
    else {
        BIDLog.d(TAG, "Unsuported applovin MAX banner format: $format")
        null
    }
    var adTag : String? = adTag
    var adapter : BIDBannerAdapterProtocol? = adapter
    var adView : WeakReference<MaxAdView>? = null
    var cachedAd: MaxAd? = null

    override fun nativeAdView(): WeakReference<View> {
        return WeakReference(adView?.get() as View)
    }

    override fun isAdReady(): Boolean {
        return cachedAd != null
    }

    override fun load(context: Any) {
        val load = runCatching {
            if (adView == null) {
                adView = WeakReference(MaxAdView(adTag, bannerFormat, context as Context))
                adView?.get()?.setListener(this as MaxAdViewAdListener)
                adView?.get()?.stopAutoRefresh()
            }
            adView?.get()?.loadAd()
        }
        if(load.isFailure) adapter?.onFailedToLoad(Error("Max banner loading error"))
    }


    override fun destroy() {
       cachedAd = null
       adView?.get()?.destroy()
    }

    override fun showOnView(view: WeakReference<View>, density: Float): Boolean {
        return try {
            val weightAndHeight : Array<Int> = when(adView?.get()?.adFormat){
                MaxAdFormat.MREC -> arrayOf(300,250)
                MaxAdFormat.BANNER -> arrayOf(320,50)
                else -> arrayOf(0,0)
            }
            (view.get() as FrameLayout).addView(adView?.get(), (weightAndHeight[0]*density).toInt(), (weightAndHeight[1]*density).toInt())
             true
        } catch (e: Exception){
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
        BIDLog.d(TAG, "loaded")
    }

    private fun onFailedToLoad(error: MaxError) {
        BIDLog.d(TAG, "AppLovin MAX failed to load ad. Error: ${error.message}")
        adapter?.onFailedToLoad(Error(error.message))
    }

    override fun onAdDisplayed(p0: MaxAd) {
        BIDLog.d(TAG, "ad displayed")
        adapter?.onDisplay()
    }

    override fun onAdHidden(p0: MaxAd) {
        BIDLog.d(TAG, "hide")
        cachedAd = null
        adapter?.onHide()
        adView?.get()?.destroy()
    }

    override fun onAdClicked(p0: MaxAd) {
        BIDLog.d(TAG, "clicked")
        adapter?.onClick()
    }

    override fun onAdLoadFailed(p0: String, p1: MaxError) {
        BIDLog.d(TAG, "ad load failed. Error:${p1.message}")
        p1.let { onFailedToLoad(it) }
    }

    override fun onAdExpanded(p0: MaxAd) {
        BIDLog.d(TAG, "ad expanded")
    }

    override fun onAdCollapsed(p0: MaxAd) {
        BIDLog.d(TAG, "collapsed")
    }

    override fun onAdDisplayFailed(p0: MaxAd, p1: MaxError) {
        adapter?.onFailedToDisplay(kotlin.Error(p1.message))
        BIDLog.d(TAG, "AppLovin MAX failed to display ad. Error: ${p1.message}")
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





