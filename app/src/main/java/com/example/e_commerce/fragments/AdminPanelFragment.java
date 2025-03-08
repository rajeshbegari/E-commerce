package com.example.e_commerce.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.example.e_commerce.R;
import com.example.e_commerce.adapters.AdminProductAdapter;
import com.example.e_commerce.models.Product;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminPanelFragment extends Fragment implements AdminProductAdapter.ProductActionListener {
    private RecyclerView productsRecyclerView;
    private AdminProductAdapter adapter;
    private FirebaseFirestore db;
    private List<Product> productList;
    private List<Product> filteredList;
    private FloatingActionButton addProductFab;
    private SearchView searchView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_panel, container, false);
        initViews(view);
        setupFirestore();
        loadProducts();
        return view;
    }

    private void initViews(View view) {
        productsRecyclerView = view.findViewById(R.id.productsRecyclerView);
        addProductFab = view.findViewById(R.id.addProductFab);
        searchView = view.findViewById(R.id.searchView);
        
        productList = new ArrayList<>();
        filteredList = new ArrayList<>();
        adapter = new AdminProductAdapter(filteredList, this);
        productsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        productsRecyclerView.setAdapter(adapter);

        addProductFab.setOnClickListener(v -> showAddProductDialog());

        setupSearchView();
    }

    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterProducts(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterProducts(newText);
                return true;
            }
        });
    }

    private void filterProducts(String query) {
        filteredList.clear();
        if (query.isEmpty()) {
            filteredList.addAll(productList);
        } else {
            String searchQuery = query.toLowerCase().trim();
            for (Product product : productList) {
                if (product.getName().toLowerCase().contains(searchQuery) ||
                    product.getDescription().toLowerCase().contains(searchQuery)) {
                    filteredList.add(product);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void setupFirestore() {
        db = FirebaseFirestore.getInstance();
    }

    private void loadProducts() {
        db.collection("products")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                productList.clear();
                filteredList.clear();
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    Product product = document.toObject(Product.class);
                    product.setId(document.getId());
                    productList.add(product);
                }
                filteredList.addAll(productList);
                adapter.notifyDataSetChanged();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(getContext(), "Error loading products: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    private void showAddProductDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_product_edit, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setView(dialogView);
        builder.setTitle("Add New Product");

        EditText nameInput = dialogView.findViewById(R.id.productNameInput);
        EditText priceInput = dialogView.findViewById(R.id.productPriceInput);
        EditText descriptionInput = dialogView.findViewById(R.id.productDescriptionInput);
        EditText imageUrlInput = dialogView.findViewById(R.id.productImageUrlInput);
        Button saveButton = dialogView.findViewById(R.id.saveButton);
        Button cancelButton = dialogView.findViewById(R.id.cancelButton);

        AlertDialog dialog = builder.create();

        saveButton.setOnClickListener(v -> {
            String name = nameInput.getText().toString().trim();
            String priceStr = priceInput.getText().toString().trim();
            String description = descriptionInput.getText().toString().trim();
            String imageUrl = imageUrlInput.getText().toString().trim();

            if (name.isEmpty() || priceStr.isEmpty()) {
                Toast.makeText(getContext(), "Name and price are required", Toast.LENGTH_SHORT).show();
                return;
            }

            double price = Double.parseDouble(priceStr);
            Map<String, Object> product = new HashMap<>();
            product.put("name", name);
            product.put("price", price);
            product.put("description", description);
            product.put("imageUrl", imageUrl);

            db.collection("products")
                .add(product)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(getContext(), "Product added successfully", Toast.LENGTH_SHORT).show();
                    loadProducts();
                    dialog.dismiss();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error adding product: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
        });

        cancelButton.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void showEditProductDialog(Product product) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_product_edit, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setView(dialogView);
        builder.setTitle("Edit Product");

        EditText nameInput = dialogView.findViewById(R.id.productNameInput);
        EditText priceInput = dialogView.findViewById(R.id.productPriceInput);
        EditText descriptionInput = dialogView.findViewById(R.id.productDescriptionInput);
        EditText imageUrlInput = dialogView.findViewById(R.id.productImageUrlInput);
        Button saveButton = dialogView.findViewById(R.id.saveButton);
        Button cancelButton = dialogView.findViewById(R.id.cancelButton);

        // Pre-fill existing values
        nameInput.setText(product.getName());
        priceInput.setText(String.valueOf(product.getPrice()));
        descriptionInput.setText(product.getDescription());
        imageUrlInput.setText(product.getImageUrl());

        AlertDialog dialog = builder.create();

        saveButton.setOnClickListener(v -> {
            String name = nameInput.getText().toString().trim();
            String priceStr = priceInput.getText().toString().trim();
            String description = descriptionInput.getText().toString().trim();
            String imageUrl = imageUrlInput.getText().toString().trim();

            if (name.isEmpty() || priceStr.isEmpty()) {
                Toast.makeText(getContext(), "Name and price are required", Toast.LENGTH_SHORT).show();
                return;
            }

            double price = Double.parseDouble(priceStr);
            Map<String, Object> updates = new HashMap<>();
            updates.put("name", name);
            updates.put("price", price);
            updates.put("description", description);
            updates.put("imageUrl", imageUrl);

            db.collection("products").document(product.getId())
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Product updated successfully", Toast.LENGTH_SHORT).show();
                    loadProducts();
                    dialog.dismiss();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error updating product: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
        });

        cancelButton.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    @Override
    public void onEditProduct(Product product) {
        showEditProductDialog(product);
    }

    @Override
    public void onDeleteProduct(Product product) {
        new AlertDialog.Builder(requireContext())
            .setTitle("Delete Product")
            .setMessage("Are you sure you want to delete this product?")
            .setPositiveButton("Delete", (dialog, which) -> {
                db.collection("products").document(product.getId())
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        productList.remove(product);
                        filterProducts(searchView.getQuery().toString()); // Update filtered list
                        Toast.makeText(getContext(), "Product deleted successfully", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Error deleting product: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
} 