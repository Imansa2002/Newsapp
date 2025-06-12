package com.example.newsapp;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.Manifest;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.content.SharedPreferences;
import android.widget.EditText;
import android.widget.Toast;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.widget.ImageView;
import androidx.annotation.NonNull;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.Objects;

public class UserInfo extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int PERMISSION_REQUEST_READ_EXTERNAL_STORAGE = 100;

    private View profileLayout;
    private TextView nameTextView, emailTextView;
    private ImageView profileIcon;

    private FirebaseFirestore db;
    private ListenerRegistration userListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_info);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.user_info), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        profileLayout = findViewById(R.id.profile_layout);
        Button btnEdit = findViewById(R.id.btnEdit);
        Button btnSignOut = findViewById(R.id.btnSignOut);

        btnEdit.setOnClickListener(v -> showEditPopup());
        btnSignOut.setOnClickListener(v -> showSignOutPopup());

        nameTextView = findViewById(R.id.userName);
        emailTextView = findViewById(R.id.email);
        profileIcon = findViewById(R.id.profileImage);

        profileIcon.setOnClickListener(v -> checkPermissionAndOpenImageChooser());

        db = FirebaseFirestore.getInstance();

        loadUserInfo();
    }

    private void checkPermissionAndOpenImageChooser() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_READ_EXTERNAL_STORAGE);
        } else {
            openImageChooser();
        }
    }

    private void openImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_READ_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openImageChooser();
            } else {
                Toast.makeText(this, "Permission denied to read external storage", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            profileIcon.setImageURI(imageUri);
        }
    }

    @SuppressLint("SetTextI18n")
    private void loadUserInfo() {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String uid = prefs.getString("uid", null);

        if (uid != null) {
            userListener = db.collection("users").document(uid)
                    .addSnapshotListener(this, (snapshot, error) -> {
                        if (error != null) {
                            Toast.makeText(this, "Error loading user info", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (snapshot != null && snapshot.exists()) {
                            String name = snapshot.getString("username");
                            String email = snapshot.getString("email");

                            nameTextView.setText("Username : " + name);
                            emailTextView.setText("Email : " + email);
                        } else {
                            Toast.makeText(this, "User info not found", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(this, "No user ID found in preferences", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (userListener != null) userListener.remove();
    }

    @SuppressLint("SetTextI18n")
    private void showEditPopup() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.popup_edit);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dimBackground(true);
        dialog.setOnDismissListener(d -> dimBackground(false));

        EditText editUsername = dialog.findViewById(R.id.edit_username);
        EditText editEmail = dialog.findViewById(R.id.edit_email);
        Button ok = dialog.findViewById(R.id.btn_ok);
        Button cancel = dialog.findViewById(R.id.btn_cancel);

        TextView warningText = dialog.findViewById(R.id.username_warning);
        warningText.setVisibility(View.GONE);

        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String uid = prefs.getString("uid", null);

        if (uid != null) {
            db.collection("users").document(uid).get().addOnSuccessListener(snapshot -> {
                if (snapshot.exists()) {
                    String existingUsername = snapshot.getString("username");
                    String existingEmail = snapshot.getString("email");

                    editUsername.setText(existingUsername);
                    editEmail.setText(existingEmail);
                    editUsername.setTextColor(Color.GRAY);
                    editEmail.setTextColor(Color.GRAY);
                }
            }).addOnFailureListener(e -> Toast.makeText(UserInfo.this, "Failed to fetch user data", Toast.LENGTH_SHORT).show());
        }

        cancel.setOnClickListener(v -> dialog.dismiss());

        ok.setOnClickListener(v -> {
            // Declare and assign before use
            String newUsername = editUsername.getText().toString().trim();
            String newEmail = editEmail.getText().toString().trim();

            if (newUsername.isEmpty() || newEmail.isEmpty()) {
                warningText.setVisibility(View.VISIBLE);
                warningText.setText("Please fill in all fields");
                return;
            } else {
                warningText.setVisibility(View.GONE);
            }

            new android.os.Handler().postDelayed(() -> {
                if (uid != null) {
                    db.collection("users").document(uid)
                            .update("username", newUsername, "email", newEmail)
                            .addOnSuccessListener(aVoid -> {
                                // Update SharedPreferences
                                SharedPreferences.Editor editor = prefs.edit();
                                editor.putString("username", newUsername);
                                editor.putString("email", newEmail);
                                editor.apply();

                                nameTextView.setText("Username : " + newUsername);
                                emailTextView.setText("Email : " + newEmail);

                                Toast.makeText(UserInfo.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            })
                            .addOnFailureListener(e -> {
                                warningText.setVisibility(View.VISIBLE);
                                warningText.setText("Failed to update profile");
                                Toast.makeText(UserInfo.this, "Failed to update profile", Toast.LENGTH_SHORT).show();
                            });
                }
            }, 1000); // 1-second delay for effect
        });

        dialog.show();
    }

    private void showSignOutPopup() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.popup_signout);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dimBackground(true);
        dialog.setOnDismissListener(d -> dimBackground(false));

        Button ok = dialog.findViewById(R.id.btn_ok);
        Button cancel = dialog.findViewById(R.id.btn_cancel);

        cancel.setOnClickListener(v -> dialog.dismiss());
        ok.setOnClickListener(v -> {
            SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.clear();
            editor.apply();

            Toast.makeText(this, "Signed out successfully", Toast.LENGTH_SHORT).show();
            dialog.dismiss();

            Intent intent = new Intent(UserInfo.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        dialog.show();
    }

    private void dimBackground(boolean dim) {
        if (profileLayout != null) {
            profileLayout.setAlpha(dim ? 0.3f : 1.0f);
        }
    }
}
