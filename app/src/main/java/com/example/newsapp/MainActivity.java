package com.example.newsapp;

import android.annotation.SuppressLint;
import android.os.Build;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import android.content.Intent;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private HorizontalScrollView horizontalScrollView;
    private LinearLayout linearLayout, newsContentLayout;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;

    @SuppressLint({"MissingInflatedId", "ClickableViewAccessibility"})
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        drawerLayout = findViewById(R.id.drawer_layout);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Set up the drawer toggle for the END (right) drawer
        drawerToggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.open_nav, R.string.close_nav);
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();

        // Show hamburger icon
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        // Hamburger button click opens/closes the END drawer
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

        // ... (all your existing initialization code for buttons, scroll, etc.)
        ImageButton sport_button = findViewById(R.id.sport_button);
        ImageButton education_button = findViewById(R.id.education_button);
        ImageButton global_button = findViewById(R.id.global_button);

        setScaleOnTouch(sport_button);
        setScaleOnTouch(education_button);
        setScaleOnTouch(global_button);

        if (sport_button != null) {
            sport_button.setOnClickListener(v -> fetchNewsFromCategory("Sports"));
        }
        if (education_button != null) {
            education_button.setOnClickListener(v -> fetchNewsFromCategory("Academic"));
        }
        if (global_button != null) {
            global_button.setOnClickListener(v -> fetchNewsFromCategory("Events"));
        }

        ImageButton imageNewYear = findViewById(R.id.imageNewYear);
        ImageButton imageSiyo = findViewById(R.id.imageSiyo);
        ImageButton imageTechbash = findViewById(R.id.imageTechbash);
        ImageButton imageGameday = findViewById(R.id.imageGameday);
        ImageButton imageNenayathra = findViewById(R.id.imageNenayathra);
        ImageButton imageWorkshop = findViewById(R.id.imageWorkshop);

        if (imageNewYear != null) {
            imageNewYear.setOnClickListener(v -> fetchNewsFromSubCategory("Events", "newyr"));
        }
        if (imageSiyo != null) {
            imageSiyo.setOnClickListener(v -> fetchNewsFromSubCategory("Events", "siyo"));
        }
        if (imageTechbash != null) {
            imageTechbash.setOnClickListener(v -> fetchNewsFromSubCategory("Sports", "techbash"));
        }
        if (imageGameday != null) {
            imageGameday.setOnClickListener(v -> fetchNewsFromSubCategory("Sports", "gameday"));
        }
        if (imageNenayathra != null) {
            imageNenayathra.setOnClickListener(v -> fetchNewsFromSubCategory("Academic", "nenayathra"));
        }
        if (imageWorkshop != null) {
            imageWorkshop.setOnClickListener(v -> fetchNewsFromSubCategory("Academic", "workshop"));
        }

        fetchNewsFromFirebase();

        ImageButton backButton = findViewById(R.id.back_button);
        setScaleOnTouch(backButton);

        if (backButton != null) {
            backButton.setOnClickListener(view -> fetchNewsFromFirebase());
        }

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

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (drawerToggle != null && drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        drawerLayout.closeDrawer(GravityCompat.START);

        int id = item.getItemId();

        // Uncomment and update these lines after merging the branches and having the activities!
        // if (id == R.id.nav_devinfo) {
        //     Intent intent = new Intent(MainActivity.this, DevInfoActivity.class);
        //     startActivity(intent);
        //     return true;
        // }

        // if (id == R.id.nav_userinfo) {
        //     Intent intent = new Intent(MainActivity.this, UserInfoActivity.class);
        //     startActivity(intent);
        //     return true;
        // }

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
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    v.animate().scaleX(0.85f).scaleY(0.85f).setDuration(120).start();
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(120).start();
                    break;
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
                if (snapshot.exists()) {
                    newsContentLayout.removeAllViews();
                    for (DataSnapshot categorySnap : snapshot.getChildren()) {
                        for (DataSnapshot newsSnap : categorySnap.getChildren()) {
                            NewsItem item = newsSnap.getValue(NewsItem.class);

                            if (item != null) {
                                View cardView = LayoutInflater.from(MainActivity.this)
                                        .inflate(R.layout.news_card, newsContentLayout, false);

                                TextView titleText = cardView.findViewById(R.id.news_title);
                                TextView dateText = cardView.findViewById(R.id.news_date);
                                TextView descriptionText = cardView.findViewById(R.id.news_description);
                                ImageView imageView = cardView.findViewById(R.id.news_image);

                                titleText.setText(item.title);
                                dateText.setText(item.date);
                                descriptionText.setText(item.content);
                                Picasso.get().load(item.imageurl).into(imageView);

                                cardView.setOnClickListener(v -> Toast.makeText(MainActivity.this, item.title, Toast.LENGTH_SHORT).show());

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

    private void fetchNewsFromCategory(String category) {
        newsContentLayout = findViewById(R.id.news_container);
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("News").child(category);

        dbRef.limitToFirst(6).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                newsContentLayout.removeAllViews();
                for (DataSnapshot newsSnap : snapshot.getChildren()) {
                    NewsItem item = newsSnap.getValue(NewsItem.class);
                    if (item != null) {
                        View cardView = LayoutInflater.from(MainActivity.this)
                                .inflate(R.layout.news_card, newsContentLayout, false);

                        TextView titleText = cardView.findViewById(R.id.news_title);
                        TextView dateText = cardView.findViewById(R.id.news_date);
                        TextView descriptionText = cardView.findViewById(R.id.news_description);
                        ImageView imageView = cardView.findViewById(R.id.news_image);

                        titleText.setText(item.title);
                        dateText.setText(item.date);
                        descriptionText.setText(item.content);
                        Picasso.get().load(item.imageurl).into(imageView);

                        cardView.setOnClickListener(v -> Toast.makeText(MainActivity.this, item.title, Toast.LENGTH_SHORT).show());

                        newsContentLayout.addView(cardView);
                    }
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
        DatabaseReference dbRef = FirebaseDatabase.getInstance()
                .getReference("News").child(parentCategory).child(subCategory);

        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                newsContentLayout.removeAllViews();
                NewsItem item = snapshot.getValue(NewsItem.class);
                if (item != null) {
                    View cardView = LayoutInflater.from(MainActivity.this)
                            .inflate(R.layout.news_card, newsContentLayout, false);

                    TextView titleText = cardView.findViewById(R.id.news_title);
                    TextView dateText = cardView.findViewById(R.id.news_date);
                    TextView descriptionText = cardView.findViewById(R.id.news_description);
                    ImageView imageView = cardView.findViewById(R.id.news_image);

                    titleText.setText(item.title);
                    dateText.setText(item.date);
                    descriptionText.setText(item.content);
                    Picasso.get().load(item.imageurl).into(imageView);

                    cardView.setOnClickListener(v -> Toast.makeText(MainActivity.this, item.title, Toast.LENGTH_SHORT).show());

                    newsContentLayout.addView(cardView);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Error loading news", error.toException());
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
