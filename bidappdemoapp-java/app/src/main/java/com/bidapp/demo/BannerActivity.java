package com.bidapp.demo;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import io.bidapp.sdk.AdFormat;
import io.bidapp.sdk.AdInfo;
import io.bidapp.sdk.BIDBannerViewDelegate;
import io.bidapp.sdk.BannerView;


public class BannerActivity extends AppCompatActivity implements BIDBannerViewDelegate {
    private ProgressBar progressBar;
    private final List<BannerView> pendingBanners = new ArrayList<>();
    private final List<View> dataSource = new ArrayList<>();
    private TimerTask generateBannerTimer;
    private TimerTask removeBannerTimer;
    private TableLayout tableLayout;
    private int bannerCount = 0;
    private boolean isAfterDelete = false;
    private ArrayList<BannerView> allBannersArray = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_banner);

        progressBar = findViewById(R.id.progressBar);
        tableLayout = findViewById(R.id.tablelay);
        progressBar.setMax(5);

        addOneMoreBanner();
        scheduleRemoveOneBanner();
    }

    private void addOneMoreBanner() {
        if (pendingBanners.size() < 2) {
            AdFormat format = (new Random().nextInt(2) == 0) ? AdFormat.banner_320x50 : AdFormat.banner_300x250;
            BannerView banner = new BannerView(this).banner(format);
            banner.setBannerViewDelegate(this);
            pendingBanners.add(banner);
            allBannersArray.add(banner);
            banner.refreshAd();
            addAdToSuperviewIfNeeded(banner, format);
            scheduleAddOneMoreBanner();
        }
    }

    private void scheduleRemoveOneBanner() {
        if (removeBannerTimer != null) {
            removeBannerTimer.cancel();
        }
        removeBannerTimer = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        removeOneBanner();
                    }
                });
            }
        };
        new Timer().schedule(removeBannerTimer, 30000);
    }

    private void scheduleAddOneMoreBanner() {
        if (generateBannerTimer != null) {
            generateBannerTimer.cancel();
        }
        generateBannerTimer = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        addOneMoreBanner();
                    }
                });
            }
        };
        new Timer().schedule(generateBannerTimer, 10000);
    }

    private void reloadTable() {
        bannerCount = 0;
        for (View view : dataSource) {
            if (view instanceof ConstraintLayout) {
                ConstraintLayout constraintLayout = (ConstraintLayout) view;
                if (constraintLayout.getChildCount() > 0) {
                    constraintLayout.removeViewAt(0);
                }
            }
        }
        dataSource.clear();
        isAfterDelete = false;

        for (BannerView banner : allBannersArray) {
            banner.destroy();
        }
        for (int i = 0; i < tableLayout.getChildCount(); i++) {
            View child = tableLayout.getChildAt(i);
            if (child instanceof TableRow) {
                TableRow row = (TableRow) child;
                row.setBackgroundResource(R.drawable.cell_border);
            }
        }
    }

    private void removeOneBanner() {
        boolean removedTheLastOne = false;
        for (int i = dataSource.size() - 1; i >= 0; i--)
         {
             View view = dataSource.get(i);
             if (view instanceof ConstraintLayout) {
                ConstraintLayout constraintLayout = (ConstraintLayout) view;
                if (constraintLayout.getChildCount() > 0) {
                    constraintLayout.removeViewAt(0);
                    ((TableRow) constraintLayout.getParent()).setBackgroundResource(R.drawable.background3);
                    removedTheLastOne = (view == dataSource.get(0));
                    break;
                }
            }
        }

        if (removedTheLastOne) {
            isAfterDelete = true;
            dataSource.clear();
            addOneMoreBanner();
        }
        scheduleRemoveOneBanner();
    }

    private void addAdToSuperviewIfNeeded(BannerView adView, AdFormat adFormat) {
        if (adView.getParent() != null) {
            return;
        }
        if (bannerCount > 0 && !isAfterDelete) {
            int count = bannerCount - 1;
            TableRow previousTargetRow = (TableRow) tableLayout.getChildAt(count);
            previousTargetRow.setBackgroundResource(R.drawable.background2);
        }
        TableRow targetRow = (TableRow) tableLayout.getChildAt(bannerCount++);
        ConstraintLayout constraintLayout = (ConstraintLayout) targetRow.getChildAt(0);
        targetRow.setBackgroundResource(R.drawable.background1);

        adView.setBackgroundColor(Color.MAGENTA);
        float density = getResources().getDisplayMetrics().density;
        int width, height;
        if (adFormat.isBanner_320x50) {
            width = (int) (320 * density);
            height = (int) (50 * density);
        } else {
            width = (int) (300 * density);
            height = (int) (250 * density);
        }
        constraintLayout.addView(adView, width, height);
        pendingBanners.remove(adView);
        dataSource.add(0, constraintLayout);
        isAfterDelete = false;
        progressBar.setProgress(bannerCount);
        if (tableLayout.getChildCount() == bannerCount) {
            reloadTable();
        }
        if (dataSource.size() == 5) {
            if (generateBannerTimer != null) {
                generateBannerTimer.cancel();
                generateBannerTimer = null;
            }
        }
    }




    @Override
    public void adViewDidDisplayAd(@NonNull BannerView adView, AdInfo adInfo) {
        System.out.println("App - didDisplayAd. AdView: " + adView + ", AdInfo: " + adInfo);
    }

    @Override
    public void adViewDidFailToDisplayAd(@NonNull BannerView adView, AdInfo adInfo, Error errors) {
        System.out.println("App - didFailToDisplayAd. AdView: " + adView + ", Error: " + errors.getLocalizedMessage());
    }

    @Override
    public void adViewClicked(@NonNull BannerView adView, AdInfo adInfo) {
        System.out.println("App - didClicked. AdView: " + adView + ", AdInfo: " + adInfo);
    }

    @Override
    public void allNetworksFailedToDisplayAd(@NonNull BannerView adView) {
        System.out.println("App - didClicked. AdView: " + adView);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        for (BannerView banner : allBannersArray) {
            banner.destroy();
        }
        if (generateBannerTimer != null) {
            generateBannerTimer.cancel();
        }
        if (removeBannerTimer != null) {
            removeBannerTimer.cancel();
        }
    }
}