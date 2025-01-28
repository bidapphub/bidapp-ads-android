package io.bidapp.networks.bigoads

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import io.bidapp.sdk.AdFormat
import io.bidapp.sdk.BIDLog
import io.bidapp.sdk.bid.BIDNativeBidData
import io.bidapp.sdk.bid.BIDNativeBidNotify
import io.bidapp.sdk.bid.BidappBid
import io.bidapp.sdk.bid.BidappBidRequester
import io.bidapp.sdk.mediation.bid_completion
import io.bidapp.sdk.protocols.BIDBannerAdapterDelegateProtocol
import io.bidapp.sdk.protocols.BIDBannerAdapterProtocol
import sg.bigo.ads.ad.banner.BigoAdView
import sg.bigo.ads.api.AdError
import sg.bigo.ads.api.AdInteractionListener
import sg.bigo.ads.api.AdLoadListener
import sg.bigo.ads.api.AdSize
import sg.bigo.ads.api.BannerAdRequest
import java.lang.ref.WeakReference

@PublishedApi
internal class BIDBigoAdsBanner(
    var adapter: BIDBannerAdapterProtocol,
    var slotId: String?,
    format: AdFormat
) : BIDBannerAdapterDelegateProtocol, AdLoadListener<BigoAdView>, AdInteractionListener {

    private val TAG = "Banner BigoAds"
    private var handler : Handler? = null
    private var task : Runnable? = null
    private var adView: WeakReference<BigoAdView>? = null
    private var cachedAd: WeakReference<BigoAdView>? = null
    private val bannerFormat = if (format.isBanner_320x50) AdSize.BANNER
    else if (format.isBanner_300x250) AdSize.MEDIUM_RECTANGLE
    else if (format.isBanner_728x90) AdSize.LEADERBOARD
    else {
        BIDLog.d(TAG, "Unsupported BigoAds banner format: ${format.name()}")
        null
    }

    override fun nativeAdView(): WeakReference<View>? {
        return WeakReference(adView?.get() as? View)
    }

    override fun isAdReady(): Boolean {
        return cachedAd != null
    }

    override fun destroy() {
        handler?.removeCallbacksAndMessages(null)
        handler = null
        task = null
        adView?.get()?.removeAllViews()
        adView?.get()?.destroy()
        adView = null
        cachedAd = null
    }

    override fun showOnView(view: WeakReference<View>, density: Float): Boolean {
        return try {
            val weightAndHeight =
                when (bannerFormat) {
                    AdSize.BANNER -> arrayOf(
                        320,
                        50
                    )
                    AdSize.MEDIUM_RECTANGLE -> arrayOf(
                        300,
                        250
                    )
                    AdSize.LEADERBOARD -> arrayOf(
                        728,
                        90
                    )
                    else -> arrayOf(0, 0)
                }
            adView?.get()?.setAdInteractionListener(this)
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
        return false
    }

    override fun load(context: Any) {
        load(context, null)
    }

    override fun load(context: Any, bidAppBid: BidappBid?) {
        cachedAd = null
        if (bidAppBid?.nativeBid != null) {
            val bannerBidAds = (bidAppBid.nativeBid as BigoAdView)
            adView = WeakReference(bannerBidAds)
            cachedAd = WeakReference(bannerBidAds)
            if (handler == null) handler = Handler(Looper.getMainLooper())
            task = Runnable {
                adapter.onLoad()
            }
            handler?.postDelayed(task!!, 500)
            return
        }
        if (context as? Context == null || bannerFormat == null) {
            adapter.onFailedToLoad(Error("BigoAds banner loading error"))
            return
        }
        if (slotId.isNullOrEmpty()) {
            adapter.onFailedToLoad(Error("BigoAds banner slotId is null or empty"))
            return
        }
        adView = WeakReference(BigoAdView(context))
        val request = if (bidAppBid != null) BannerAdRequest.Builder().withSlotId(slotId)
            .withBid(bidAppBid.nativeBid.toString()).withAdSizes(bannerFormat).build()
        else BannerAdRequest.Builder().withSlotId(slotId).withAdSizes(bannerFormat).build()
        adView?.get()?.setAdLoadListener(this)
        adView?.get()?.loadAd(request)
    }

    override fun revenue(): Double? {
        return null
    }

    override fun onError(p0: AdError) {
        cachedAd = null
        BIDLog.d(TAG, "Ad failed to load. Error: ${p0.message}")
        adapter.onFailedToLoad(Error(p0.message.toString()))
    }

    override fun onAdLoaded(p0: BigoAdView) {
        task?.let { handler?.removeCallbacks(it) }
        handler = null
        task = null
        cachedAd = WeakReference(p0)
        adapter.onLoad()
        BIDLog.d(TAG, "Ad load")
    }

    override fun onAdError(p0: AdError) {
        cachedAd = null
        BIDLog.d(TAG, "Ad failed to display. Error: ${p0.message}")
        adapter.onFailedToDisplay(Error(p0.message))
    }

    override fun onAdImpression() {
        BIDLog.d(TAG, "Ad display")
        adapter.onDisplay()
    }

    override fun onAdClicked() {
        BIDLog.d(TAG, "Ad clicked ")
        adapter.onClick()
    }


    override fun onAdOpened() {
        BIDLog.d(TAG, "Ad open")
    }

    override fun onAdClosed() {
        BIDLog.d(TAG, "Ad close")
    }

    companion object {
        fun bid(
            context: Context?,
            request: BidappBidRequester,
            slotId: String?,
            appId: String?,
            accountId: String?,
            adFormat: AdFormat?,
            bidCompletion: bid_completion
        ) {
            val TAG = "BigoAds banner bid"
            if (context == null) {
                bidCompletion.invoke(null, Error("BigoAds banner bid error : context is null"))
                return
            }
            val bannerFormat = if (adFormat?.isBanner_320x50 == true) AdSize.BANNER
            else if (adFormat?.isBanner_300x250 == true) AdSize.MEDIUM_RECTANGLE
            else if (adFormat?.isBanner_728x90 == true) AdSize.LEADERBOARD
            else {
                bidCompletion.invoke(null, Error("Unsupported BigoAds banner format"))
                return
            }
            val adLoadListener = object : AdLoadListener<BigoAdView> {
                override fun onError(p0: AdError) {
                    bidCompletion.invoke(null, Error(p0.message.toString()))
                }

                override fun onAdLoaded(bidAds: BigoAdView) {
                    val notifyListener = object : BIDNativeBidNotify {
                        override fun winNotify(secPrice: Double?, secBidder: String?) {
                            bidAds.bid?.notifyWin(secPrice, secBidder)
                        }

                        override fun loseNotify(
                            firstPrice: Double?,
                            firstBidder: String?,
                            lossReason: Int?
                        ) {
                            bidAds.bid?.notifyLoss(firstPrice, firstBidder, lossReason ?: 102)
                        }
                    }
                    request.requestBidsWithCompletion(
                        BIDNativeBidData(bidAds, bidAds.bid?.price ?: 0.0, notifyListener),
                        slotId,
                        appId,
                        accountId,
                        adFormat,
                        BIDBigoAdsSDK.testMode,
                        BIDBigoAdsSDK.coppa,
                        bidCompletion
                    )
                }
            }
            val bannerAdRequest =
                BannerAdRequest.Builder().withAdSizes(bannerFormat).withSlotId(slotId).build()
            val nativeView = BigoAdView(context)
            nativeView.setAdLoadListener(adLoadListener)
            nativeView.loadAd(bannerAdRequest)
        }
    }
}