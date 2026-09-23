package com.example.androidpractice;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private static final long SLIDE_INTERVAL_MS = 1800;
    private static final long FADE_DURATION_MS = 500;

    private View welcomeRoot;
    private ImageSwitcher backgroundSwitcher;
    private View[] dots;
    private View[] staggeredTexts;

    private final int[] backgroundImages = {
            R.drawable.waterfall3,
            R.drawable.forest,
            R.drawable.road,
            R.drawable.forest2
    };

    private int currentImageIndex = 0;
    private ObjectAnimator pulseAnimator;
    private boolean isNavigating = false;

    private final Handler slideshowHandler = new Handler(Looper.getMainLooper());
    private final Runnable slideshowRunnable = new Runnable() {
        @Override
        public void run() {
            showNextImage();
            slideshowHandler.postDelayed(this, SLIDE_INTERVAL_MS);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        welcomeRoot = findViewById(R.id.welcomeRoot);
        backgroundSwitcher = findViewById(R.id.backgroundSwitcher);

        dots = new View[]{
                findViewById(R.id.dot0),
                findViewById(R.id.dot1),
                findViewById(R.id.dot2),
                findViewById(R.id.dot3)
        };

        staggeredTexts = new View[]{
                findViewById(R.id.headingText),
                findViewById(R.id.subHeadingText),
                findViewById(R.id.taglineText),
                findViewById(R.id.descriptionText),
                findViewById(R.id.dotsContainer),
                findViewById(R.id.tapAnywhereHint)
        };

        setupBackgroundSwitcher();
        playStaggeredEntrance();

        welcomeRoot.setOnClickListener(v -> goToLogin());
    }

    // Sets up crossfade + Ken Burns zoom for the slideshow
    private void setupBackgroundSwitcher() {
        backgroundSwitcher.setFactory(() -> {
            ImageView imageView = new ImageView(MainActivity.this);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setLayoutParams(new ViewSwitcher.LayoutParams(
                    ViewSwitcher.LayoutParams.MATCH_PARENT,
                    ViewSwitcher.LayoutParams.MATCH_PARENT));
            return imageView;
        });

        AlphaAnimation fadeIn = new AlphaAnimation(0f, 1f);
        fadeIn.setDuration(FADE_DURATION_MS);
        AlphaAnimation fadeOut = new AlphaAnimation(1f, 0f);
        fadeOut.setDuration(FADE_DURATION_MS);
        backgroundSwitcher.setInAnimation(fadeIn);
        backgroundSwitcher.setOutAnimation(fadeOut);

        backgroundSwitcher.setImageResource(backgroundImages[currentImageIndex]);
        startKenBurnsOnCurrentImage();
        updateDots();

        slideshowHandler.postDelayed(slideshowRunnable, SLIDE_INTERVAL_MS);
    }

    private void showNextImage() {
        currentImageIndex = (currentImageIndex + 1) % backgroundImages.length;
        backgroundSwitcher.setImageResource(backgroundImages[currentImageIndex]);
        startKenBurnsOnCurrentImage();
        updateDots();
    }

    // Slow continuous zoom on whichever image is currently visible
    private void startKenBurnsOnCurrentImage() {
        View current = backgroundSwitcher.getCurrentView();
        if (current == null) return;

        current.setScaleX(1f);
        current.setScaleY(1f);
        current.animate()
                .scaleX(1.08f)
                .scaleY(1.08f)
                .setDuration(SLIDE_INTERVAL_MS + FADE_DURATION_MS)
                .setInterpolator(new android.view.animation.LinearInterpolator())
                .start();
    }

    private void updateDots() {
        for (int i = 0; i < dots.length; i++) {
            dots[i].setBackgroundResource(
                    i == currentImageIndex ? R.drawable.dot_active : R.drawable.dot_inactive);
        }
    }

    // Fades/slides each text element up one after another instead of all at once
    private void playStaggeredEntrance() {
        long baseDelay = 200;
        long stepDelay = 130;

        for (int i = 0; i < staggeredTexts.length; i++) {
            View view = staggeredTexts[i];
            view.setAlpha(0f);
            view.setTranslationY(40f);
            view.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(550)
                    .setStartDelay(baseDelay + (i * stepDelay))
                    .setInterpolator(new DecelerateInterpolator())
                    .withEndAction(i == staggeredTexts.length - 1 ? this::startHintPulse : null)
                    .start();
        }
    }

    private void startHintPulse() {
        TextView hint = findViewById(R.id.tapAnywhereHint);
        pulseAnimator = ObjectAnimator.ofFloat(hint, View.ALPHA, 1f, 0.35f);
        pulseAnimator.setDuration(1100);
        pulseAnimator.setRepeatCount(ObjectAnimator.INFINITE);
        pulseAnimator.setRepeatMode(ObjectAnimator.REVERSE);
        pulseAnimator.start();
    }

    private void goToLogin() {
        if (isNavigating) return;
        isNavigating = true;

        slideshowHandler.removeCallbacks(slideshowRunnable);
        if (pulseAnimator != null) {
            pulseAnimator.cancel();
        }

        welcomeRoot.animate()
                .alpha(0f)
                .setDuration(300)
                .setInterpolator(new AccelerateInterpolator())
                .withEndAction(() -> {
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(intent);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    finish();
                })
                .start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        slideshowHandler.removeCallbacks(slideshowRunnable);
    }
}