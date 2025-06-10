package com.example.newsapp;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private HorizontalScrollView horizontalScrollView;
    private LinearLayout linearLayout;
    private ImageButton backButton;

    @SuppressLint({"MissingInflatedId", "ClickableViewAccessibility"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        backButton = findViewById(R.id.back_button);
        if (backButton != null) {
            backButton.setOnTouchListener((v, event) -> {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        backButton.setColorFilter(Color.parseColor("#77000000"), PorterDuff.Mode.SRC_ATOP);
                        break;
                    case MotionEvent.ACTION_UP:
                        backButton.clearColorFilter();
                        // Handle click here:
                        // For example: show a toast, or any action you want
                        // Just don’t call onBackPressed()
                        break;
                    case MotionEvent.ACTION_CANCEL:
                        backButton.clearColorFilter();
                        break;
                }
                return false; // Let click event still fire
            });

            backButton.setOnClickListener(view -> {
                // Put your custom click action here.
                // This is triggered after ACTION_UP
                // For example, do nothing or show a message
            });
        }

        // Other code remains the same...
        horizontalScrollView = findViewById(R.id.horizontalScrollView2);
        linearLayout = findViewById(R.id.linearLayoutInsideScroll);

        if (horizontalScrollView != null && linearLayout != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                horizontalScrollView.setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
                    int scrollViewCenterX = scrollX + horizontalScrollView.getWidth() / 2;

                    for (int i = 0; i < linearLayout.getChildCount(); i++) {
                        View child = linearLayout.getChildAt(i);
                        int childCenterX = child.getLeft() + child.getWidth() / 2;
                        int distance = Math.abs(scrollViewCenterX - childCenterX);

                        float maxDistance = horizontalScrollView.getWidth() / 2f;
                        float scale = 1f - (distance / maxDistance) * 0.3f;
                        if (scale < 0.7f) scale = 0.7f;

                        child.setScaleX(scale);
                        child.setScaleY(scale);
                        child.setAlpha(scale);
                    }
                });
            }

            horizontalScrollView.post(() -> {
                int scrollX = horizontalScrollView.getScrollX();
                horizontalScrollView.setScrollX(scrollX);
            });
        }

        setupCardTouchEffect(R.id.imageNewYear, R.id.overlayNewYear);
        setupCardTouchEffect(R.id.imageSiyo, R.id.overlaySiyo);
        setupCardTouchEffect(R.id.imageTechbash, R.id.overlayTechbash);
        setupCardTouchEffect(R.id.imageGameday, R.id.overlayGameday);
        setupCardTouchEffect(R.id.imageNenayathra, R.id.overlayNenayathra);
        setupCardTouchEffect(R.id.imageWorkshop, R.id.overlayWorkshop);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupCardTouchEffect(int imageButtonId, int overlayViewId) {
        ImageButton imageButton = findViewById(imageButtonId);
        View overlay = findViewById(overlayViewId);

        if (imageButton != null && overlay != null) {
            imageButton.setOnTouchListener((v, event) -> {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        overlay.setVisibility(View.VISIBLE);
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        overlay.setVisibility(View.GONE);
                        break;
                }
                return false;
            });
        }
    }
}