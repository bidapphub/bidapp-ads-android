package io.bidapp.networks.pangle

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.bytedance.sdk.openadsdk.api.banner.PAGBannerAd
import com.bytedance.sdk.openadsdk.api.banner.PAGBannerAdInteractionListener
import com.bytedance.sdk.openadsdk.api.banner.PAGBannerAdLoadListener
import com.bytedance.sdk.openadsdk.api.banner.PAGBannerRequest
import com.bytedance.sdk.openadsdk.api.banner.PAGBannerSize
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

@PublishedApi
internal class BIDPangleBanner(
    private val adapter: BIDBannerAdapterProtocol,
    private val placementID: String?,
    private val format: AdFormat
) :
    BIDBannerAdapterDelegateProtocol, PAGBannerAdLoadListener, PAGBannerAdInteractionListener {
    private var adView: WeakReference<PAGBannerAd>? = null
    private var cachedAd: Boolean? = null
    private val TAG = "Banner Pangle"
    private var handler : Handler? = null
    private var task : Runnable? = null
    private val bannerFormat = if (format.isBanner_320x50) PAGBannerSize.BANNER_W_320_H_50
    else if (format.isBanner_300x250) PAGBannerSize.BANNER_W_300_H_250
    else if (format.isBanner_728x90) PAGBannerSize.BANNER_W_728_H_90
    else {
        BIDLog.d(TAG, "Unsupported Pangle banner format: ${format?.name()}")
        null
    }

    override fun load(context: Any, bidAppBid: BidappBid?) {
        cachedAd = null
        if (bidAppBid != null){
            adView = WeakReference((bidAppBid.nativeBid as? PAGBannerAd))
            if (adView?.get() == null){
                adapter.onFailedToLoad(Error("Pangle bidding banner load is failed. Error cast is failed"))
                return
            }
            if (handler == null) handler = Handler(Looper.getMainLooper())
            task = Runnable {
                adapter.onLoad()
            }
            handler?.postDelayed(task!!, 1000)
            return
        }
        if (placementID.isNullOrEmpty()) {
            adapter.onFailedToLoad(Error("Pangle banner placementID is null or empty"))
            return
        }
        if (bannerFormat == null) {
            adapter.onFailedToLoad(Error("Unsupported Pangle banner format: ${format.name()}"))
            return
        }
        val request = PAGBannerRequest(bannerFormat)
        PAGBannerAd.loadAd(placementID, request, this)
    }

    override fun load(context: Any) {
        load(context, null)
    }

    override fun revenue(): Double? {
        return null
    }

    override fun nativeAdView(): WeakReference<View>? {
        return WeakReference(adView?.get()?.bannerView)
    }

    override fun isAdReady(): Boolean {
        return cachedAd == true
    }

    override fun destroy() {
        handler?.removeCallbacksAndMessages(null)
        handler = null
        task = null
        cachedAd = null
        adView?.get()?.destroy()
        adView?.clear()
    }

    override fun showOnView(view: WeakReference<View>, density: Float): Boolean {
        if (cachedAd == null){
            adapter.onFailedToDisplay(Error("Smaato banner not ready"))
        }
        return try {
            val weightAndHeight =
                when (bannerFormat) {
                    PAGBannerSize.BANNER_W_320_H_50 -> arrayOf(
                        320,
                        50
                    )
                    PAGBannerSize.BANNER_W_300_H_250 -> arrayOf(
                        300,
                        250
                    )
                    PAGBannerSize.BANNER_W_728_H_90 -> arrayOf(
                        728,
                        90
                    )
                    else -> {
                        throw IllegalArgumentException("Pangle banner size incorrect")
                    }
                }
            adView!!.get()!!.setAdInteractionListener(this)
            (view.get() as FrameLayout).addView(
                adView!!.get()!!.bannerView,
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

    // Pangle banner listener

    override fun onError(p0: Int, p1: String?) {
        cachedAd = null
        BIDLog.d(TAG, "Ad failed to load. Error: $p1. Code: $p0")
        adapter.onFailedToLoad(Error("Error: $p1. Code: $p0"))
    }

    override fun onAdLoaded(p0: PAGBannerAd?) {
        adView = WeakReference(p0)
        cachedAd = true
        adapter.onLoad()
        BIDLog.d(TAG, "Ad load")
    }

    override fun onAdShowed() {
        BIDLog.d(TAG, "Ad impression $placementID")
        adapter.onDisplay()
    }

    override fun onAdClicked() {
        BIDLog.d(TAG, "Ad clicked ")
        adapter.onClick()
    }

    override fun onAdDismissed() {
        BIDLog.d(TAG, "Ad hide")
        adapter.onHide()
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
            val bannerFormat = if (adFormat?.isBanner_320x50 == true) PAGBannerSize.BANNER_W_320_H_50
            else if (adFormat?.isBanner_300x250 == true) PAGBannerSize.BANNER_W_300_H_250
            else if (adFormat?.isBanner_728x90 == true) PAGBannerSize.BANNER_W_728_H_90
            else {
                 bidCompletion.invoke(null, Error("Pangle banner bidding error is incorrect banner format"))
                 return
            }
            if (placementId == null){
                bidCompletion.invoke(null, Error("Pangle banner bidding is failed Error: placement ID is null"))
                return
            }
            val adLoadListener = object : PAGBannerAdLoadListener {
                override fun onError(p0: Int, p1: String?) {
                    bidCompletion.invoke(null, Error("Error: $p1. Code: $p0"))
                }

                override fun onAdLoaded(p0: PAGBannerAd?) {
                    val notifyListener = object : BIDNativeBidNotify{
                        override fun winNotify(secPrice: Double?, secBidder: String?) {
                            p0?.win(secPrice)
                        }

                        override fun loseNotify(
                            firstPrice: Double?,
                            firstBidder: String?,
                            lossReason: Int?
                        ) {
                            p0?.loss(firstPrice, firstBidder, lossReason.toString() ?: "102")
                        }
                    }
                    if (p0 == null){
                        bidCompletion.invoke(null, Error("Pangle banner bid is null"))
                        return
                    }
                    if (p0.getExtraInfo("price") == null) {
                        bidCompletion.invoke(null, Error("Pangle banner bid failed"))
                        return
                    }
                    val price = p0.getExtraInfo("price").toString().toDoubleOrNull()
                    request.requestBidsWithCompletion(
                        BIDNativeBidData(p0, price ?: 0.0, notifyListener),
                        placementId,
                        appId,
                        accountId,
                        adFormat,
                        BIDPangleSDK.testMode,
                        BIDPangleSDK.COPPA,
                        bidCompletion
                    )

                }

            }

            val requestBanner = PAGBannerRequest(bannerFormat)
            PAGBannerAd.loadAd(placementId, requestBanner, adLoadListener)
        }
    }
}