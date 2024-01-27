package io.bidapp.networks.liftoff

import android.content.Context
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
internal class BIDLiftoffBanner(adapter: BIDBannerAdapterProtocol, val adTag: String?, format: AdFormat) :
    BIDBannerAdapterDelegateProtocol, BannerAdListener {
    var adapter: BIDBannerAdapterProtocol? = adapter
    var bannerAd: WeakReference<BannerAd>? = null
    var adView: WeakReference<BannerView>? = null
    var cachedAd: String? = null
    val TAG = "Banner Liftoff"
    val bannerFormat = if (format.isBanner_320x50) BannerAdSize.BANNER
    else if (format.isBanner_300x250) BannerAdSize.VUNGLE_MREC
    else {
        BIDLog.d(TAG, "Unsupported Liftoff banner format: $format")
        null
    }


    override fun nativeAdView(): WeakReference<View>? {
        return WeakReference(adView?.get() as? View)
    }

    override fun isAdReady(): Boolean {
        return cachedAd != null
    }

    override fun load(context: Any) {
        cachedAd = null
        if (context as? Context == null || bannerFormat == null){
            adapter?.onFailedToLoad(Error("banner loading error"))
            return
        }
        if (adTag == null){
            adapter?.onFailedToLoad(Error("Liftoff banner adtag is null"))
            return
        }
        if (bannerAd == null) {
            bannerAd = WeakReference(BannerAd(context, adTag!!, bannerFormat))
        }
            bannerAd?.get()?.adListener = this
            bannerAd?.get()?.load()

    }



    override fun destroy() {
       cachedAd = null
       adView?.get()?.removeAllViews()
    }

    override fun showOnView(view: WeakReference<View>, density: Float): Boolean {
        return try {
            val weightAndHeight: Array<Int> = when (bannerFormat) {
                BannerAdSize.VUNGLE_MREC -> arrayOf(300, 250)
                BannerAdSize.BANNER -> arrayOf(320, 50)
                else -> arrayOf(0, 0)
            }
            adView = WeakReference(bannerAd!!.get()!!.getBannerView())
            (view.get() as FrameLayout).addView(
                adView!!.get(),
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
        return false
    }

    override fun onAdClicked(baseAd: BaseAd) {
        BIDLog.d(TAG, "ad click. adtag: ($adTag)")
        adapter?.onClick()
    }

    override fun onAdEnd(baseAd: BaseAd) {
        BIDLog.d(TAG, "ad end. adtag: ($adTag)")
        adapter?.onHide()
        cachedAd = null
    }

    override fun onAdFailedToLoad(baseAd: BaseAd, adError: VungleError) {
        BIDLog.d(TAG, "failed to load ad. Error: ${adError.message} adtag: ($adTag)")
        adapter?.onFailedToLoad(Error(adError.message))
    }

    override fun onAdFailedToPlay(baseAd: BaseAd, adError: VungleError) {
        BIDLog.d(TAG, "failed to play ${adError.message} adtag: ($adTag)")
        adapter?.onFailedToDisplay(Error(adError.message))
    }

    override fun onAdImpression(baseAd: BaseAd) {
        BIDLog.d(TAG, "ad displayed. adtag: ($adTag)")
        adapter?.onDisplay()
    }

    override fun onAdLeftApplication(baseAd: BaseAd) {
        cachedAd = null
        BIDLog.d(TAG, "ad left application. adtag: ($adTag)")
    }
    override fun onAdLoaded(baseAd: BaseAd) {
        cachedAd = baseAd.placementId
        adapter?.onLoad()
        BIDLog.d(TAG, "ad loaded. adtag: ($adTag)")
    }

    override fun onAdStart(baseAd: BaseAd) {
        BIDLog.d(TAG, "ad start. adtag: ($adTag)")
    }

    override fun revenue(): Double? {
        return null
    }
}

