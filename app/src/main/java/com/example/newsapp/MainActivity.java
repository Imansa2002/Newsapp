package com.example.newsapp;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.LayoutInflater;
import android.util.Log;

import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private HorizontalScrollView horizontalScrollView;
    private LinearLayout linearLayout, newsContentLayout;
    private ImageButton backButton;

    @SuppressLint({"MissingInflatedId", "ClickableViewAccessibility"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fetchNewsFromFirebase();

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

    //fetch news from the database
    private void fetchNewsFromFirebase() {
        newsContentLayout = findViewById(R.id.news_container);
        DatabaseReference newsRef = FirebaseDatabase.getInstance().getReference("News");

        newsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    newsContentLayout.removeAllViews(); // Clear existing cards

                    for (DataSnapshot categorySnap : snapshot.getChildren()) {
                        Log.d("FirebaseData", "Category: " + categorySnap.getKey());
                        for (DataSnapshot newsSnap : categorySnap.getChildren()) {
                            Log.d("FirebaseData", "News Item: " + newsSnap.getKey());
                            NewsItem item = newsSnap.getValue(NewsItem.class);

                            if (item != null) {
                                Log.d("FirebaseData", "Title: " + item.title);
                                // Inflate the card
                                View cardView = LayoutInflater.from(MainActivity.this)
                                        .inflate(R.layout.news_card, newsContentLayout, false);

                                // Populate it
                                TextView titleText = cardView.findViewById(R.id.news_title);
                                TextView dateText = cardView.findViewById(R.id.news_date);
                                TextView descriptionText = cardView.findViewById(R.id.news_description);
                                ImageView imageView = cardView.findViewById(R.id.news_image);

                                titleText.setText(item.title);
                                dateText.setText(item.date);
                                descriptionText.setText(item.content);
                                Picasso.get().load(item.imageurl).into(imageView);

                                // Optional: click listener
                                cardView.setOnClickListener(v -> {
                                    Toast.makeText(MainActivity.this, item.title, Toast.LENGTH_SHORT).show();
                                });

                                // Add to layout
                                newsContentLayout.addView(cardView);
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseError", "Error fetching news: " + error.getMessage());
            }
        });
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