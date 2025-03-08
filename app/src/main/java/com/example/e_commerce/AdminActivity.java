package com.example.e_commerce;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.e_commerce.adapters.SearchResultAdapter;
import com.example.e_commerce.models.Product;
import com.example.e_commerce.models.*;
import com.example.e_commerce.LoginFragment;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.auth.FirebaseAuth;
import java.util.ArrayList;
import java.util.List;


public class AdminActivity extends AppCompatActivity implements SearchResultAdapter.ProductActionListener {
    private SearchView searchView;
    private RecyclerView searchResultsRecyclerView;
    private SearchResultAdapter searchAdapter;
    private List<Product> searchResults;
    private View buttonsLayout;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        initializeViews();
        setupFirestore();
        setupSearchView();
    }

    private void initializeViews() {
        searchView = findViewById(R.id.searchView);
        searchResultsRecyclerView = findViewById(R.id.searchResultsRecyclerView);
        buttonsLayout = findViewById(R.id.buttonsLayout);

        // Initialize RecyclerView
        searchResults = new ArrayList<>();
        searchAdapter = new SearchResultAdapter(searchResults, this);
        searchResultsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        searchResultsRecyclerView.setAdapter(searchAdapter);

        MaterialButton addProductButton = findViewById(R.id.addProductButton);
        MaterialButton manageOrdersButton = findViewById(R.id.manageOrdersButton);
        MaterialButton manageUsersButton = findViewById(R.id.manageUsersButton);
        MaterialButton logoutButton = findViewById(R.id.logoutButton);

        // Set up button click listeners
        addProductButton.setOnClickListener(v -> navigateToAddProduct());
        manageOrdersButton.setOnClickListener(v -> navigateToManageOrders());
        manageUsersButton.setOnClickListener(v -> navigateToManageUsers());
        logoutButton.setOnClickListener(v -> performLogout());
    }

    private void setupFirestore() {
        db = FirebaseFirestore.getInstance();
    }

    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                performSearch(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) {
                    // Hide search results and show buttons when search is cleared
                    searchResultsRecyclerView.setVisibility(View.GONE);
                    buttonsLayout.setVisibility(View.VISIBLE);
                    searchResults.clear();
                    searchAdapter.notifyDataSetChanged();
                } else {
                    performSearch(newText);
                }
                return true;
            }
        });

        // Add close listener to handle when search is closed
        searchView.setOnCloseListener(() -> {
            searchResultsRecyclerView.setVisibility(View.GONE);
            buttonsLayout.setVisibility(View.VISIBLE);
            return false;
        });
    }

    private void performSearch(String query) {
        if (query.trim().isEmpty()) {
            return;
        }

        String searchQuery = query.toLowerCase().trim();
        db.collection("products")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                searchResults.clear();
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    Product product = document.toObject(Product.class);
                    product.setId(document.getId());
                    

                    if (product.getName().toLowerCase().contains(searchQuery) ||
                        product.getDescription().toLowerCase().contains(searchQuery)) {
                        searchResults.add(product);
                    }
                }
                
                // Show/hide views based on search results
                if (!searchResults.isEmpty()) {
                    searchResultsRecyclerView.setVisibility(View.VISIBLE);
                    buttonsLayout.setVisibility(View.GONE);
                } else {
                    Toast.makeText(this, "No products found", Toast.LENGTH_SHORT).show();
                    searchResultsRecyclerView.setVisibility(View.GONE);
                    buttonsLayout.setVisibility(View.VISIBLE);
                }
                
                searchAdapter.notifyDataSetChanged();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Error searching products: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            });
    }

    private void navigateToAddProduct() {
        Intent intent = new Intent(this, AddProductActivity.class);
        startActivity(intent);
    }

    private void navigateToManageOrders() {
        Intent intent = new Intent(this, ManageOrdersActivity.class);
        startActivity(intent);
    }

    private void navigateToManageUsers() {
        Intent intent = new Intent(this, ManageUsersActivity.class);
        startActivity(intent);
    }

    private void performLogout() {
        // Sign out from Firebase Auth
        FirebaseAuth.getInstance().signOut();
        
        // Clear shared preferences
        SharedPreferences preferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.apply();

        // Create intent to start MainActivity which will host the LoginFragment
        Intent intent = new Intent(this, AuthActivity.class);

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        // Add extra to indicate that LoginFragment should be loaded
      //  intent.putExtra("load_login", true);
        startActivity(intent);
        finish();
    }

    // Implement the interface methods
    @Override
    public void onEditProduct(Product product) {
        Intent intent = new Intent(this, AddProductActivity.class);
        intent.putExtra("product_id", product.getId());
        intent.putExtra("is_edit", true);
        startActivity(intent);
    }

    @Override
    public void onDeleteProduct(Product product) {
        new AlertDialog.Builder(this)
            .setTitle("Delete Product")
            .setMessage("Are you sure you want to delete this product?")
            .setPositiveButton("Delete", (dialog, which) -> {
                db.collection("products").document(product.getId())
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        searchResults.remove(product);
                        searchAdapter.notifyDataSetChanged();
                        
                        if (searchResults.isEmpty()) {
                            searchResultsRecyclerView.setVisibility(View.GONE);
                            buttonsLayout.setVisibility(View.VISIBLE);
                        }
                        
                        Toast.makeText(this, "Product deleted successfully", 
                            Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error deleting product: " + e.getMessage(), 
                            Toast.LENGTH_SHORT).show();
                    });
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
} 