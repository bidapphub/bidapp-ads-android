package io.bidapp.networks.liftoff

import android.app.Activity
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.vungle.ads.BannerAd
import com.vungle.ads.BannerAdListener
import com.vungle.ads.BannerAdSize
import com.vungle.ads.BannerView
import com.vungle.ads.BaseAd
import com.vungle.ads.VungleError
import io.bidapp.sdk.AdFormat
import io.bidapp.sdk.BIDLog
import io.bidapp.sdk.protocols.BIDBannerAdapterDelegateProtocol
import io.bidapp.sdk.protocols.BIDBannerAdapterProtocol
import java.lang.ref.WeakReference


@PublishedApi
internal class BIDLiftoffBanner(adapter: BIDBannerAdapterProtocol, adTag: String, format: AdFormat) :
    BIDBannerAdapterDelegateProtocol, BannerAdListener {
    var adTag: String? = adTag
    var adapter: BIDBannerAdapterProtocol? = adapter
    var bannerAd: WeakReference<BannerAd>? = null
    var adView: WeakReference<BannerView>? = null
    var cachedAd: String? = null
    val TAG = "Banner Liftoff"
    val bannerFormat = if (format.isbanner_320x50) BannerAdSize.BANNER
    else if (format.isbanner_300x250) BannerAdSize.VUNGLE_MREC
    else {
        BIDLog.d(TAG, "Unsuported Liftoff banner format: $format")
        null
    }


    override fun nativeAdView(): WeakReference<View> {
        return WeakReference(adView?.get())
    }

    override fun isAdReady(): Boolean {
        return cachedAd != null
    }

    override fun load(activity: Activity) {
        cachedAd = null
        val load = runCatching {
            if (bannerAd == null)
                bannerAd =
                    WeakReference(BannerAd(activity.applicationContext, adTag!!, bannerFormat!!))
            bannerAd?.get()?.adListener = this
            bannerAd?.get()?.load()
        }
        if (load.isFailure) adapter?.onFailedToLoad(Error("banner loading error"))
    }


    override fun prepareForDealloc() {
        //   prepareForDealloc
    }

    override fun showOnView(view: WeakReference<View>, activity: Activity): Boolean {
        return try {
            val density = activity.resources.displayMetrics.density
            val weightAndHeight: Array<Int> = when (bannerFormat) {
                BannerAdSize.VUNGLE_MREC -> arrayOf(300, 250)
                BannerAdSize.BANNER -> arrayOf(320, 50)
                else -> arrayOf(0, 0)
            }
            adView = WeakReference(bannerAd?.get()?.getBannerView())
            (view.get() as FrameLayout).addView(
                adView?.get(),
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

    override fun onAdClicked(baseAd: BaseAd) {
        BIDLog.d(TAG, "on click")
        adapter?.onClick()
    }

    override fun onAdEnd(baseAd: BaseAd) {
        BIDLog.d(TAG, "on ad end")
        cachedAd = null
    }

    override fun onAdFailedToLoad(baseAd: BaseAd, adError: VungleError) {
        BIDLog.d(TAG, "failed to load ad. Error: ${adError.message}")
        adapter?.onFailedToLoad(Error(adError.message))
    }

    override fun onAdFailedToPlay(baseAd: BaseAd, adError: VungleError) {
        BIDLog.d(TAG, "failed to play ${adError.message}")
        adapter?.onFailedToDisplay(Error(adError.message))
    }

    override fun onAdImpression(baseAd: BaseAd) {
        BIDLog.d(TAG, "on ad impression")
    }

    override fun onAdLeftApplication(baseAd: BaseAd) {
        cachedAd = null
        BIDLog.d(TAG, "hide")
        adapter?.onHide()
    }
    override fun onAdLoaded(baseAd: BaseAd) {
        cachedAd = baseAd.placementId
        adapter?.onLoad()
        BIDLog.d(TAG, "loaded")
    }

    override fun onAdStart(baseAd: BaseAd) {
        BIDLog.d(TAG, "shown")
        adapter?.onDisplay()
    }
}

