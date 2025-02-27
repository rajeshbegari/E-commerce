package com.example.e_commerce.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.e_commerce.R;
import com.example.e_commerce.adapters.ProductAdapter;
import com.example.e_commerce.models.Product;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ProductFragment extends Fragment {
    private String category;
    private RecyclerView recyclerView;
    private ProductAdapter adapter;
    private List<Product> productList;
    private FirebaseFirestore db;

    public static ProductFragment newInstance(String category) {
        ProductFragment fragment = new ProductFragment();
        Bundle args = new Bundle();
        args.putString("category", category);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            category = getArguments().getString("category");
        }
        db = FirebaseFirestore.getInstance();
        productList = new ArrayList<>();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_product, container, false);
        recyclerView = view.findViewById(R.id.productsRecyclerView);
        setupRecyclerView();
        loadProducts();
        return view;
    }

    private void setupRecyclerView() {
        adapter = new ProductAdapter(productList, this::addToCart);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        recyclerView.setAdapter(adapter);
    }

    private void loadProducts() {
        db.collection("products")
                .whereEqualTo("category", category)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(getContext(), "Error loading products",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    productList.clear();
                    for (QueryDocumentSnapshot doc : value) {
                        Product product = doc.toObject(Product.class);
                        product.setId(doc.getId());
                        productList.add(product);
                    }
                    adapter.notifyDataSetChanged();
                });
    }

    private void addToCart(Product product) {
        // TODO: Implement add to cart functionality
        Toast.makeText(getContext(), "Added to cart: " + product.getName(),
                Toast.LENGTH_SHORT).show();
    }
} 