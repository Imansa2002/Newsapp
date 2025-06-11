package com.example.newsapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class dev_info extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dev_info);

        // Apply insets for proper edge-to-edge layout
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.dev_info), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ImageButton backButton = findViewById(R.id.back_button);
        setScaleOnTouch(backButton);

        if (backButton != null) {
            backButton.setOnClickListener(view -> {
                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            });
        }
        
        // Set up the EXIT button to go to MainActivity
        Button exitButton = findViewById(R.id.exitButton);
        exitButton.setOnClickListener(v -> {
            Intent intent = new Intent(dev_info.this, MainActivity.class);
            startActivity(intent);
            finish(); // Optional: closes the current activity
        });
    }

    private void setScaleOnTouch(ImageButton backButton) {
    }
}
