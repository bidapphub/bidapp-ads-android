package com.bidapp.demo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import io.bidapp.sdk.AdFormat;
import io.bidapp.sdk.BIDConfiguration;
import io.bidapp.sdk.BannerView;
import io.bidapp.sdk.BidappAds;
import io.bidapp.sdk.Interstitial;
import io.bidapp.sdk.Rewarded;

public class MainActivity extends AppCompatActivity {
    FullscreenLoadDelegate loadDelegate = new FullscreenLoadDelegate();
    FullscreenShowDelegate showDelegate = new FullscreenShowDelegate();
    BannerViewDelegate bannerShowDelegate = new BannerViewDelegate();

    BannerView banner = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView interstitialButton = findViewById(R.id.inter);
        TextView rewardedButton = findViewById(R.id.reward);
        TextView bannersButton = findViewById(R.id.banners);
        ConstraintLayout bannerMain = findViewById(R.id.bannerMain);

        Interstitial interstitial = new Interstitial(this);
        interstitial.setLoadDelegate(loadDelegate);
        Rewarded rewarded = new Rewarded(this);
        rewarded.setLoadDelegate(loadDelegate);



        BIDConfiguration bidConfig = new BIDConfiguration();


        bidConfig.enableTestMode();
        bidConfig.enableTestMode();
        bidConfig.enableLogging();

        String pubid = "15ddd248-7acc-46ce-a6fd-e6f6543d22cd";

        BidappAds.start(pubid, bidConfig, this);

        banner = new BannerView(this).banner(AdFormat.banner_320x50);
        banner.setBannerViewDelegate(bannerShowDelegate);
        bannerMain.addView(banner);
        banner.startAutoRefresh(30.0);

        interstitialButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                interstitial.showWithDelegate(MainActivity.this, showDelegate);
            }
        });

        rewardedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rewarded.showWithDelegate(MainActivity.this, showDelegate);
            }
        });

        bannersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), BannerActivity.class);
                startActivity(intent);
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (banner != null) {
            banner.destroy();
        }
    }
}