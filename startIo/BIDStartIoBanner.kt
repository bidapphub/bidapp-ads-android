package io.bidapp.networks.startIo

import android.app.Activity
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.startapp.sdk.ads.banner.Banner
import com.startapp.sdk.ads.banner.BannerListener
import com.startapp.sdk.ads.banner.Mrec
import com.startapp.sdk.adsbase.model.AdPreferences
import io.bidapp.sdk.AdFormat
import io.bidapp.sdk.BIDLog
import io.bidapp.sdk.protocols.BIDBannerAdapterDelegateProtocol
import io.bidapp.sdk.protocols.BIDBannerAdapterProtocol
import java.lang.ref.WeakReference

class BIDStartIoBanner(adapter: BIDBannerAdapterProtocol, val adTag: String?, format: AdFormat, val ecpm : Double) :
    BIDBannerAdapterDelegateProtocol {

    val TAG = "Banner StartIo"
    var adapter: BIDBannerAdapterProtocol? = adapter
    var startAppAdPreferences: AdPreferences? = null
    var cachedAd: WeakReference<View>? = null
    var adView: WeakReference<Any>? = null
    var bannerFormat = if (format.isBanner_320x50) "banner"
    else if (format.isBanner_300x250) "mrec"
    else {
        adapter.onFailedToLoad(Error("Unsuported Liftoff banner format"))
        null
    }

    val bannerListener = object : BannerListener {
        override fun onReceiveAd(p0: View?) {
            cachedAd = WeakReference(p0)
            adapter.onLoad()
            BIDLog.d(TAG, "loaded $adTag")
        }

        override fun onFailedToReceiveAd(p0: View?) {
            BIDLog.d(TAG, "startIo failed to load ad $adTag")
            adapter.onFailedToLoad(Error("failed to load ad"))
        }

        override fun onImpression(p0: View?) {
            BIDLog.d(TAG, "on ad clicked $adTag")
            adapter.onDisplay()
        }

        override fun onClick(p0: View?) {
            BIDLog.d(TAG, "on ad clicked $adTag")
            adapter.onClick()
        }

    }

    override fun nativeAdView() : WeakReference<View>? {
        return cachedAd
    }

    override fun isAdReady(): Boolean {
        return cachedAd?.get() != null
    }

    override fun load(context: Any) {
        cachedAd?.clear()
        startAppAdPreferences = AdPreferences()
        if (bannerFormat == null || context as? Activity == null) {
            adapter?.onFailedToLoad(Error("StartIo banner loading error"))
            return
        }
        if (adTag == null) BIDLog.d(TAG, "AdTag is null")
        else startAppAdPreferences?.adTag = adTag
        startAppAdPreferences?.minCpm = ecpm
            if (bannerFormat == "banner") {
                adView = WeakReference(
                    Banner(
                        context,
                        startAppAdPreferences,
                        bannerListener
                    )
                )
                (adView!!.get() as Banner).loadAd(320, 50)
            } else if (bannerFormat == "mrec") {
                adView =
                    WeakReference(Mrec(context, startAppAdPreferences, bannerListener))
                (adView!!.get() as Mrec).loadAd(300, 250)
            }


    }

    override fun destroy() {
        adView?.clear()
        cachedAd?.clear()
    }

    override fun showOnView(view: WeakReference<View>, density: Float): Boolean {
        return try {
            val weightAndHeight: Array<Int> = when (bannerFormat) {
                "mrec" -> arrayOf(300, 250)
                "banner" -> arrayOf(320, 50)
                else -> arrayOf(0, 0)
            }
            (view.get() as FrameLayout).addView(
                cachedAd!!.get(),
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
        return true
    }

    override fun revenue(): Double? {
        return startAppAdPreferences?.minCpm
    }
}