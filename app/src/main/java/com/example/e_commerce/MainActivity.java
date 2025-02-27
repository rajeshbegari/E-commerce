package com.example.e_commerce;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.e_commerce.adapters.CategoryPagerAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
    private ViewPager2 viewPager;
    private TabLayout categoryTabs;
    private TextView userNameText;
    private BottomNavigationView bottomNavigation;
    private final String[] categories = {"Men's Clothing", "Women's Clothing", "Kids' Clothing",
            "Accessories", "Footwear"};
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        setupViews();
        setupViewPager();
        loadUserName();
        setupBottomNavigation();
    }

    private void setupViews() {
        viewPager = findViewById(R.id.viewPager);
        categoryTabs = findViewById(R.id.categoryTabs);
        userNameText = findViewById(R.id.userNameText);
        bottomNavigation = findViewById(R.id.bottomNavigation);
    }

    private void loadUserName() {
        String userId = auth.getCurrentUser().getUid();
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String userName = documentSnapshot.getString("name");
                        userNameText.setText("Hi, " + userName);
                    }
                });
    }

    private void setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_home) {
                return true;
            } else if (itemId == R.id.navigation_cart) {
                startActivity(new Intent(this, CartActivity.class));
                return true;
            } else if (itemId == R.id.navigation_orders) {
                startActivity(new Intent(this, OrdersActivity.class));
                return true;
            } else if (itemId == R.id.navigation_account) {
                startActivity(new Intent(this, AccountActivity.class));
                return true;
            }
            return false;
        });
    }

    private void setupViewPager() {
        CategoryPagerAdapter pagerAdapter = new CategoryPagerAdapter(this, categories);
        viewPager.setAdapter(pagerAdapter);

        new TabLayoutMediator(categoryTabs, viewPager,
                (tab, position) -> tab.setText(categories[position])
        ).attach();
    }
}