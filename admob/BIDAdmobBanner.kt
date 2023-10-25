package io.bidapp.networks.admob

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.RequiresApi
import androidx.core.view.get
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
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
    var adapter: BIDBannerAdapterProtocol?,
    var adTag: String?,
    format: AdFormat?
) : BIDBannerAdapterDelegateProtocol {
    val TAG = "Banner Admob"
    val bannerFormat = if (format?.isbanner_320x50 == true) AdSize.BANNER
    else if (format?.isbanner_300x250 == true) AdSize.MEDIUM_RECTANGLE
    else {
        BIDLog.d(TAG, "Unsuported Admob banner format: $format")
        null
    }
    var adView: WeakReference<AdView>? = null
    var ready = false

    val adListener = object : AdListener() {
        override fun onAdClicked() {
            super.onAdClicked()
            BIDLog.d(TAG, "on ad clicked")
            adapter?.onClick()
        }

        override fun onAdClosed() {
            super.onAdClosed()
            BIDLog.d(TAG, "on ad closed")
        }

        override fun onAdFailedToLoad(p0: LoadAdError) {
            super.onAdFailedToLoad(p0)
            BIDLog.d(TAG, "Admob failed to load ad. Error: ${p0.message}")
            adapter?.onFailedToLoad(Error(p0.message))
        }

        override fun onAdImpression() {
            super.onAdImpression()
            BIDLog.d(TAG, "show")
            adapter?.onDisplay()
        }

        override fun onAdLoaded() {
            super.onAdLoaded()
            BIDLog.d(TAG, "loaded")
            ready = true
            adapter?.onLoad()
        }

        override fun onAdOpened() {
            super.onAdOpened()
            BIDLog.d(TAG, "on ad open")
        }

        override fun onAdSwipeGestureClicked() {
            super.onAdSwipeGestureClicked()
            BIDLog.d(TAG, "on ad swipe gesture clicked")
        }
    }


    override fun nativeAdView(): WeakReference<View>? {
        return WeakReference(adView?.get() as View)
    }

    override fun isAdReady(): Boolean {
        return ready
    }

    override fun load(activity: Activity) {
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
            val loading = runCatching {
                if (adView == null) {
                    adView = WeakReference(AdView(activity.applicationContext))
                    adView!!.get()!!.setAdSize(bannerFormat!!)
                    adView!!.get()!!.adUnitId = adTag!!
                    adView!!.get()!!.adListener = adListener
                }
                adView!!.get()!!.loadAd(request)
            }
            if (loading.isFailure) adapter?.onFailedToLoad(Error("Admob banner loading error"))

    }

    override fun prepareForDealloc() {
        //   prepareForDealloc
    }


    override fun showOnView(view: WeakReference<View>, activity: Activity): Boolean {
        try {
            val density = activity.resources.displayMetrics.density
            val weightAndHeight: Array<Int> = when (bannerFormat) {
                AdSize.MEDIUM_RECTANGLE -> arrayOf(300, 250)
                AdSize.BANNER -> arrayOf(320, 50)
                else -> arrayOf(0, 0)
            }
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
            BIDLog.d(TAG, "Show on view is failed ${e.message}")
            return false
        }
    }

    override fun waitForAdToShow(): Boolean {
        return true
    }


}

