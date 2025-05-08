package com.example.metro_app.Activity;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.metro_app.R;

public class MainActivity extends AppCompatActivity {

    private static final int SPLASH_DURATION = 3000;
    private static final int METRO_FADE_DURATION = 500;
    private static final int METRO_MOVE_DURATION = 1000;
    private static final int LOGO_FADE_DELAY = 500;
    private static final int LOGO_FADE_DURATION = 500;
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageView metroImage = findViewById(R.id.metro_image);
        ImageView logoImage = findViewById(R.id.logo_image);

        metroImage.setAlpha(0f);
        logoImage.setAlpha(0f);

        metroImage.animate()
                .alpha(1f)
                .setDuration(METRO_FADE_DURATION)
                .withEndAction(() -> {
                    metroImage.animate()
                            .translationXBy(310f)
                            .setDuration(METRO_MOVE_DURATION)
                            .withEndAction(() -> {
                                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                                    logoImage.animate()
                                            .alpha(1f)
                                            .setDuration(LOGO_FADE_DURATION)
                                            .start();
                                }, LOGO_FADE_DELAY);
                            })
                            .start();
                })
                .start();

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            try {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                ActivityOptions options = ActivityOptions.makeCustomAnimation(
                        MainActivity.this,
                        android.R.anim.fade_in,
                        android.R.anim.fade_out
                );
                Log.d(TAG, "Starting LoginActivity");
                startActivity(intent, options.toBundle());
                finish();
            } catch (Exception e) {
                Log.e(TAG, "Failed to start LoginActivity: " + e.getMessage());
            }
        }, SPLASH_DURATION);
    }
}