package com.example.androidpractice;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class BusinessIntroActivity extends AppCompatActivity {

    private static final long FADE_DURATION_MS = 500;

    private ImageSwitcher backgroundSwitcher;
    private View slideContentContainer;
    private View showcaseCard;
    private View finalActionScroll;

    private TextView tvHeaderCategory;
    private TextView tvStepBadge;
    private TextView tvFeatureCategory;
    private TextView tvSlideTitle;
    private TextView tvSlideDescription;
    private TextView tapAnywhereHint;

    private Button btnBusinessContinue;
    private TextView tvBusinessSkip;

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
                    "BUSINESS PARTNER • 01 / 03",
                    "FEATURE 01",
                    "SHOWCASE MENU & DAILY SPECIALS",
                    "Reach Hungry Travelers Nearby",
                    "Highlight your signature dishes, daily lunch specials, and complete menu with transparent pricing directly to travelers exploring your route."
            ),
            new SlideData(
                    R.drawable.hidden_spot_locator,
                    "BUSINESS PARTNER • 02 / 03",
                    "FEATURE 02",
                    "TRACK VISITOR INSIGHTS & ENQUIRIES",
                    "Measure Real Customer Interest",
                    "Monitor how many travelers view your profile, request map directions to your restaurant, and send direct dining enquiries."
            ),
            new SlideData(
                    R.drawable.hidden2,
                    "BUSINESS PARTNER • 03 / 03",
                    "FEATURE 03",
                    "STAND OUT ON THE MAP",
                    "Get Featured in Local Recommendations",
                    "Be discovered by tourists and road-trippers looking for authentic local food stops that don't always appear on mainstream maps."
            )
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_business_intro);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        backgroundSwitcher = findViewById(R.id.businessBackgroundSwitcher);
        slideContentContainer = findViewById(R.id.slideContentContainer);
        showcaseCard = findViewById(R.id.showcaseCard);
        finalActionScroll = findViewById(R.id.finalActionScroll);

        tvHeaderCategory = findViewById(R.id.tvHeaderCategory);
        tvStepBadge = findViewById(R.id.tvStepBadge);
        tvFeatureCategory = findViewById(R.id.tvFeatureCategory);
        tvSlideTitle = findViewById(R.id.tvSlideTitle);
        tvSlideDescription = findViewById(R.id.tvSlideDescription);
        tapAnywhereHint = findViewById(R.id.tapAnywhereHint);

        btnBusinessContinue = findViewById(R.id.btnBusinessContinue);
        tvBusinessSkip = findViewById(R.id.tvBusinessSkip);

        dots = new View[]{
                findViewById(R.id.dot0),
                findViewById(R.id.dot1),
                findViewById(R.id.dot2)
        };

        setupBackgroundSwitcher();
        applySlide(0, false);
        startHintPulse();

        // Continue button on final slide -> Business Option Activity
        btnBusinessContinue.setOnClickListener(v -> {
            if (isNavigating) return;
            isNavigating = true;

            Intent intent = new Intent(BusinessIntroActivity.this, BusinessOptionActivity.class);
            startActivity(intent);
        });

        // Skip button on final slide -> Home Activity as Guest
        tvBusinessSkip.setOnClickListener(v -> {
            if (isNavigating) return;
            isNavigating = true;

            Intent intent = new Intent(BusinessIntroActivity.this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
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

        if (index < slides.length) {
            // Show feature slide 0, 1, or 2
            SlideData data = slides[index];

            backgroundSwitcher.setImageResource(data.bgDrawable);
            startKenBurnsOnCurrentImage();

            if (slideContentContainer.getVisibility() != View.VISIBLE) {
                slideContentContainer.setAlpha(0f);
                slideContentContainer.setVisibility(View.VISIBLE);
                slideContentContainer.animate().alpha(1f).setDuration(300).start();
            }

            if (finalActionScroll.getVisibility() == View.VISIBLE) {
                finalActionScroll.setVisibility(View.GONE);
            }

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

            tapAnywhereHint.setText("Tap anywhere to continue ›");

        } else {
            // Transition to Final Action Page (Slide 3)
            showFinalActionPage();
        }
    }

    private void showFinalActionPage() {
        currentSlideIndex = slides.length;
        backgroundSwitcher.setImageResource(R.drawable.forest2);
        startKenBurnsOnCurrentImage();

        if (pulseAnimator != null) {
            pulseAnimator.cancel();
        }

        slideContentContainer.animate()
                .alpha(0f)
                .setDuration(280)
                .setInterpolator(new AccelerateInterpolator())
                .withEndAction(() -> {
                    slideContentContainer.setVisibility(View.GONE);

                    finalActionScroll.setAlpha(0f);
                    finalActionScroll.setTranslationY(40f);
                    finalActionScroll.setVisibility(View.VISIBLE);
                    finalActionScroll.animate()
                            .alpha(1f)
                            .translationY(0f)
                            .setDuration(400)
                            .setInterpolator(new DecelerateInterpolator())
                            .start();
                })
                .start();
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
            // Only advance on screen tap if still in feature slides (slide 0, 1, 2)
            if (currentSlideIndex < slides.length) {
                handleTap();
                return true;
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    private void handleTap() {
        if (isNavigating) return;

        if (currentSlideIndex < slides.length - 1) {
            applySlide(currentSlideIndex + 1, true);
        } else {
            showFinalActionPage();
        }
    }
}