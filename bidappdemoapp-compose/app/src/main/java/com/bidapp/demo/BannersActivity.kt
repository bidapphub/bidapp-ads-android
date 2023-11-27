package com.bidapp.demo

import android.os.Bundle
import android.util.Log
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import io.bidapp.sdk.AdFormat
import io.bidapp.sdk.AdInfo
import io.bidapp.sdk.BannerView
import io.bidapp.sdk.protocols.BIDBannerViewDelegate
import java.util.Timer
import java.util.TimerTask
import kotlin.concurrent.schedule
import kotlin.random.Random

class BannersActivity : ComponentActivity(), BIDBannerViewDelegate {
    private var items = mutableStateListOf<CellModel>()
    private var generateBannerTimer: TimerTask? = null
    private var removeBannerTimer: TimerTask? = null
    private var pendingBanners: MutableList<BannerView> = mutableListOf()
    private var bannerCount = 0
    private var isAfterDelete = false
    private var dataSource: MutableList<CellModel> = mutableListOf()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Main()
            Table()
        }

        addOneMoreBanner()
        scheduleRemoveOneBanner()
    }

    private fun addOneMoreBanner() {
        if (pendingBanners.size < 2) {
            val format =
                if (Random.nextInt(2) == 0) AdFormat.banner_320x50 else AdFormat.banner_300x250
            val banner = BannerView(this).banner(format)
            banner.setBannerViewDelegate(this)
            pendingBanners.add(banner)
            scheduleAddOneMoreBanner()
        }
    }

    private fun scheduleAddOneMoreBanner() {
        generateBannerTimer?.cancel()
        generateBannerTimer = Timer().schedule(10000) {
            runOnUiThread {
                addOneMoreBanner()
            }
        }
    }


    private fun reloadTable() {
        bannerCount = 0
        items.forEach {
            if (it.view.value.getChildAt(0) != null) {
                (it.view.value.getChildAt(0) as BannerView).destroy()
            }
            it.view.value.removeAllViews()
        }
        dataSource.clear()
        isAfterDelete = false
        items.forEach {
            it.color.value = Color.White
            it.showBorder.value = false
            it.height.value = 50.dp
            it.width.value = 320.dp
        }

    }

    private fun scheduleRemoveOneBanner() {
        removeBannerTimer?.cancel()
        removeBannerTimer = Timer().schedule(30000) {
            runOnUiThread {
                removeOneBanner()
            }
        }
    }

    private fun removeOneBanner() {
        var removedTheLastOne = false
        for (cellModel in dataSource.reversed()) {
            if ((cellModel.view.value).childCount > 0) {
                (cellModel.view.value.getChildAt(0) as BannerView).destroy()
                cellModel.view.value.removeViewAt(0)
                cellModel.color.value = Color.Red
                removedTheLastOne = (cellModel == dataSource.first())
                break
            }
        }
        if (removedTheLastOne) {
            isAfterDelete = true
            dataSource.clear()
            addOneMoreBanner()
        }
        scheduleRemoveOneBanner()
    }


    private fun addAdToSuperviewIfNeeded(adView: BannerView, adFormat: AdFormat) {
        if (adView.parent != null) return
        if (bannerCount > 0 && !isAfterDelete) {
            val count = bannerCount - 1
            items[count].color.value = Color.Green
            items[count].showBorder.value = false
        }
        val targetRow = items[bannerCount++]
        targetRow.color.value = Color.Green
        targetRow.showBorder.value = true

        adView.setBackgroundColor(android.graphics.Color.MAGENTA)
        val density = this.resources.displayMetrics.density
        if (adFormat.isbanner_320x50) {
            targetRow.height.value = 80.dp
            targetRow.width.value = 320.dp
            targetRow.view.value.addView(adView, (320 * density).toInt(), (50 * density).toInt())
        } else {
            targetRow.height.value = 280.dp
            targetRow.width.value = 300.dp
            targetRow.view.value.addView(adView, (300 * density).toInt(), (250 * density).toInt())
        }

        pendingBanners.remove(adView)
        dataSource.add(0, targetRow)
        isAfterDelete = false
        if (bannerCount == 30) reloadTable()
        if (dataSource.size == 5) {
            generateBannerTimer?.cancel()
            generateBannerTimer = null
        }


    }

    @Composable
    fun Main() {
        DisposableEffect(Unit) {
            onDispose {
               generateBannerTimer?.cancel()
               removeBannerTimer?.cancel()
            }
        }
        Column(
            modifier = Modifier
                .padding(32.dp, 0.dp, 32.dp, 0.dp)
                .fillMaxWidth()
                .height(30.dp)
                .background(
                    color = Color.LightGray,
                    shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
                )
        )
        {
        }
        Column(
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(16.dp, 26.dp, 16.dp, 0.dp)
                .fillMaxWidth()
                .height(40.dp)
                .background(
                    color = Color.LightGray,
                    shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
                )
        )
        {
            Text(
                text = "LOADING...",
                color = Color.Black,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(32.dp, 0.dp, 0.dp, 0.dp)
            )
        }
    }

    @Composable
    fun Table() {
        val context = LocalContext.current
        items = remember {
            mutableStateListOf<CellModel>().apply {
                addAll(List(30) {
                    CellModel(
                        mutableStateOf(50.dp),
                        mutableStateOf(320.dp),
                        mutableStateOf(Color.White),
                        mutableStateOf(FrameLayout(context)),
                        mutableStateOf(false)
                    )
                })
            }
        }


        LazyColumn(
            modifier = Modifier.padding(0.dp, 65.dp, 0.dp, 0.dp),
        ) {
            itemsIndexed(items) { _, item ->
                TableCell(item)
            }
        }
    }

    @Composable
    fun TableCell(cellModel: CellModel) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .width(cellModel.width.value)
                .height(cellModel.height.value)
                .background(cellModel.color.value)
                .border(
                    border = BorderStroke(
                        width = 1.dp,
                        color = Color.Black
                    ),
                    shape = RectangleShape
                ),

            ) {
            AndroidView(
                modifier = Modifier.padding(0.dp, 8.dp),
                factory = {
                    cellModel.view.value
                },
                update ={
                    if (it.getChildAt(0) != null) {
                        if (((it.getChildAt(0) as? BannerView)?.getChildAt(0)).toString().contains("isDestroyed=true")) {
                            (it.getChildAt(0) as? BannerView)?.destroy()
                            (it.getChildAt(0) as? BannerView)?.refreshAd()
                        }
                    }
                },
            )
            if (cellModel.showBorder.value) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .background(Color.Red)
                ) {
                }
            }

        }
    }

    override fun adViewReadyToRefresh(adView: BannerView, adInfo: AdInfo?) {
        val addBanner = runCatching {
            print("App - adViewReadyToRefresh. AdView: $adView, AdInfo: $adInfo")
            addAdToSuperviewIfNeeded(adView, adInfo?.adFormat as AdFormat)
            adView.refreshAd()
        }
        if (addBanner.isFailure) {
            print("Failure add banner")
            adView.destroy()
        }

    }

    override fun adViewDidDisplayAd(adView: BannerView, adInfo: AdInfo?) {
        print("App - didDisplayAd. AdView: $adView, AdInfo: $adInfo")
    }

    override fun adViewDidFailToDisplayAd(adView: BannerView, adInfo: AdInfo?, errors: Error) {
        print("App - didFailToDisplayAd. AdView: $adView, Error:${errors.localizedMessage}")
    }

    override fun adViewClicked(adView: BannerView, adInfo: AdInfo?) {
        print("App - didClicked. AdView: $adView, AdInfo: $adInfo")
    }
}

data class CellModel(
    var height: MutableState<Dp>,
    var width: MutableState<Dp>,
    var color: MutableState<Color>,
    var view: MutableState<FrameLayout>,
    var showBorder: MutableState<Boolean>,
)


