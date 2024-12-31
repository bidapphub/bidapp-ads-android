package io.bidapp.networks.inmobi

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.inmobi.ads.AdMetaInfo
import com.inmobi.ads.InMobiAdRequestStatus
import com.inmobi.ads.InMobiBanner
import com.inmobi.ads.InMobiInterstitial
import com.inmobi.ads.listeners.BannerAdEventListener
import com.inmobi.ads.listeners.InterstitialAdEventListener
import io.bidapp.sdk.AdFormat
import io.bidapp.sdk.BIDLog
import io.bidapp.sdk.bid.BIDNativeBidData
import io.bidapp.sdk.bid.BIDNativeBidNotify
import io.bidapp.sdk.bid.BidappBid
import io.bidapp.sdk.bid.BidappBidRequester
import io.bidapp.sdk.mediation.bid_completion
import io.bidapp.sdk.protocols.BIDBannerAdapterDelegateProtocol
import io.bidapp.sdk.protocols.BIDBannerAdapterProtocol
import java.lang.ref.WeakReference


private const val BANNER = "banner"
private const val MREC = "mrec"
private const val LEADERBOARD = "leader_board"

@PublishedApi
internal class BIDInMobiBanner(
    private val adapter: BIDBannerAdapterProtocol,
    private val placementId: String?,
    format: AdFormat
) :
    BIDBannerAdapterDelegateProtocol, BannerAdEventListener() {
    private val TAG = "Banner InMobi"
    private var adView: WeakReference<InMobiBanner>? = null
    private var cachedAd: WeakReference<InMobiBanner>? = null
    private val bannerFormat = if (format.isBanner_320x50) BANNER
    else if (format.isBanner_300x250) MREC
    else if (format.isBanner_728x90) LEADERBOARD
    else {
        BIDLog.d(TAG, "Unsupported Iron Source banner format: ${format.name()}")
        null
    }

    override fun revenue(): Double? {
        return null
    }


    override fun load(context: Any) {
        load(context, null)
    }

    override fun load(context: Any, bidAppBid: BidappBid?) {
        cachedAd = null
        if (bidAppBid?.nativeBid != null){
            val banner = bidAppBid.nativeBid as InMobiBanner
            adView = WeakReference(banner)
        }
        else {
            if (context as? Context == null || bannerFormat == null) {
                adapter.onFailedToLoad(Error("Banner InMobi loading error." + if (context as? Context == null) "Context" else "Format" + "is null"))
                return
            }
            if (bidAppBid == null && (placementId.isNullOrEmpty() || placementId.toLongOrNull() == null)) {
                adapter.onFailedToLoad(Error("InMobi banner Placement ID is null or empty"))
                return
            }
            adView = WeakReference(InMobiBanner(context, placementId!!.toLong()))
        }
        adView?.get()?.setAnimationType(InMobiBanner.AnimationType.ANIMATION_OFF)
        adView?.get()?.setEnableAutoRefresh(false)
        adView?.get()?.setListener(this)
        val size = when (bannerFormat) {
            BANNER -> Pair(320, 50)
            MREC -> Pair(300, 250)
            LEADERBOARD -> Pair(728, 90)
            else -> Pair(0, 0)
        }
        val density = (context as Context).resources?.displayMetrics?.density ?: 1.0f
        adView?.get()?.layoutParams = ViewGroup.LayoutParams(
            Math.round(size.first * density),
            Math.round(size.second * density)
        )
        if (bidAppBid == null) {
            adView?.get()?.load()
        }
        else {
            adView?.get()?.preloadManager?.load()
        }

    }

    override fun nativeAdView(): WeakReference<View>? {
        return WeakReference(adView?.get())
    }

    override fun isAdReady(): Boolean {
        return cachedAd != null
    }

    override fun destroy() {
        adView?.get()?.destroy()
        cachedAd?.get()?.destroy()
        adView?.clear()
        cachedAd?.clear()
        adView = null
        cachedAd = null
    }

    override fun showOnView(view: WeakReference<View>, density: Float): Boolean {
        return try {
            val weightAndHeight: Array<Int> = when (bannerFormat) {
                MREC -> arrayOf(300, 250)
                BANNER -> arrayOf(320, 50)
                LEADERBOARD -> arrayOf(728, 90)
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
            BIDLog.d(TAG, "Show on view is failed. Error : ${e.message}")
            false
        }
    }

    override fun waitForAdToShow(): Boolean {
        return true
    }

    override fun activityNeededForLoad(): Boolean {
        return false
    }


    override fun onAdLoadSucceeded(p0: InMobiBanner, p1: AdMetaInfo) {
        cachedAd = WeakReference(p0)
        adapter.onLoad()
        BIDLog.d(TAG, "Banner ad loaded: $placementId")
    }

    override fun onAdLoadFailed(p0: InMobiBanner, p1: InMobiAdRequestStatus) {
        cachedAd = null
        BIDLog.d(TAG, "Failed to load banner ad $placementId. Error: ${p1.message}")
        adapter.onFailedToLoad(Error(p1.message))
    }

    override fun onAdDisplayed(p0: InMobiBanner) {
        BIDLog.d(TAG, "Banner ad displayed: $placementId")
    }

    override fun onAdImpression(p0: InMobiBanner) {
        adapter.onDisplay()
        BIDLog.d(TAG, "Banner ad impression: $placementId")
    }

    override fun onAdDismissed(p0: InMobiBanner) {
        BIDLog.d(TAG, "Banner ad dismissed. $placementId")
        adapter.onHide()
        cachedAd = null
    }

    override fun onAdClicked(p0: InMobiBanner, p1: MutableMap<Any, Any>?) {
        adapter.onClick()
        BIDLog.d(TAG, "Banner ad clicked: $placementId")
    }

    override fun onUserLeftApplication(p0: InMobiBanner) {
        BIDLog.d(TAG, "AdView will leave application: $placementId")
    }

    companion object {
        fun bid(
            context: Context?,
            request: BidappBidRequester,
            placementId: String?,
            appId: String?,
            accountId: String?,
            adFormat: AdFormat?,
            bidCompletion: bid_completion
        ) {
            if (context == null || placementId == null || placementId.toLongOrNull() == null) {
                bidCompletion.invoke(
                    null,
                    Error("InMobi bid is failed." + if (context == null) "Context" else "Placement ID" + "is null")
                )
                return
            }
            var banner: InMobiBanner? = null
            val bannerAdListener = object : BannerAdEventListener() {
                override fun onAdFetchFailed(p0: InMobiBanner, p1: InMobiAdRequestStatus) {
                    bidCompletion.invoke(
                        null,
                        Error("InMobi bid is failed. Banner instance is null")
                    )
                }

                override fun onAdFetchSuccessful(p0: InMobiBanner, p1: AdMetaInfo) {
                    val notifyListener = object : BIDNativeBidNotify {
                        override fun winNotify(secPrice: Double?, secBidder: String?) {}
                        override fun loseNotify(
                            firstPrice: Double?,
                            firstBidder: String?,
                            lossReason: Int?
                        ) {
                        }
                    }
                    if (banner != null) {
                        request.requestBidsWithCompletion(
                            BIDNativeBidData(banner!!, p1.bid, notifyListener),
                            placementId,
                            appId,
                            accountId,
                            adFormat,
                            BIDInMobiSDK.testMode,
                            BIDInMobiSDK.COPPA,
                            bidCompletion
                        )
                    } else {
                        bidCompletion.invoke(
                            null,
                            Error("InMobi bid is failed. Banner instance is null")
                        )
                    }
                }
            }
            banner = InMobiBanner(context, placementId.toLong())
            when {
                adFormat?.isBanner_320x50 == true -> banner.setBannerSize(320, 50)
                adFormat?.isBanner_300x250 == true -> banner.setBannerSize(300, 250)
                adFormat?.isBanner_728x90 == true -> banner.setBannerSize(728, 90)
            }
            banner.setListener(bannerAdListener)
            banner.preloadManager.preload()
        }
    }
}