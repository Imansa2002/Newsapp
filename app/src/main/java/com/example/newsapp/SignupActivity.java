package com.example.newsapp;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SignupActivity extends AppCompatActivity {

    EditText usernameField, passwordField, confirmPasswordField, emailField;
    Button signupButton;
    FirebaseAuth mAuth;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        usernameField = findViewById(R.id.usernameField);
        passwordField = findViewById(R.id.passwordField);
        confirmPasswordField = findViewById(R.id.confirmPasswordField);
        emailField = findViewById(R.id.emailField);
        signupButton = findViewById(R.id.signupButton);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        signupButton.setOnClickListener(v -> {
            String username = usernameField.getText().toString().trim();
            String email = emailField.getText().toString().trim();
            String password = passwordField.getText().toString().trim();
            String confirmPassword = confirmPasswordField.getText().toString().trim();

            if (TextUtils.isEmpty(username) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword)) {
                Toast.makeText(SignupActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirmPassword)) {
                Toast.makeText(SignupActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            // Check if username already exists
            db.collection("users")
                    .whereEqualTo("username", username)
                    .get()
                    .addOnSuccessListener(query -> {
                        if (!query.isEmpty()) {
                            Toast.makeText(SignupActivity.this, "Username already taken", Toast.LENGTH_SHORT).show();
                        } else {
                            // Create Firebase user
                            mAuth.createUserWithEmailAndPassword(email, password)
                                    .addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            String userId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

                                            Map<String, Object> userMap = new HashMap<>();
                                            userMap.put("username", username);
                                            userMap.put("email", email);

                                            db.collection("users").document(userId)
                                                    .set(userMap)
                                                    .addOnSuccessListener(unused ->
                                                            Toast.makeText(SignupActivity.this, "Signup successful!", Toast.LENGTH_SHORT).show())
                                                    .addOnFailureListener(e ->
                                                            Toast.makeText(SignupActivity.this, "Failed to store user data", Toast.LENGTH_SHORT).show());
                                        } else {
                                            Toast.makeText(SignupActivity.this, "Signup failed: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_LONG).show();
                                        }
                                    });
                        }
                    })
                    .addOnFailureListener(e -> Toast.makeText(SignupActivity.this, "Error checking username", Toast.LENGTH_SHORT).show());
        });
    }
}
