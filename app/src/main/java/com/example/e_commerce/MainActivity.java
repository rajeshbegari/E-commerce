package com.example.e_commerce;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

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
    private View mainContent;
    private View fragmentContainer;
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

        if (getIntent().getBooleanExtra("load_login", false)) {
            showLoginFragment();
        } else {
            showMainContent();
        }
    }

    private void setupViews() {
        fragmentContainer = findViewById(R.id.fragment_container);
        mainContent = findViewById(R.id.mainContent);
        viewPager = findViewById(R.id.viewPager);
        categoryTabs = findViewById(R.id.categoryTabs);
        userNameText = findViewById(R.id.userNameText);
        bottomNavigation = findViewById(R.id.bottomNavigation);
    }

    private void showLoginFragment() {
        fragmentContainer.setVisibility(View.VISIBLE);
        mainContent.setVisibility(View.GONE);
        bottomNavigation.setVisibility(View.GONE);

        LoginFragment loginFragment = new LoginFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, loginFragment);
        transaction.commit();
    }

    private void showMainContent() {
        fragmentContainer.setVisibility(View.GONE);
        mainContent.setVisibility(View.VISIBLE);
        bottomNavigation.setVisibility(View.VISIBLE);

        setupViewPager();
        loadUserName();
        setupBottomNavigation();
    }

    private void loadUserName() {
        if (auth.getCurrentUser() != null) {
            String userId = auth.getCurrentUser().getUid();
            db.collection("users").document(userId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String userName = documentSnapshot.getString("name");
                            userNameText.setText("Hello, " + userName);
                        }
                    });
        }
    }

    private void setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_home) {
                showMainContent();
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

    public void navigateToHome() {
        if (bottomNavigation != null) {
            bottomNavigation.setSelectedItemId(R.id.navigation_home);
        }
        if (viewPager != null) {
            viewPager.setCurrentItem(0, true); // Scroll to first tab with animation
        }
    }

    public void navigateToManageOrders() {
        Intent intent = new Intent(this, ManageOrdersActivity.class);
        startActivity(intent);
    }
}