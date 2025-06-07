package com.example.newsapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.newsapp.SignupActivity;


import java.util.Objects;

public class SigninActivity extends AppCompatActivity {

    private EditText usernameField, passwordField;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        usernameField = findViewById(R.id.usernameField);
        passwordField = findViewById(R.id.passwordField);
        Button loginButton = findViewById(R.id.loginButton);
        TextView btnSignupTab = findViewById(R.id.btnSignupTab);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        loginButton.setOnClickListener(v -> {
            String username = usernameField.getText().toString().trim();
            String password = passwordField.getText().toString().trim();

            if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
                Toast.makeText(SigninActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Check if username exists
            db.collection("users")
                    .whereEqualTo("username", username)
                    .get()
                    .addOnSuccessListener(query -> {
                        if (!query.isEmpty()) {
                            String email = query.getDocuments().get(0).getString("email");

                            if (email != null) {
                                mAuth.signInWithEmailAndPassword(email, password)
                                        .addOnCompleteListener(task -> {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();
                                                // ➡️ Navigate to next screen
                                                startActivity(new Intent(SigninActivity.this, MainActivity.class));
                                                finish();
                                            } else {
                                                Toast.makeText(this, "Login failed: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_LONG).show();
                                            }
                                        });
                            } else {
                                Toast.makeText(this, "Email not found for username", Toast.LENGTH_SHORT).show();
                            }

                        } else {
                            Toast.makeText(this, "Username not found", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Failed to check username", Toast.LENGTH_SHORT).show());
        });

        // Go to Signup screen
        btnSignupTab.setOnClickListener(v ->
                startActivity(new Intent(SigninActivity.this, SignupActivity.class))
        );
    }
}
