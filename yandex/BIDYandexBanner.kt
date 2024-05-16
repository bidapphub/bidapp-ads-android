package io.bidapp.networks.yandex

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.yandex.mobile.ads.banner.BannerAdEventListener
import com.yandex.mobile.ads.banner.BannerAdSize
import com.yandex.mobile.ads.banner.BannerAdView
import com.yandex.mobile.ads.common.AdRequest
import com.yandex.mobile.ads.common.AdRequestError
import com.yandex.mobile.ads.common.ImpressionData
import io.bidapp.sdk.AdFormat
import io.bidapp.sdk.BIDLog
import io.bidapp.sdk.protocols.BIDBannerAdapterDelegateProtocol
import io.bidapp.sdk.protocols.BIDBannerAdapterProtocol
import java.lang.ref.WeakReference

class BIDYandexBanner(
    private val adapter: BIDBannerAdapterProtocol,
    private val adUnitId: String?,
    private val format: AdFormat
) :
    BIDBannerAdapterDelegateProtocol, BannerAdEventListener {

    var adView: WeakReference<BannerAdView>? = null
    val TAG = "Banner Yandex"
    private var bannerFormat: BannerAdSize? = null
    private var isCachedAd = false
    private var size: String? = null


    override fun nativeAdView(): WeakReference<View>? {
        return WeakReference(adView?.get() as? View)
    }

    override fun isAdReady(): Boolean {
        return isCachedAd
    }

    override fun load(context: Any) {
        if (context as? Activity == null) {
            adapter.onFailedToLoad(Error("Yandex banner loading error"))
            return
        }
        bannerFormat = when(format.currentFormat()){
            "banner_320x50" -> {
                size = "banner"
                BannerAdSize.fixedSize((context as Activity).applicationContext, 320, 50)
            }
            "banner_300x250" -> {
                size = "mrec"
                BannerAdSize.fixedSize((context as Activity).applicationContext, 300, 250)
            }
            "banner_728x90" -> {
                size = "leaderboard"
                BannerAdSize.fixedSize((context as Activity).applicationContext, 728, 90)
            }
            else -> {
                adapter.onFailedToLoad(Error("Unsupported Yandex banner format: ${format?.name()}"))
                return
            }
        }
        if (adUnitId.isNullOrEmpty()) {
            adapter.onFailedToLoad(Error("Yandex banner adUnitId is null or empty"))
            return
        }
        isCachedAd = false
        val loadBannerAdRunnable = Runnable {
            if (adView?.get() == null) {
                adView = WeakReference(BannerAdView((context as Activity).applicationContext))
            }
            adView?.get()?.setAdUnitId(adUnitId)
            adView?.get()?.setAdSize(bannerFormat!!)
            adView?.get()?.setBannerAdEventListener(this)
            adView?.get()?.loadAd(AdRequest.Builder().build())
        }
        (context as Activity).runOnUiThread(loadBannerAdRunnable)
    }

    override fun destroy() {
        adView?.get()?.destroy()
        adView?.clear()
    }

    override fun showOnView(view: WeakReference<View>, density: Float): Boolean {
        return try {
            val weightAndHeight: Array<Int> = when (size) {
                "mrec" -> arrayOf(300, 250)
                "banner" -> arrayOf(320, 50)
                "leaderboard" -> arrayOf(728, 90)
                else -> {
                    adapter.onFailedToLoad(Error("Unsupported Yandex banner format: $format"))
                    return false
                }
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

    override fun onAdLoaded() {
        isCachedAd = true
        adapter.onLoad()
        BIDLog.d(TAG, "ad loaded. adUnitId: $adUnitId")
    }

    override fun onAdFailedToLoad(error: AdRequestError) {
        isCachedAd = false
        BIDLog.d(TAG, "failed to load ad. Error: ${error.description} adUnitId: ($adUnitId)")
        adapter.onFailedToLoad(Error(error.description))
    }

    override fun onAdClicked() {
        BIDLog.d(TAG, "ad click. adUnitId: ($adUnitId)")
        adapter.onClick()
    }

    override fun onLeftApplication() {
        BIDLog.d(TAG, "Yandex banner on left application")
    }

    override fun onReturnedToApplication() {
        BIDLog.d(TAG, "Yandex banner on returned to application")
    }

    override fun onImpression(impressionData: ImpressionData?) {
        BIDLog.d(TAG, "ad on impression. adUnitId: ($adUnitId)")
        adapter.onDisplay()
    }

    override fun revenue(): Double? {
        return null
    }
}