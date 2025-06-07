package com.example.newsapp;

import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class SignupActivity extends AppCompatActivity {

    EditText usernameField, passwordField, confirmPasswordField, emailField;
    Button btnSignupTab, signupButton;
    TextView btnLoginTab;

    // ✅ Firebase Auth instance
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signup);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            disableAutofill(findViewById(R.id.main));
        }

        usernameField = findViewById(R.id.usernameField);
        passwordField = findViewById(R.id.passwordField);
        confirmPasswordField = findViewById(R.id.confirmPasswordField);
        emailField = findViewById(R.id.emailField);
        btnSignupTab = findViewById(R.id.btnSignupTab);
        signupButton = findViewById(R.id.signupButton);
        btnLoginTab = findViewById(R.id.btnLoginTab);

        //FirebaseAuth fAuth = FirebaseAuth.getInstance();

        //if(fAuth.getCurrentUser()!=null) {
         //   startActivity(new Intent(getApplicationContext(), MainActivity.class));
           // finish();
        //}

        // ✅ Initialize FirebaseAuth
        mAuth = FirebaseAuth.getInstance();

        // 🔘 Signup button click event
        signupButton.setOnClickListener(v -> {
            String email = emailField.getText().toString().trim();
            String password = passwordField.getText().toString().trim();
            String confirmPassword = confirmPasswordField.getText().toString().trim();

            // 🔒 Basic validation
            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword)) {
                Toast.makeText(SignupActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirmPassword)) {
                Toast.makeText(SignupActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            // ✅ Create user with Firebase Auth
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(SignupActivity.this, "Signup successful!", Toast.LENGTH_SHORT).show();
                            // TODO: Navigate to main screen or login
                        } else {
                            Toast.makeText(SignupActivity.this, "Signup failed: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        });
    }

    private void disableAutofill(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            view.setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_NO_EXCLUDE_DESCENDANTS);
        }
    }
}
