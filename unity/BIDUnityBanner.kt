package io.bidapp.networks.unity


import android.app.Activity
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout

import com.unity3d.services.banners.BannerErrorInfo
import com.unity3d.services.banners.BannerView
import com.unity3d.services.banners.UnityBannerSize
import io.bidapp.sdk.AdFormat
import io.bidapp.sdk.BIDLog
import io.bidapp.sdk.protocols.BIDBannerAdapterDelegateProtocol
import io.bidapp.sdk.protocols.BIDBannerAdapterProtocol
import java.lang.ref.WeakReference


@PublishedApi
internal class BIDUnityBanner(
    var adapter: BIDBannerAdapterProtocol?,
    var adTag: String?,
    format: AdFormat?
) : BIDBannerAdapterDelegateProtocol, BannerView.IListener {

    val TAG = "Banner Unity"

    val bannerFormat = if (format?.isbanner_320x50 == true) UnityBannerSize(320, 50)
    else if (format?.isbanner_300x250 == true) UnityBannerSize(300, 250)
    else {
        BIDLog.d(TAG, "Unsuported banner format: $format")
        null
    }
    var adView: WeakReference<BannerView>? = null
    var cachedAd: BannerView? = null


    override fun nativeAdView(): WeakReference<View>? {
        return WeakReference(adView?.get() as View)
    }

    override fun isAdReady(): Boolean {
        return cachedAd != null
    }

    override fun load(activity: Activity) {
        cachedAd = null
        val load = runCatching {
            if (adView == null) {
                adView = WeakReference(BannerView(activity, adTag!!, bannerFormat))
                adView?.get()?.listener = this
            }
            adView?.get()?.load()
        }
        if (load.isFailure) adapter?.onFailedToLoad(Error("Unity banner loading error"))
    }

    override fun prepareForDealloc() {
       //  prepareForDealloc
    }

    override fun showOnView(view: WeakReference<View>, activity: Activity): Boolean {
        return try {
            val density = activity.resources.displayMetrics.density
            val weightAndHeight =
                if (bannerFormat?.height == 50 && bannerFormat.width == 320) arrayOf(
                    320,
                    50
                ) else if (bannerFormat?.height == 250 && bannerFormat.width == 300) arrayOf(
                    300,
                    250
                ) else arrayOf(0, 0)
            (view.get() as FrameLayout).addView(
                adView?.get(),0, ViewGroup.LayoutParams( (weightAndHeight[0]*density).toInt(), (weightAndHeight[1]*density).toInt())
            )
            true
        } catch (e: Exception) {
            BIDLog.d(TAG, "Show on view is failed ${e.message}")
            false
        }
    }

    override fun waitForAdToShow(): Boolean {
        return true
    }


    override fun onBannerLoaded(ad: BannerView?) {
        cachedAd = ad
        adapter?.onLoad()
        BIDLog.d(TAG, "load")
    }

    override fun onBannerShown(bannerAdView: BannerView?) {
        BIDLog.d(TAG, "show")
        adapter?.onDisplay()
    }

    override fun onBannerClick(bannerAdView: BannerView?) {
        BIDLog.d(TAG, "click")
        adapter?.onClick()
    }

    override fun onBannerFailedToLoad(bannerAdView: BannerView?, errorInfo: BannerErrorInfo?) {
        BIDLog.d(TAG, "failed to load ad. Error: ${errorInfo?.errorCode}")
        adapter?.onFailedToLoad(Error(errorInfo?.errorCode.toString()))
    }

    override fun onBannerLeftApplication(bannerView: BannerView?) {
        BIDLog.d(TAG, "banner left application")
        adapter?.onHide()
    }
}



