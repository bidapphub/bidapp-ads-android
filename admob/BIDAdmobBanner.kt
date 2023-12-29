package io.bidapp.networks.admob

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import io.bidapp.sdk.AdFormat
import io.bidapp.sdk.BIDLog
import io.bidapp.sdk.protocols.BIDBannerAdapterDelegateProtocol
import io.bidapp.sdk.protocols.BIDBannerAdapterProtocol
import java.lang.ref.WeakReference

@PublishedApi
internal class BIDAdmobBanner(
    var adapter: BIDBannerAdapterProtocol,
    var adTag: String?,
    format: AdFormat?
) : BIDBannerAdapterDelegateProtocol {
    val TAG = "Banner Admob"
    val bannerFormat = if (format?.isBanner_320x50 == true) AdSize.BANNER
    else if (format?.isBanner_300x250 == true) AdSize.MEDIUM_RECTANGLE
    else {
        BIDLog.d(TAG, "Unsuported Admob banner format: $format")
        null
    }
    var adView: WeakReference<AdView>? = null
    var ready = false
    private val adListener = object : AdListener() {

        override fun onAdClicked() {
            BIDLog.d(TAG, "ad clicked. adtag: ($adTag)")
            adapter.onClick()
        }

        override fun onAdClosed() {
            BIDLog.d(TAG, "ad closed. adtag: ($adTag)")
        }

        override fun onAdFailedToLoad(p0: LoadAdError) {

            BIDLog.d(TAG, "Admob failed to load ad. adtag: ($adTag) Error: ${p0.message}")
            adapter.onFailedToLoad(Error(p0.message))
        }

        override fun onAdImpression() {
            BIDLog.d(TAG, "ad show adtag: ($adTag)")
            adapter.onDisplay()
        }

        override fun onAdLoaded() {
            BIDLog.d(TAG, "ad loaded adtag: ($adTag)")
            ready = true
            adapter.onLoad()
        }

        override fun onAdOpened() {
            BIDLog.d(TAG, "ad open adtag: ($adTag)")
        }

    }


    override fun nativeAdView(): WeakReference<View>? {
        return WeakReference(adView?.get() as View)
    }

    override fun isAdReady(): Boolean {
        return ready
    }

    override fun load(context: Any) {
        ready = false
        if (bannerFormat == null || context as? Context == null) {
            adapter.onFailedToLoad(Error("Admob banner loading error"))
            return
        }
        if (adTag == null) {
            adapter.onFailedToLoad(Error("Admob banner adtag is null"))
            return
        }

        val networkExtrasBundle = Bundle()
        var request = AdRequest.Builder().build()
        if (BIDAdmobSDK.getGDPR() != null) {
            if (BIDAdmobSDK.getGDPR() == true) {
                networkExtrasBundle.putInt("npa", 1)
                request = AdRequest.Builder()
                    .addNetworkExtrasBundle(AdMobAdapter::class.java, networkExtrasBundle)
                    .build()
            }
        }
        if (adView?.get() == null) {
            adView = WeakReference(AdView(context))
            adView?.get()?.setAdSize(bannerFormat)
            adView?.get()?.adUnitId = adTag!!
        }
        adView?.get()?.adListener = adListener
        adView?.get()?.loadAd(request)

    }

    override fun destroy() {
        ready = false
        adView?.get()?.destroy()
        adView?.clear()
    }


    override fun showOnView(view: WeakReference<View>, density: Float): Boolean {
        try {
            val weightAndHeight: Array<Int> = when (bannerFormat) {
                AdSize.MEDIUM_RECTANGLE -> arrayOf(300, 250)
                AdSize.BANNER -> arrayOf(320, 50)
                else -> arrayOf(0, 0)
            }
            (view.get() as FrameLayout).addView(
                adView!!.get()!!,
                0,
                ViewGroup.LayoutParams(
                    (weightAndHeight[0] * density).toInt(),
                    (weightAndHeight[1] * density).toInt()
                )
            )
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

