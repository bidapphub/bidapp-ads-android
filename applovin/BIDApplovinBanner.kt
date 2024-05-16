package io.bidapp.networks.applovin

import android.content.Context
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
internal class BIDApplovinBanner(val adapter: BIDBannerAdapterProtocol, adTag: String? = null, format: AdFormat) : BIDBannerAdapterDelegateProtocol, AppLovinAdLoadListener, AppLovinAdDisplayListener,
    AppLovinAdViewEventListener, AppLovinAdClickListener {
    val TAG = "Banner Applovin"
    val bannerFormat = if (format.isBanner_320x50) AppLovinAdSize.BANNER
    else if (format.isBanner_300x250) AppLovinAdSize.MREC
    else if (format.isBanner_728x90) AppLovinAdSize.LEADER
    else {
        BIDLog.d(TAG, "Unsupported applovin banner format: ${format?.name()}")
        null
    }
    var adView : WeakReference<AppLovinAdView>? = null
    var cachedAd: AppLovinAd? = null


    override fun adClicked(p0: AppLovinAd?) {
        BIDLog.d(TAG, "ad clicked ")
        adapter.onClick()
    }

    override fun adOpenedFullscreen(p0: AppLovinAd?, p1: AppLovinAdView?) {
        BIDLog.d(TAG, "ad open fullscreen")
    }

    override fun adClosedFullscreen(p0: AppLovinAd?, p1: AppLovinAdView?) {
        BIDLog.d(TAG, "ad closed fullscreen")
    }

    override fun adLeftApplication(p0: AppLovinAd?, p1: AppLovinAdView?) {
        BIDLog.d(TAG, "ad left application")
    }

    override fun adFailedToDisplay(
        p0: AppLovinAd?,
        p1: AppLovinAdView?,
        p2: AppLovinAdViewDisplayErrorCode?
    ) {
        val errorCode = p2 ?: "Unknown code"
        adapter.onFailedToDisplay(Error("$errorCode"))
        BIDLog.d(TAG, "AppLovin failed to display ad. Error: $errorCode")
    }

    override fun adDisplayed(p0: AppLovinAd?) {
        BIDLog.d(TAG, "ad display")
        adapter.onDisplay()
    }

    override fun adHidden(p0: AppLovinAd?) {
        BIDLog.d(TAG, "ad hide")
        adapter.onHide()
    }

    override fun adReceived(ad: AppLovinAd?) {
        cachedAd = ad
        adapter.onLoad()
        BIDLog.d(TAG, "ad load")
    }

    override fun failedToReceiveAd(p0: Int) {
        cachedAd = null
        BIDLog.d(TAG, "ad failed to load. Error: $p0")
        adapter.onFailedToLoad(Error(p0.toString()))
    }


    override fun nativeAdView(): WeakReference<View> {
        return WeakReference(adView?.get() as? View)
    }

    override fun isAdReady(): Boolean {
        return cachedAd != null
    }

    override fun load(context: Any) {
        cachedAd = null
        if (bannerFormat == null || context as? Context == null){
            adapter.onFailedToLoad(Error("Applovin banner loading error"))
            return
        }
            if (adView?.get() == null) {
                adView = WeakReference(AppLovinAdView(BIDApplovinSDK.appLovinGetInstanceSDK((context as Context).applicationContext), bannerFormat, context))
            }
            adView?.get()?.setAdLoadListener(this)
            adView?.get()?.setAdDisplayListener(this)
            adView?.get()?.setAdViewEventListener(this)
            adView?.get()?.setAdClickListener(this)
            adView?.get()?.loadNextAd()
    }

    override fun destroy() {
        cachedAd = null
        adView?.get()?.destroy()
        adView?.clear()
    }

    override fun showOnView(view: WeakReference<View>, density: Float): Boolean {
        BIDLog.d(TAG, "Banner show applovin")
        return try {
            val weightAndHeight : Array<Int> = when(bannerFormat){
                AppLovinAdSize.MREC -> arrayOf(300,250)
                AppLovinAdSize.BANNER -> arrayOf(320,50)
                AppLovinAdSize.LEADER -> arrayOf(728,90)
                else -> arrayOf(0,0)
            }
            (view.get() as FrameLayout).addView(adView!!.get(), (weightAndHeight[0]*density).toInt(), (weightAndHeight[1]*density).toInt())
            true
        } catch (e: Exception){
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
