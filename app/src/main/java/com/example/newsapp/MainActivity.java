package com.example.newsapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
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

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.*;
import com.squareup.picasso.Picasso;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private HorizontalScrollView horizontalScrollView;
    private LinearLayout linearLayout, newsContentLayout;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        drawerLayout = findViewById(R.id.drawer_layout);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_nav, R.string.close_nav);
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();

        ImageButton hamburger = findViewById(R.id.hamburger);
        if (hamburger != null) {
            hamburger.setOnClickListener(v -> {
                if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
                    drawerLayout.closeDrawer(GravityCompat.END);
                } else {
                    drawerLayout.openDrawer(GravityCompat.END);
                }
            });
        }

        ImageButton sport_button = findViewById(R.id.sport_button);
        ImageButton education_button = findViewById(R.id.education_button);
        ImageButton global_button = findViewById(R.id.global_button);

        setScaleOnTouch(sport_button);
        setScaleOnTouch(education_button);
        setScaleOnTouch(global_button);

        sport_button.setOnClickListener(v -> fetchNewsFromCategory("Sports"));
        education_button.setOnClickListener(v -> fetchNewsFromCategory("Academic"));
        global_button.setOnClickListener(v -> fetchNewsFromCategory("Events"));

        setupCardTouchEffect(R.id.imageNewYear, R.id.overlayNewYear);
        setupCardTouchEffect(R.id.imageSiyo, R.id.overlaySiyo);
        setupCardTouchEffect(R.id.imageTechbash, R.id.overlayTechbash);
        setupCardTouchEffect(R.id.imageGameday, R.id.overlayGameday);
        setupCardTouchEffect(R.id.imageNenayathra, R.id.overlayNenayathra);
        setupCardTouchEffect(R.id.imageWorkshop, R.id.overlayWorkshop);

        findViewById(R.id.imageNewYear).setOnClickListener(v -> fetchNewsFromSubCategory("Events", "newyr"));
        findViewById(R.id.imageSiyo).setOnClickListener(v -> fetchNewsFromSubCategory("Events", "siyo"));
        findViewById(R.id.imageTechbash).setOnClickListener(v -> fetchNewsFromSubCategory("Sports", "techbash"));
        findViewById(R.id.imageGameday).setOnClickListener(v -> fetchNewsFromSubCategory("Sports", "gameday"));
        findViewById(R.id.imageNenayathra).setOnClickListener(v -> fetchNewsFromSubCategory("Academic", "nenayathra"));
        findViewById(R.id.imageWorkshop).setOnClickListener(v -> fetchNewsFromSubCategory("Academic", "workshop"));

        ImageButton backButton = findViewById(R.id.back_button);
        setScaleOnTouch(backButton);
        backButton.setOnClickListener(view -> fetchNewsFromFirebase());

        horizontalScrollView = findViewById(R.id.horizontalScrollView2);
        linearLayout = findViewById(R.id.linearLayoutInsideScroll);

        horizontalScrollView.setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            int centerX = scrollX + horizontalScrollView.getWidth() / 2;
            for (int i = 0; i < linearLayout.getChildCount(); i++) {
                View child = linearLayout.getChildAt(i);
                int childCenterX = child.getLeft() + child.getWidth() / 2;
                float distance = Math.abs(centerX - childCenterX);
                float scale = 1f - (distance / (horizontalScrollView.getWidth() / 2f)) * 0.3f;
                scale = Math.max(scale, 0.7f);
                child.setScaleX(scale);
                child.setScaleY(scale);
                child.setAlpha(scale);
            }
        });

        fetchNewsFromFirebase();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        drawerLayout.closeDrawer(GravityCompat.END);
        if (item.getItemId() == R.id.nav_devinfo) {
            startActivity(new Intent(MainActivity.this, DevInfo.class));
        }
        return true;
    }


    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
            drawerLayout.closeDrawer(GravityCompat.END);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setScaleOnTouch(View view) {
        if (view == null) return;
        view.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                v.animate().scaleX(0.85f).scaleY(0.85f).setDuration(120).start();
            } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                v.animate().scaleX(1f).scaleY(1f).setDuration(120).start();
            }
            return false;
        });
    }

    private void fetchNewsFromFirebase() {
        newsContentLayout = findViewById(R.id.news_container);
        DatabaseReference newsRef = FirebaseDatabase.getInstance().getReference("News");

        newsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                newsContentLayout.removeAllViews();
                for (DataSnapshot categorySnap : snapshot.getChildren()) {
                    for (DataSnapshot newsSnap : categorySnap.getChildren()) {
                        NewsItem item = newsSnap.getValue(NewsItem.class);
                        addNewsCard(item);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseError", "Error fetching news: " + error.getMessage());
            }
        });
    }

    private void fetchNewsFromCategory(String category) {
        newsContentLayout = findViewById(R.id.news_container);
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("News").child(category);

        dbRef.limitToFirst(6).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                newsContentLayout.removeAllViews();
                for (DataSnapshot newsSnap : snapshot.getChildren()) {
                    NewsItem item = newsSnap.getValue(NewsItem.class);
                    addNewsCard(item);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Error loading " + category, error.toException());
            }
        });
    }

    private void fetchNewsFromSubCategory(String parentCategory, String subCategory) {
        newsContentLayout = findViewById(R.id.news_container);
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("News").child(parentCategory).child(subCategory);

        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                newsContentLayout.removeAllViews();
                NewsItem item = snapshot.getValue(NewsItem.class);
                addNewsCard(item);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Error loading subcategory", error.toException());
            }
        });
    }

    private void addNewsCard(NewsItem item) {
        if (item == null) return;

        View cardView = LayoutInflater.from(MainActivity.this).inflate(R.layout.news_card, newsContentLayout, false);
        ((TextView) cardView.findViewById(R.id.news_title)).setText(item.title);
        ((TextView) cardView.findViewById(R.id.news_date)).setText(item.date);
        ((TextView) cardView.findViewById(R.id.news_description)).setText(item.content);
        Picasso.get().load(item.imageurl).into((ImageView) cardView.findViewById(R.id.news_image));

        cardView.setOnClickListener(v -> Toast.makeText(MainActivity.this, item.title, Toast.LENGTH_SHORT).show());
        newsContentLayout.addView(cardView);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupCardTouchEffect(int imageButtonId, int overlayViewId) {
        ImageButton imageButton = findViewById(imageButtonId);
        View overlay = findViewById(overlayViewId);

        if (imageButton != null && overlay != null) {
            imageButton.setOnTouchListener((v, event) -> {
                overlay.setVisibility(event.getAction() == MotionEvent.ACTION_DOWN ? View.VISIBLE : View.GONE);
                return false;
            });
        }
    }
}
