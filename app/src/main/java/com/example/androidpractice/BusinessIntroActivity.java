package com.example.androidpractice;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import androidx.appcompat.app.AppCompatActivity;

public class BusinessIntroActivity extends AppCompatActivity {

    private static final long FADE_DURATION_MS = 500;

    private View businessWelcomeRoot;
    private ImageSwitcher backgroundSwitcher;
    private View showcaseCard;

    private TextView tvHeaderCategory;
    private TextView tvStepBadge;
    private TextView tvFeatureCategory;
    private TextView tvSlideTitle;
    private TextView tvSlideDescription;
    private TextView tapAnywhereHint;

    private View[] dots;

    private int currentSlideIndex = 0;
    private boolean isNavigating = false;
    private ObjectAnimator pulseAnimator;

    private static class SlideData {
        int bgDrawable;
        String header;
        String badge;
        String category;
        String title;
        String description;

        SlideData(int bgDrawable, String header, String badge, String category, String title, String description) {
            this.bgDrawable = bgDrawable;
            this.header = header;
            this.badge = badge;
            this.category = category;
            this.title = title;
            this.description = description;
        }
    }

    private final SlideData[] slides = new SlideData[]{
            new SlideData(
                    R.drawable.create_profile_background,
                    "BUSINESS PARTNER • 01 / 04",
                    "FEATURE 01",
                    "SHOWCASE MENU & DAILY SPECIALS",
                    "Reach Hungry Travelers Nearby",
                    "Highlight your signature dishes, daily lunch specials, and complete menu with transparent pricing directly to travelers exploring your route."
            ),
            new SlideData(
                    R.drawable.hidden_spot_locator,
                    "BUSINESS PARTNER • 02 / 04",
                    "FEATURE 02",
                    "TRACK VISITOR INSIGHTS & ENQUIRIES",
                    "Measure Real Customer Interest",
                    "Monitor how many travelers view your profile, request map directions to your restaurant, and send direct dining enquiries."
            ),
            new SlideData(
                    R.drawable.hidden2,
                    "BUSINESS PARTNER • 03 / 04",
                    "FEATURE 03",
                    "STAND OUT ON THE MAP",
                    "Get Featured in Local Recommendations",
                    "Be discovered by tourists and road-trippers looking for authentic local food stops that don't always appear on mainstream maps."
            ),
            new SlideData(
                    R.drawable.road,
                    "BUSINESS PARTNER • 04 / 04",
                    "FEATURE 04",
                    "GROW YOUR RESTAURANT REVENUE",
                    "Connect Directly With Customers",
                    "Welcome new guests every day, showcase your unique ambiance, and turn passing travelers into loyal customers."
            )
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_business_intro);

        businessWelcomeRoot = findViewById(R.id.businessWelcomeRoot);
        backgroundSwitcher = findViewById(R.id.businessBackgroundSwitcher);
        showcaseCard = findViewById(R.id.showcaseCard);

        tvHeaderCategory = findViewById(R.id.tvHeaderCategory);
        tvStepBadge = findViewById(R.id.tvStepBadge);
        tvFeatureCategory = findViewById(R.id.tvFeatureCategory);
        tvSlideTitle = findViewById(R.id.tvSlideTitle);
        tvSlideDescription = findViewById(R.id.tvSlideDescription);
        tapAnywhereHint = findViewById(R.id.tapAnywhereHint);

        dots = new View[]{
                findViewById(R.id.dot0),
                findViewById(R.id.dot1),
                findViewById(R.id.dot2),
                findViewById(R.id.dot3)
        };

        setupBackgroundSwitcher();
        applySlide(0, false);
        startHintPulse();
    }

    private void setupBackgroundSwitcher() {
        backgroundSwitcher.setFactory(() -> {
            ImageView imageView = new ImageView(BusinessIntroActivity.this);
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
    }

    private void applySlide(int index, boolean animate) {
        currentSlideIndex = index;
        SlideData data = slides[index];

        backgroundSwitcher.setImageResource(data.bgDrawable);
        startKenBurnsOnCurrentImage();
        updateDots();

        if (animate) {
            showcaseCard.animate()
                    .alpha(0f)
                    .translationY(-20f)
                    .setDuration(220)
                    .setInterpolator(new AccelerateInterpolator())
                    .withEndAction(() -> {
                        updateSlideTexts(data);
                        showcaseCard.setTranslationY(30f);
                        showcaseCard.animate()
                                .alpha(1f)
                                .translationY(0f)
                                .setDuration(350)
                                .setInterpolator(new DecelerateInterpolator())
                                .start();
                    })
                    .start();
        } else {
            updateSlideTexts(data);
        }

        if (index == slides.length - 1) {
            tapAnywhereHint.setText("Tap anywhere to get started ›");
        } else {
            tapAnywhereHint.setText("Tap anywhere to continue ›");
        }
    }

    private void updateSlideTexts(SlideData data) {
        tvHeaderCategory.setText(data.header);
        tvStepBadge.setText(data.badge);
        tvFeatureCategory.setText(data.category);
        tvSlideTitle.setText(data.title);
        tvSlideDescription.setText(data.description);
    }

    private void startKenBurnsOnCurrentImage() {
        View current = backgroundSwitcher.getCurrentView();
        if (current == null) return;

        current.setScaleX(1f);
        current.setScaleY(1f);
        current.animate()
                .scaleX(1.08f)
                .scaleY(1.08f)
                .setDuration(3000)
                .setInterpolator(new android.view.animation.LinearInterpolator())
                .start();
    }

    private void updateDots() {
        for (int i = 0; i < dots.length; i++) {
            dots[i].setBackgroundResource(
                    i == currentSlideIndex ? R.drawable.dot_active : R.drawable.dot_inactive);
        }
    }

    private void startHintPulse() {
        if (tapAnywhereHint == null) return;
        pulseAnimator = ObjectAnimator.ofFloat(tapAnywhereHint, View.ALPHA, 1f, 0.35f);
        pulseAnimator.setDuration(1000);
        pulseAnimator.setRepeatCount(ObjectAnimator.INFINITE);
        pulseAnimator.setRepeatMode(ObjectAnimator.REVERSE);
        pulseAnimator.start();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_UP) {
            handleTap();
            return true;
        }
        return super.dispatchTouchEvent(ev);
    }

    private void handleTap() {
        if (isNavigating) return;

        if (currentSlideIndex < slides.length - 1) {
            applySlide(currentSlideIndex + 1, true);
        } else {
            goToOptions();
        }
    }

    private void goToOptions() {
        if (isNavigating) return;
        isNavigating = true;

        if (pulseAnimator != null) {
            pulseAnimator.cancel();
        }

        businessWelcomeRoot.animate()
                .alpha(0f)
                .setDuration(300)
                .setInterpolator(new AccelerateInterpolator())
                .withEndAction(() -> {
                    Intent intent = new Intent(BusinessIntroActivity.this, BusinessOptionActivity.class);
                    startActivity(intent);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    finish();
                })
                .start();
    }
}