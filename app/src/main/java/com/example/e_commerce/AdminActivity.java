package com.example.e_commerce;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;

public class AdminActivity extends AppCompatActivity {
    private MaterialButton manageUsersButton, addProductButton, logoutButton;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        auth = FirebaseAuth.getInstance();
        manageUsersButton = findViewById(R.id.manageUsersButton);
        addProductButton = findViewById(R.id.addProductButton);
        logoutButton = findViewById(R.id.logoutButton);

        manageUsersButton.setOnClickListener(v -> 
            startActivity(new Intent(this, ManageUsersActivity.class)));

        addProductButton.setOnClickListener(v -> 
            startActivity(new Intent(this, AddProductActivity.class)));

        logoutButton.setOnClickListener(v -> {
            auth.signOut();
            startActivity(new Intent(this, AuthActivity.class));
            finish();
        });
    }
} 