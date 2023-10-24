package io.bidapp.networks.applovin

import android.app.Activity
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import com.applovin.adview.AppLovinAdView
import com.applovin.adview.AppLovinAdViewDisplayErrorCode
import com.applovin.adview.AppLovinAdViewEventListener
import com.applovin.sdk.AppLovinAd
import com.applovin.sdk.AppLovinAdClickListener
import com.applovin.sdk.AppLovinAdDisplayListener
import com.applovin.sdk.AppLovinAdLoadListener
import com.applovin.sdk.AppLovinAdSize
import io.bidapp.sdk.AdFormat
import io.bidapp.sdk.BIDLog
import io.bidapp.sdk.protocols.BIDBannerAdapterDelegateProtocol
import io.bidapp.sdk.protocols.BIDBannerAdapterProtocol
import java.lang.ref.WeakReference


@PublishedApi
internal class BIDApplovinBanner(adapter: BIDBannerAdapterProtocol, adTag: String, format: AdFormat) : BIDBannerAdapterDelegateProtocol, AppLovinAdLoadListener, AppLovinAdDisplayListener,
    AppLovinAdViewEventListener, AppLovinAdClickListener {
    val TAG = "Banner Applovin"
    val bannerFormat = if (format.isbanner_320x50) AppLovinAdSize.BANNER
    else if (format.isbanner_300x250) AppLovinAdSize.MREC
    else {
        BIDLog.d(TAG, "Unsuported applovin banner format: $format")
        null
    }
    var adapter : BIDBannerAdapterProtocol? = adapter
    var adView : WeakReference<AppLovinAdView>? = null
    var cachedAd: AppLovinAd? = null


    override fun adClicked(p0: AppLovinAd?) {
        BIDLog.d(TAG, "Ad clicked")
        adapter?.onClick()
    }

    override fun adOpenedFullscreen(p0: AppLovinAd?, p1: AppLovinAdView?) {
        BIDLog.d(TAG, "Ad open fullscreen")
    }

    override fun adClosedFullscreen(p0: AppLovinAd?, p1: AppLovinAdView?) {
        BIDLog.d(TAG, "Ad closed fullscreen")
    }

    override fun adLeftApplication(p0: AppLovinAd?, p1: AppLovinAdView?) {
        BIDLog.d(TAG, "Ad left application")
    }

    override fun adFailedToDisplay(
        p0: AppLovinAd?,
        p1: AppLovinAdView?,
        p2: AppLovinAdViewDisplayErrorCode?
    ) {
        adapter?.onFailedToDisplay(Error(p2?.toString()))
        BIDLog.d(TAG, "AppLovin failed to display ad. Error: ${p2?.toString()}")
    }

    override fun adDisplayed(p0: AppLovinAd?) {
        BIDLog.d(TAG, "display")
        adapter?.onDisplay()
    }

    override fun adHidden(p0: AppLovinAd?) {
        BIDLog.d(TAG, "hide")
        adapter?.onHide()
    }

    override fun adReceived(ad: AppLovinAd?) {
        cachedAd = ad
        adapter?.onLoad()
        BIDLog.d(TAG, "load")
    }

    override fun failedToReceiveAd(p0: Int) {
        BIDLog.d(TAG, "failed to load ad. Error: ${p0}")
        adapter?.onFailedToLoad(Error(p0.toString()))
    }


    override fun nativeAdView(): WeakReference<View> {
        return WeakReference(adView?.get() as View)
    }

    override fun isAdReady(): Boolean {
        return cachedAd != null
    }

    override fun load(activity: Activity) {
        val load = runCatching {
            if (adView == null) {
                adView = WeakReference(AppLovinAdView(bannerFormat, activity))
                adView?.get()?.setAdLoadListener(this)
                adView?.get()?.setAdDisplayListener(this)
                adView?.get()?.setAdViewEventListener(this)
                adView?.get()?.setAdClickListener(this)
            }
            adView?.get()?.loadNextAd()
        }
        if (load.isFailure)  adapter?.onFailedToLoad(Error("Applovin banner loading error"))
    }

    override fun prepareForDealloc() {
        //  prepareForDealloc
    }

    override fun showOnView(view: WeakReference<View>, activity: Activity): Boolean {
        BIDLog.d(TAG, "Banner show applovin")
        return try {
            val density = activity.resources.displayMetrics.density
            val weightAndHeight : Array<Int> = when(adView?.get()?.size.toString()){
                "MREC" -> arrayOf(300,250)
                "BANNER" -> arrayOf(320,50)
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
}
