package io.bidapp.demo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;



public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        TextView interstitialButton = findViewById(R.id.inter);
        TextView rewardedButton = findViewById(R.id.reward);
        TextView bannersButton = findViewById(R.id.banners);
        ConstraintLayout bannerMain = findViewById(R.id.bannerMain);

        FullscreenLoadDelegate loadDelegate = new FullscreenLoadDelegate(this);
        Interstitial.INSTANCE.loadDelegate = loadDelegate;
        Rewarded.INSTANCE.loadDelegate = loadDelegate;

        FullscreenShowDelegate showDelegate = new FullscreenShowDelegate(this);
        BannerViewDelegate bannerShowDelegate = new BannerViewDelegate(this);
        BannerView banner = new BannerView(this).banner(bannerShowDelegate, AdFormat.banner_320x50);

        BIDConfiguration bidConfig = new BIDConfiguration();


        bidConfig.enableTestMode();

        bidConfig.enableTestMode();
        bidConfig.enableLogging();
        bidConfig.enableInterstitialAds();
        bidConfig.enableRewardedAds();
        bidConfig.bannerEnabled_300x250();
        bidConfig.bannerEnabled_320x50();


        String pubid = "15ddd248-7acc-46ce-a6fd-e6f6543d22cd";

        BidappAds.start(this, pubid, bidConfig);

        interstitialButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Interstitial.INSTANCE.show(showDelegate);
            }
        });

        rewardedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Rewarded.INSTANCE.show(showDelegate);
            }
        });

        bannersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), BannerActivity.class);
                startActivity(intent);
            }
        });
        bannerMain.addView(banner);
    }
}