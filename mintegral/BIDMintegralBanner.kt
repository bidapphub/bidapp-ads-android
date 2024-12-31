package io.bidapp.networks.mintegral

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.mbridge.msdk.mbbid.out.BannerBidRequestParams
import com.mbridge.msdk.mbbid.out.BidListennning
import com.mbridge.msdk.mbbid.out.BidLossCode
import com.mbridge.msdk.mbbid.out.BidManager
import com.mbridge.msdk.mbbid.out.BidResponsed
import com.mbridge.msdk.out.BannerAdListener
import com.mbridge.msdk.out.BannerSize
import com.mbridge.msdk.out.MBBannerView
import com.mbridge.msdk.out.MBridgeIds
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


private const val MREC = "mrec"
private const val BANNER = "banner"
private const val LEADERBOARD = "leaderBoard"

@PublishedApi
internal class BIDMintegralBanner(
    private val adapter: BIDBannerAdapterProtocol,
    private val tagId: String?,
    format: AdFormat
) :
    BIDBannerAdapterDelegateProtocol, BannerAdListener {
    private val TAG = "Banner Mintegral"
    private val formatTag = tagId?.split("#", limit = 2)
    val placementId = formatTag?.getOrNull(0)
    val unitId = formatTag?.getOrNull(1)
    private var adView: WeakReference<MBBannerView>? = null
    private var cachedAd: Boolean = false
    private val bannerFormat = if (format.isBanner_320x50) Pair(BannerSize.SMART_TYPE, BANNER)
    else if (format.isBanner_300x250) Pair(BannerSize.MEDIUM_TYPE, MREC)
    else if (format.isBanner_728x90) Pair(BannerSize.SMART_TYPE, LEADERBOARD)
    else {
        BIDLog.d(TAG, "Unsupported Iron Source banner format: ${format.name()}")
        null
    }

    override fun load(context: Any, bidAppBid: BidappBid?) {
        cachedAd = false
        if (context as? Context == null || bannerFormat == null) {
            adapter.onFailedToLoad(Error("Banner IronSource loading error." + if (context as? Activity == null) "Activity" else "Format" + "is null"))
            return
        }
        if (placementId.isNullOrEmpty() || unitId.isNullOrEmpty()) {
            adapter.onFailedToLoad(Error("IronSource banner" + if (placementId.isNullOrEmpty()) "placement ID" else "unit ID" + "is null or empty"))
            return
        }
        adView = WeakReference(MBBannerView(context))
        adView?.get()?.init(BannerSize(bannerFormat.first, 0, 0), placementId, unitId)
        adView?.get()?.setBannerAdListener(this)
        adView?.get()?.setAllowShowCloseBtn(false)
        adView?.get()?.setRefreshTime(0)
        if (bidAppBid?.nativeBid as? String != null) {
            adView?.get()?.loadFromBid(bidAppBid.nativeBid as? String)
        } else {
            adView?.get()?.load()
        }

    }

    override fun load(context: Any) {
        load(context, null)
    }

    override fun revenue(): Double? {
        return null
    }

    override fun nativeAdView(): WeakReference<View>? {
        return WeakReference(adView?.get())
    }

    override fun isAdReady(): Boolean {
        return cachedAd
    }

    override fun destroy() {
        cachedAd = false
        adView?.get()?.release()
        adView?.clear()
        adView = null
    }

    override fun showOnView(view: WeakReference<View>, density: Float): Boolean {
        return try {
            val weightAndHeight: Array<Int> = when {
                bannerFormat?.second == MREC -> arrayOf(300, 250)
                bannerFormat?.second == BANNER -> arrayOf(320, 50)
                bannerFormat?.second == LEADERBOARD -> arrayOf(728, 90)
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
        return true
    }

    override fun onLoadFailed(p0: MBridgeIds?, p1: String?) {
        cachedAd = false
        BIDLog.d(TAG, "failed to load banner ad $placementId. Error: ${p1 ?: "Unknown error"}")
        adapter.onFailedToLoad(Error(p1 ?: "Unknown error"))
    }

    override fun onLoadSuccessed(p0: MBridgeIds?) {
        cachedAd = true
        adapter.onLoad()
        BIDLog.d(TAG, "Banner ad loaded: $placementId")
    }

    override fun onLogImpression(p0: MBridgeIds?) {
        adapter.onDisplay()
        BIDLog.d(TAG, "Banner ad displayed: $placementId")
    }

    override fun onClick(p0: MBridgeIds?) {
        adapter.onClick()
        BIDLog.d(TAG, "Banner ad clicked: $placementId")
    }

    override fun onLeaveApp(p0: MBridgeIds?) {
        cachedAd = false
        adapter.onHide()
        BIDLog.d(TAG, "Banner ad hide: $placementId")
    }

    override fun showFullScreen(p0: MBridgeIds?) {
        BIDLog.d(TAG, "Banner ad show fullscreen: $placementId")
    }

    override fun closeFullScreen(p0: MBridgeIds?) {
        BIDLog.d(TAG, "Banner ad close fullscreen: $placementId")
    }

    override fun onCloseBanner(p0: MBridgeIds?) {
        BIDLog.d(TAG, "Banner ad close: $placementId")
    }

    companion object {
        fun bid(
            context: Context?,
            request: BidappBidRequester,
            tagId: String?,
            appId: String?,
            accountId: String?,
            adFormat: AdFormat?,
            bidCompletion: bid_completion
        ) {
            val format = tagId?.split("#", limit = 2)
            val placementId = format?.getOrNull(0)
            val unitId = format?.getOrNull(1)
            if (placementId == null) {
                bidCompletion.invoke(null, Error("Mintegral banner bid failed placement ID is null"))
                return
            }
            if (unitId == null) {
                bidCompletion.invoke(null, Error("Mintegral banner bid failed unit ID is null"))
                return
            }
            val size = when {
                adFormat?.isBanner_320x50 == true -> Pair(320, 50)
                adFormat?.isBanner_300x250 == true -> Pair(300, 250)
                adFormat?.isBanner_728x90 == true -> Pair(728, 90)
                else -> Pair(0, 0)
            }

            val bidManager = BidManager(BannerBidRequestParams(placementId, unitId, size.first, size.second))
            bidManager.setBidListener(object : BidListennning {
                override fun onFailed(p0: String?) {
                    bidCompletion.invoke(null, Error("Mintgral bid ${p0 ?: "Unknown error"}"))
                }

                override fun onSuccessed(p0: BidResponsed?) {
                    if (p0 == null) {
                        bidCompletion.invoke(
                            null,
                            Error("Mintegral banner bid failed. BidResponse is null ")
                        )
                    } else {
                        val notifyListener = object : BIDNativeBidNotify {
                            override fun winNotify(secPrice: Double?, secBidder: String?) {
                                if (context != null) p0.sendWinNotice(context)
                            }

                            override fun loseNotify(
                                firstPrice: Double?,
                                firstBidder: String?,
                                lossReason: Int?
                            ) {
                                if (context != null) p0.sendLossNotice(
                                    context,
                                    BidLossCode.bidPriceNotHighest()
                                )
                            }
                        }
                        request.requestBidsWithCompletion(
                            BIDNativeBidData(
                                p0.bidToken,
                                p0.price.toDoubleOrNull() ?: 0.0,
                                notifyListener
                            ),
                            tagId,
                            appId,
                            accountId,
                            adFormat,
                            BIDMintegralSDK.testMode,
                            BIDMintegralSDK.COPPA,
                            bidCompletion
                        )


                    }
                }
            })
            bidManager.bid()
        }
    }

}