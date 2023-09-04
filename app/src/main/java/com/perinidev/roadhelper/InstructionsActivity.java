package com.perinidev.roadhelper;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.text.LineBreaker;
import android.os.Bundle;
import android.os.Handler;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

public class InstructionsActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_MAIN_ACTIVITY = 1;
    private AdView mAdView;

    private InterstitialAd mInterstitialAd;

    private Handler handler;
    private String text;
    private TextView textView;
    private ScrollView scrollView;


    Button skipButton;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instructions);

        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE); // for landscape mode




        skipButton = (Button) findViewById(R.id.button_skip);

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        //AdRequest adRequest = new AdRequest.Builder().build();
        //adds
        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        InterstitialAd.load(this,"ca-app-pub-5679479887909470/1198269049", adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        // The mInterstitialAd reference will be null until
                        // an ad is loaded.
                        mInterstitialAd = interstitialAd;
                        Log.i(TAG, "onAdLoaded");

                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // Handle the error
                        Log.d(TAG, loadAdError.toString());
                        mInterstitialAd = null;
                    }
                });



        skipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent mainIntent = new Intent(InstructionsActivity.this, videoClass.class);
                if (isTaskRoot()) {
                    // Start the history activity if it's not already running
                    startActivityForResult(mainIntent, REQUEST_CODE_MAIN_ACTIVITY);
                }
                // when user click this button after the first time, just close the activity and return to history activity
                else {
                    finish();
                }
                if (mInterstitialAd != null) {
                    mInterstitialAd.show(InstructionsActivity.this);
                } else {
                    Log.d("TAG", "The interstitial ad wasn't ready yet.");
                }

            }
        });

        //scroll automatically textview and show text letter by letter

        textView = findViewById(R.id.textView4);
        textView.setText("");
        scrollView = findViewById(R.id.scrollView);
        text = (String) getText(R.string.instruction2);

        //scroll automatically textview and show text letter by letter
        handler = new Handler();
        handler.postDelayed(new Runnable() {
            int index = 0;
            final CharSequence charSequence = text;
            final int length = charSequence.length();
            final int delay =40;

            @Override
            public void run() {
                String builder = String.valueOf(textView.getText()) +
                        charSequence.charAt(index);
                textView.setText(builder);

                //scroll to the bottom of the textview
                scrollView.post(() -> scrollView.fullScroll(ScrollView.FOCUS_DOWN));

                //justify the text after it is displayed
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    textView.setJustificationMode(LineBreaker.JUSTIFICATION_MODE_INTER_WORD);
                }

                if (index < length-1){
                    index++;
                    handler.postDelayed(this, delay);
                }


            }
        },2000);

    }

}
