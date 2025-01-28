package io.bidapp.networks.unity


import android.app.Activity
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
    private var adapter: BIDBannerAdapterProtocol?,
    private var adTag: String?,
    format: AdFormat?
) : BIDBannerAdapterDelegateProtocol, BannerView.IListener {

    private val TAG = "Banner Unity"
    private val bannerFormat = if (format?.isBanner_320x50 == true) UnityBannerSize.standard
    else if (format?.isBanner_300x250 == true) UnityBannerSize(300, 250)
    else if (format?.isBanner_728x90 == true) UnityBannerSize.leaderboard
    else {
        BIDLog.d(TAG, "Unsupported banner format: ${format?.name()}")
        null
    }
    private var adView: WeakReference<BannerView>? = null
    private var cachedAd: String? = null


    override fun nativeAdView(): WeakReference<View>? {
        return WeakReference(adView?.get() as? View)
    }

    override fun isAdReady(): Boolean {
        return cachedAd != null
    }

    override fun load(context: Any) {
        cachedAd = null
        if (context as? Activity == null || bannerFormat == null) {
            adapter?.onFailedToLoad(Error("Unity banner loading error"))
            return
        }
        if (adTag.isNullOrEmpty()) {
            adapter?.onFailedToLoad(Error("Unity banner adTag is null or empty"))
            return
        }
        adView = WeakReference(BannerView(context, adTag, bannerFormat))
        adView?.get()?.listener = this
        adView?.get()?.load()
    }

    override fun destroy() {
        cachedAd = null
        val destroy = runCatching {
            adView?.get()?.destroy()
        }
        if (destroy.isFailure) {
            BIDLog.d(TAG, "Banner destroy failed. adTag: ($adTag). Error ${ destroy.exceptionOrNull()?.localizedMessage }")
        }
        adView?.get()?.removeAllViews()
        adView = null

    }

    override fun showOnView(view: WeakReference<View>, density: Float): Boolean {
        return try {
            val weightAndHeight =
                if (bannerFormat == UnityBannerSize.standard) arrayOf(
                    320,
                    50
                ) else if (bannerFormat?.height == 250 && bannerFormat.width == 300) arrayOf(
                    300,
                    250
                )
                else if (bannerFormat == UnityBannerSize.leaderboard) arrayOf(
                    728,
                    90
                )
                else arrayOf(0, 0)
            (view.get() as FrameLayout).addView(
                adView!!.get(),
                0,
                ViewGroup.LayoutParams(
                    (weightAndHeight[0] * density).toInt(),
                    (weightAndHeight[1] * density).toInt()
                )
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

    override fun activityNeededForLoad(): Boolean {
        return true
    }


    override fun onBannerLoaded(ad: BannerView?) {
        cachedAd = ad?.placementId
        adapter?.onLoad()
        BIDLog.d(TAG, "Ad load. adTag: ($adTag)")
    }

    override fun onBannerShown(bannerAdView: BannerView?) {
        BIDLog.d(TAG, "Ad show. adTag: ($adTag)")
        adapter?.onDisplay()
    }

    override fun onBannerClick(bannerAdView: BannerView?) {
        BIDLog.d(TAG, "Ad click. adTag: ($adTag)")
        adapter?.onClick()
    }

    override fun onBannerFailedToLoad(bannerAdView: BannerView?, errorInfo: BannerErrorInfo?) {
        BIDLog.d(TAG, "Failed to load ad. Error: ${errorInfo?.errorCode}")
        adapter?.onFailedToLoad(Error(errorInfo?.errorCode.toString()))
    }

    override fun onBannerLeftApplication(bannerView: BannerView?) {
        BIDLog.d(TAG, "Banner left application. adTag: ($adTag)")
    }

    override fun revenue(): Double? {
        return null
    }
}



